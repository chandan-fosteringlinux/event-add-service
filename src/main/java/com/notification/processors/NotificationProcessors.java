package com.notification.processors;

import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.bean.validator.BeanValidationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.MDC;

import com.notification.Data.NotificationRequest;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintViolation;

@ApplicationScoped
public class NotificationProcessors {

    @ConfigProperty(name = "auth.appId")
    String appId;

    @ConfigProperty(name = "auth.accountId")
    String accountId;

    @ConfigProperty(name = "auth.apiKey")
    String apiKey;

public Processor sendResponseProcessor() {
    return exchange -> {
        // Build response
        Map<String, Object> response = Map.of(
            "message", "Notification processed successfully",
            "status", "success",
            "traceId", getTraceId()
        );

        exchange.getIn().setBody(response);
    };
}

public String getTraceId() {
        Span currentSpan = Span.current();
        SpanContext spanContext = currentSpan.getSpanContext();
        return spanContext.isValid() ? spanContext.getTraceId() : "no-trace-id";
}


public Processor buildNotificationPayloadProcessor() {
        return exchange -> {
                NotificationRequest input = exchange.getIn().getBody(NotificationRequest.class);
                MDC.put("userId", input.getUserId());
                MDC.put("event", input.getEventName());

                Map<String, Object> payload = Map.of(
                    "auth", Map.of(
                        "appId", appId,
                        "accountId", accountId,
                        "apiKey", apiKey
                    ),
                    "data", createDataPayload(input)
                );

                exchange.getIn().setBody(payload);
        };
    }

    private Map<String, Object> createDataPayload(NotificationRequest input) {
        String eventId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();
        
        return Map.of(
            "appuid", input.getUserId(),
            "userId", input.getUserId(),
            "eventId", eventId,
            "sessionId", eventId,
            "platform", input.getPlatform(),
            "eventName", input.getEventName(),
            "eventAttributes", input.getEventAttributes(),
            "startTime", timestamp,
            "endTime", timestamp,
            "tzoffset", TimeZone.getDefault().getRawOffset()
        );
    }
    
    

public Processor ExceptionResponseProcessor() {
    return exchange -> {
        Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        String message = "Validation error";
        
        if (exception instanceof BeanValidationException bve) {
            Set<ConstraintViolation<Object>> violations = bve.getConstraintViolations();
            if (!violations.isEmpty()) {
                message = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
            }
        }
        
        Map<String, String> response = Map.of(
            "message", message,
            "status", "fail",
            "traceId", getTraceId()
        );
        exchange.getMessage().setBody(response);
    };
}


}