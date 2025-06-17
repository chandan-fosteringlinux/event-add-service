package com.notification.route;

import java.util.HashSet;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.spi.MaskingFormatter;
import org.apache.camel.support.processor.DefaultMaskingFormatter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.notification.notificationService.NotificationServiceImpl;
import com.notification.processors.NotificationProcessors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;

@ApplicationScoped
public class NotificationRoute extends RouteBuilder {

    @ConfigProperty(name = "quarkus.log.level", defaultValue = "INFO")
    String maskingLevel;

    @Inject
    NotificationProcessors processors;

    @Override
    public void configure() {
        // Configure MDC logging
        getContext().setUseMDCLogging(true);
        getContext().setMDCLoggingKeysPattern("userId,event");
        
        // Configure sensitive data masking
        configureMasking();
        
        // REST configuration
        restConfiguration()
            .bindingMode(RestBindingMode.json)
            .enableCORS(true);

        // Global error handling
        configureExceptionHandling();
        
        // REST endpoint
        rest("/addNotification")
            .post()
            .consumes(MediaType.APPLICATION_JSON)
            .produces(MediaType.APPLICATION_JSON)
            .description("Send notification to UPSHOT")
            .to("direct:callAddNotification");

        // Processing route
        from("direct:callAddNotification")
            .routeId("notification-processing-route")
            // .process(processors.trackingProcessor())
            .log("Request received: ${body}")
            
            .doTry()
                .process(processors.validationProcessor())
                .log("validation passed for received request")
                .process(processors.buildNotificationPayloadProcessor())
                .log("Sending to UPSHOT: ${body}")
                .to("bean:NotificationBean?method=addExchange")
                .log("UPSHOT response: ${header.CamelHttpResponseCode} - ${body}")
                
                // Handle non-2xx responses
                .choice()
                    .when(header(Exchange.HTTP_RESPONSE_CODE).isGreaterThanOrEqualTo(300))
                        .throwException(new NotificationServiceImpl.NotificationServiceException(
                            "Downstream service error: ${header.CamelHttpResponseCode}", null))
                .end()
                
                .process(processors.SendResponseProcessor())

            .endDoTry()
            
            .doCatch(WebApplicationException.class)
                .log("Validation error: ${exception.message}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setBody(simple("{\"error\": \"${exception.message}\"}"))
            .doCatch(NotificationServiceImpl.NotificationServiceException.class)
                .log("Service error: ${exception.message}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(502))
                .setBody(simple("{\"error\": \"Notification service unavailable\"}"))
            .doCatch(Exception.class)
                .log("Unexpected error: ${exception.message}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .setBody(simple("{\"error\": \"Internal server error\"}"))
            .end();
    }

    private void configureExceptionHandling() {
        onException(WebApplicationException.class)
            .handled(true)
            .setHeader(Exchange.HTTP_RESPONSE_CODE, simple("${exception.status}"))
            .setBody(simple("{\"error\": \"${exception.message}\"}"))
            .log("Validation error: ${exception.message}");
        
        onException(NotificationServiceImpl.NotificationServiceException.class)
            .handled(true)
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(502))
            .setBody(constant("{\"error\": \"Notification service unavailable\"}"))
            .log("Service error: ${exception.message}");
        
        onException(Exception.class)
            .handled(true)
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
            .setBody(constant("{\"error\": \"Internal server error\"}"))
            .log("Unexpected error: ${exception.stacktrace}");
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