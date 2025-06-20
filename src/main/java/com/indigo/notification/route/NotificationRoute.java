package com.indigo.notification.route;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.bean.validator.BeanValidationException;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.component.kafka.consumer.KafkaManualCommit;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.spi.MaskingFormatter;
import org.apache.camel.support.processor.DefaultMaskingFormatter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.indigo.notification.Data.NotificationRequest;
import com.indigo.notification.notificationService.NotificationServiceException;
import com.indigo.notification.processors.NotificationProcessors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;


/**
 * This class defines the main route for processing notifications using Apache Camel.
 * It includes message validation, transformation, error handling, and logging.
 */
@ApplicationScoped
public class NotificationRoute extends RouteBuilder {

    @ConfigProperty(name = "quarkus.log.level", defaultValue = "INFO")
    String maskingLevel;

    @Inject
    NotificationProcessors processors;

    @Override
    public void configure() {

        // Apply masking if not in DEBUG mode
        configureMasking();

        // Exception handler
        configureExceptionHandling();

        // Kafka processing route
            
from("{{kafka.request.topic.uri}}")
    .routeId("kafka-validation-route")
    // Enable manual commit in URI: add allowManualCommit=true
    .log("Raw Kafka message: ${body}")
    .unmarshal().json(JsonLibrary.Jackson, Map.class)
    .process(processors.OriginalRequestProcessor2())
    // .process(processors.LogOriginalRequestProcessor())
    .marshal().json(JsonLibrary.Jackson)
    .unmarshal().json(NotificationRequest.class)
    .to("bean-validator")
    .process(processors.OriginalRequestProcessor())
    .log("Validation passed for request")
    .process(processors.buildNotificationPayloadProcessor())
    .log("Sending to UPSHOT: ${body}")
    .to("bean:NotificationBean?method=SendNotificationBean")
    .process(processors.sendResponseProcessor())
    .log("Sending to status-topic: ${body}")
    
    // Send to status topic (critical point - must succeed before commit)
    .to("{{kafka.status.topic.uri}}")
    
    // MANUAL OFFSET COMMIT (ACKNOWLEDGMENT)
    .process(exchange -> {
    KafkaManualCommit manual = exchange.getIn().getHeader(
        KafkaConstants.MANUAL_COMMIT, 
        KafkaManualCommit.class
    );

    if (manual != null) {
        manual.commit();
        log.info("allowManualCommit=true → Manual commit successful for partition {}",
            exchange.getIn().getHeader(KafkaConstants.PARTITION));
    } else {
        log.warn("KafkaManualCommit is NULL → Is allowManualCommit=false?");
    }
   })

    .log("Processing complete - offset committed");        
    }

    private void configureExceptionHandling() {

       onException(MismatchedInputException.class)
        .handled(true)
        .log("Invalid JSON input: ${exception.message}")
        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
        .setBody().constant("{\"error\": \"Invalid JSON format or missing fields\"}")
        .to("{{kafka.status.topic.uri}}");

    onException(BeanValidationException.class)
        .handled(true)
        .process(processors.ExceptionResponseProcessor())
        .marshal().json()
        .log("Validation failed. Sending to status-topic: ${body}")
        .to("{{kafka.status.topic.uri}}");

    onException(NotificationServiceException.class)
        .handled(true)
        .log("Service error: ${exception.message}")
        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(502))
        .setBody(simple("{\"error\": \"Notification service unavailable\"}"))
        .log("Sending to status-topic: ${body}")
        .to("{{kafka.status.topic.uri}}");

    onException(Exception.class)
        .handled(true)
        .log("Unexpected error: ${exception.message}")
        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
        .setBody(simple("{\"error\": \"Internal server error\"}"))
        .log("Sending to status-topic: ${body}")
        .to("{{kafka.status.topic.uri}}");

// Catch truly malformed JSON like "this is not JSON"
    onException(JsonParseException.class)
        .handled(true)
        .log("Malformed JSON: ${exception.message}")
        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
        .setBody().constant("{\"error\": \"Malformed JSON - please send a valid JSON object\"}")
        .to("{{kafka.status.topic.uri}}");
}


 /**
     * Applies sensitive field masking for logs based on log level.
     * Fields such as apiKey, appId, and accountId are masked in logs to avoid leakage of sensitive information.
     */
    private void configureMasking() {
        if (!"DEBUG".equalsIgnoreCase(maskingLevel)) {
            Set<String> keywords = new HashSet<>();
            keywords.add("apiKey");
            keywords.add("appId");
            keywords.add("accountId");

            DefaultMaskingFormatter formatter = new DefaultMaskingFormatter(keywords, true, true, true);
            getContext().getRegistry().bind(MaskingFormatter.CUSTOM_LOG_MASK_REF, formatter);
            getContext().setLogMask(true);
        }
    }
}