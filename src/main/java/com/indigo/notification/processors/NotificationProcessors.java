package com.indigo.notification.processors;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.bean.validator.BeanValidationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.indigo.notification.Data.NotificationRequest;
import com.indigo.notification.Data.NotificationRequest.PushRecipient;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintViolation;

/**
 * This class contains Camel `Processor` implementations used within the Camel routes
 * for transforming requests, generating responses, and handling validation errors.
 */
@ApplicationScoped
public class NotificationProcessors {

    @ConfigProperty(name = "auth.appId")
    String appId;

    @ConfigProperty(name = "auth.accountId")
    String accountId;

    @ConfigProperty(name = "auth.apiKey")
    String apiKey;

    @ConfigProperty(name = "vendor.name")
    String vendorName;

    @ConfigProperty(name = "current.topic")
    String currentTopic;

    @ConfigProperty(name = "log.message")
    String logMessage;

@SuppressWarnings("unchecked")
public Processor sendResponseProcessor() {
    return exchange -> {
        // Get the original payload from exchange property
        Map<String, Object> originalRequest = exchange.getProperty("originalRequest2", Map.class);
        Map<String, Object> response = new HashMap<>();

        if (originalRequest == null) {
            throw new IllegalStateException("originalRequest2 property not found in exchange");
        }

        // Copy all key-value pairs from original request
        response.putAll(originalRequest);

        // Extract recipient.userId (if available) for vendor metadata
        Map<String, Object> recipient = (Map<String, Object>) originalRequest.get("recipient");
        String recipientMobile = (recipient != null && recipient.get("userId") != null)
                ? recipient.get("userId").toString()
                : "unknown";

        // Add vendor details
        String vendorId = UUID.randomUUID().toString();
        Map<String, Object> vendor = Map.of(
            "id", Map.of(recipientMobile, vendorId),
            "name", vendorName
        );

        // Add metadata to response
        response.put("currentStage", "Message Posted to " + vendorName);
        response.put("currentTopic", currentTopic);
        response.put("log", logMessage);
        response.put("timeStamp", java.time.ZonedDateTime.now().toString());
        response.put("vendor", vendor);

        // Set the final response as the new message body
        exchange.getIn().setBody(response);
    };
}


// public Processor sendResponseProcessor() {
//     return exchange -> {
//         NotificationRequest input = exchange.getProperty("originalRequest", NotificationRequest.class);
//         NotificationRequest.PushRecipient recipient = input.getPushRecipients().get(0); // already validated non-null

//         String recipientMobile = recipient.getUserId(); // e.g., +9786********
//         String vendorId = UUID.randomUUID().toString(); // Simulated vendor mapping ID

//         Map<String, Object> vendor = Map.of(
//             "id", Map.of(
//                 recipientMobile, vendorId
//             ),
//             "name", vendorName
//         );

//         Map<String, Object> response = new HashMap<>();

//         response.put("requestId", input.getRequestId());
//         response.put("messageId", input.getMessageId());
//         response.put("applicationId", input.getApplicationId());
//         response.put("applicationName", input.getApplicationName());
//         response.put("templateId", input.getTemplateId());
//         response.put("priorityQueue", input.getPriorityQueue());
//         response.put("channel", input.getChannel());
//         response.put("content", input.getContent());
//         response.put("bulkContent", input.getBulkContent());
//         response.put("recipients", List.of(recipientMobile));
//         response.put("currentStage", "Message Posted to "+vendorName);
//         response.put("currentTopic", "statusTopic");
//         response.put("log", "Message sent to the opentelemetry");
//         response.put("timeStamp", java.time.ZonedDateTime.now().toString());
//         response.put("vendor", vendor);

//         exchange.getIn().setBody(response);
//     };
// }



public Processor buildNotificationPayloadProcessor() {
        return exchange -> {
                NotificationRequest input = exchange.getProperty("originalRequest", NotificationRequest.class);
                PushRecipient recipient = input.getPushRecipients().get(0); 
                // MDC.put("userId", recipient.getUserId());
                // MDC.put("eventName", input.getTemplateName());

                Map<String, Object> payload = Map.of(
                    "auth", Map.of(
                        "appId", appId,
                        "accountId", accountId,
                        "apiKey", apiKey
                    ),
                    "data", createDataPayload(input,recipient)
                );

                exchange.getIn().setBody(payload);
        };
    }

    private Map<String, Object> createDataPayload(NotificationRequest input, PushRecipient recipient) {
        String eventId = UUID.randomUUID().toString();
        long timestamp = System.currentTimeMillis();
        
        return Map.of(
            "appuid", recipient.getAppuid(),
            "userId", recipient.getUserId(),
            "eventId", eventId,
            "sessionId", eventId,
            "platform", recipient.getPlatform(),
            "eventName", input.getTemplateName(),
            "eventAttributes", input.getAttributes(),
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
            "status", "fail"
        );
        exchange.getMessage().setBody(response);
    };
}

public Processor OriginalRequestProcessor() {
    return exchange -> {
        NotificationRequest request = exchange.getIn().getBody(NotificationRequest.class);
        exchange.setProperty("originalRequest", request); // 🔐 Save original request
    };
}

        // public Processor LogOriginalRequestProcessor() {
        //     return exchange -> {
        //         Object originalRequest = exchange.getProperty("originalRequest2");

        //         if (originalRequest instanceof Map<?, ?> requestMap) {
        //             System.out.println("Captured values in originalRequest:");
        //             requestMap.forEach((key, value) -> {
        //                 System.out.println("Key: " + key + ", Value: " + value);
        //             });
        //         } else {
        //             System.out.println("originalRequest is not a Map or is null.");
        //         }
        //     };
        // }

@SuppressWarnings("unchecked")
public Processor OriginalRequestProcessor2() {
    return exchange -> {
        Map<String, Object> rawPayload = exchange.getMessage().getBody(Map.class);
        exchange.setProperty("originalRequest2", rawPayload);

    };
}



}