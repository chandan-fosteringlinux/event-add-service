package com.notification.route;

import java.util.HashSet;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.bean.validator.BeanValidationException;
import org.apache.camel.spi.MaskingFormatter;
import org.apache.camel.support.processor.DefaultMaskingFormatter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.notification.Data.NotificationRequest;
import com.notification.notificationService.NotificationServiceException;
import com.notification.processors.NotificationProcessors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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
                from("kafka:test-topic?brokers=localhost:9092")
            .routeId("kafka-validation-route")
            .log("Raw Kafka message: ${body}")
                .unmarshal().json(NotificationRequest.class)
                .to("bean-validator://notification-validation")
                .log("Validation passed for received request")
                .process(processors.buildNotificationPayloadProcessor())
                .log("Sending to UPSHOT: ${body}")
                .to("bean:NotificationBean?method=addExchange")                               
                .process(processors.sendResponseProcessor())
                .log("Sending to status-topic: ${body}")
                .to("kafka:status-topic?brokers=localhost:9092");
            
    }

    private void configureExceptionHandling() {
    onException(BeanValidationException.class)
        .handled(true)
        .process(processors.ExceptionResponseProcessor())
        .marshal().json()
        .log("Validation failed. Sending to exception-topic: ${body}")
        .to("kafka:exception-topic?brokers=localhost:9092");

    onException(NotificationServiceException.class)
        .handled(true)
        .log("Service error: ${exception.message}")
        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(502))
        .setBody(simple("{\"error\": \"Notification service unavailable\"}"))
        .log("Sending to exception-topic: ${body}")
        .to("kafka:exception-topic?brokers=localhost:9092");

    onException(Exception.class)
        .handled(true)
        .log("Unexpected error: ${exception.message}")
        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
        .setBody(simple("{\"error\": \"Internal server error\"}"))
        .log("Sending to exception-topic: ${body}")
        .to("kafka:exception-topic?brokers=localhost:9092");
}


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
