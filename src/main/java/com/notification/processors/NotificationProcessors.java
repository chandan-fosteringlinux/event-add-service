package com.notification.processors;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.camel.Processor;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.MDC;

import com.notification.Data.ValidationUtils;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NotificationProcessors {

    @ConfigProperty(name = "auth.appId")
    String appId;

    @ConfigProperty(name = "auth.accountId")
    String accountId;

    @ConfigProperty(name = "auth.apiKey")
    String apiKey;

    // public Processor trackingProcessor() {
    //     return exchange -> {
    //         String requestID = exchange.getIn().getHeader("requestID", String.class);
    //             if (requestID == null) {
    //                 requestID = UUID.randomUUID().toString();
    //                 exchange.getIn().setHeader("requestID", requestID);
    //             }
    //         MDC.put("requestID", requestID);
    //     };
    // }

public Processor sendResponseProcessor() {
    return exchange -> {
        String requestId = exchange.getIn().getHeader("requestID", String.class);
        Map<String, Object> response = Map.of(
            "message", "Notification processed successfully",
            "status", "success",
            "requestId", requestId
        );
        exchange.getIn().setBody(response);
    };
}


    @SuppressWarnings("unchecked")
    public Processor buildNotificationPayloadProcessor() {
        return exchange -> {
            Map<String, Object> input = exchange.getIn().getBody(Map.class);
            
            // Add contextual info to logs
            MDC.put("userId", (String) input.get("userId"));
            MDC.put("event", (String) input.get("eventName"));
            
            String userId = (String) input.get("userId");
            String platform = (String) input.get("platform");
            String eventName = (String) input.get("eventName");
            Map<String, Object> eventAttributes = (Map<String, Object>) input.get("eventAttributes");

            String eventId = UUID.randomUUID().toString();
            long timestamp = System.currentTimeMillis();
            int tzOffset = TimeZone.getDefault().getRawOffset();

            Map<String, Object> auth = new HashMap<>();
            auth.put("appId", appId);
            auth.put("accountId", accountId);
            auth.put("apiKey", apiKey);

            Map<String, Object> data = new HashMap<>();
            data.put("appuid", userId);
            data.put("userId", userId);
            data.put("eventId", eventId);
            data.put("sessionId", eventId);
            data.put("platform", platform);
            data.put("eventName", eventName);
            data.put("eventAttributes", eventAttributes);
            data.put("startTime", timestamp);
            data.put("endTime", timestamp);
            data.put("tzoffset", tzOffset);

            Map<String, Object> finalPayload = new HashMap<>();
            finalPayload.put("auth", auth);
            finalPayload.put("data", data);

            exchange.getIn().setBody(finalPayload);
        };
    }
    
    

    public Processor validationProcessor() {
        return exchange -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = exchange.getIn().getBody(Map.class);
            ValidationUtils.validateNotificationRequest(payload);
        };
    }

    public Processor ResponseProcessor() {
        return exchange -> {
            String requestId = exchange.getIn().getHeader("requestID", String.class);
            Map<String, Object> response = Map.of(
                "message", "Notification processed successfully",
                "status", "success",
                "requestId", requestId
            );
            exchange.getIn().setBody(response);
        };
    }

    // @Inject
    // NotificationValidator notificationValidator;

    // @Inject
    // CamelContext camelContext;

    // public Processor validationProcessor() {
    //     return exchange -> {
    //         NotificationRequest request = exchange.getIn().getBody(NotificationRequest.class);
    //         notificationValidator.validate(request);  // 💡 Validates here
    //       String json = camelContext.getTypeConverter().convertTo(String.class, request);
    //         exchange.getIn().setBody(json);  
    //     };
    // }


}