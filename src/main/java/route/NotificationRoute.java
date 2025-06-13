package route;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.spi.MaskingFormatter;
import org.apache.camel.support.processor.DefaultMaskingFormatter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import client.NotificationClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class NotificationRoute extends RouteBuilder {

    @ConfigProperty(name = "quarkus.log.level", defaultValue = "INFO")
    String maskingLevel;

    @ConfigProperty(name = "auth.appId")
    String appId;

    @ConfigProperty(name = "auth.accountId")
    String accountId;

    @ConfigProperty(name = "auth.apiKey")
    String apiKey;

    @Inject
    @RestClient
    NotificationClient notificationClient;

    @Override
    public void configure() throws Exception {
        // Enable MDC logging with specific keys
        getContext().setUseMDCLogging(true);
        getContext().setMDCLoggingKeysPattern("requestID,correlationId");

        boolean enableMasking = !"DEBUG".equalsIgnoreCase(maskingLevel);

        if (enableMasking) {
            Set<String> keywords = new HashSet<>();
            keywords.add("apiKey");
            keywords.add("appId");
            keywords.add("accountId");

            DefaultMaskingFormatter formatter = new DefaultMaskingFormatter(keywords, true, true, true);
            getContext().getRegistry().bind(MaskingFormatter.CUSTOM_LOG_MASK_REF, formatter);
            getContext().setLogMask(true);
        }

        restConfiguration()
            .bindingMode(RestBindingMode.json);

        rest("/addNotification")
            .post()
            .consumes("application/json")
            .produces("application/json")
            .to("direct:callAddNotification");

        RouteDefinition route = from("direct:callAddNotification")
            .routeId("call-notification-service");

        if (enableMasking) {
            route = route.logMask();
        }

        route
            // Set tracking identifiers
            .process(exchange -> {
                // Generate or reuse identifiers
                String requestID = exchange.getIn().getHeader("requestID", String.class);
                if (requestID == null) {
                    requestID = UUID.randomUUID().toString();
                    exchange.getIn().setHeader("requestID", requestID);
                }
                
                String correlationId = exchange.getIn().getHeader("correlationId", String.class);
                if (correlationId == null) {
                    correlationId = requestID; // Default to requestID if not provided
                    exchange.getIn().setHeader("correlationId", correlationId);
                }
                
                // Set request timestamp
                exchange.getIn().setHeader("requestTime", new Date());
            })
            // Log with tracking info using MDC
            // .log("Request start time ${header.requestTime} with ID: ${header.requestID}")
            .log("Request start time ${header.requestTime} with ID: ${header.requestID} Received user input: ${body}")
            .doTry()
    .process(exchange -> {
        Map<String, Object> input = exchange.getIn().getBody(Map.class);

        // Extract values from incoming payload
        String userId = (String) input.get("userId");
        String platform = (String) input.get("platform");
        String eventName = (String) input.get("eventName");
        Map<String, Object> eventAttributes = (Map<String, Object>) input.get("eventAttributes");

        // Generate UUID for eventId/sessionId
        String eventId = UUID.randomUUID().toString();

        // Epoch timestamp (milliseconds)
        long timestamp = System.currentTimeMillis();

        // Timezone offset (in milliseconds)
        int tzOffset = java.util.TimeZone.getDefault().getRawOffset();

        // Construct auth block
        Map<String, Object> auth = new HashMap<>();
        auth.put("appId", appId);
        auth.put("accountId", accountId);
        auth.put("apiKey", apiKey);

        // Construct data block
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

        // Final payload
        Map<String, Object> finalPayload = new HashMap<>();
        finalPayload.put("auth", auth);
        finalPayload.put("data", data);

        exchange.getIn().setBody(finalPayload);
    })
    .log("Request start time ${header.requestTime} with ID: ${header.requestID} Modified payload to send to external API: ${body}")
    .process(exchange -> {
                    Map<String, Object> payload = exchange.getIn().getBody(Map.class);
                    jakarta.ws.rs.core.Response response = notificationClient.sendNotification(payload);
                    
                    // Set response status code and body
                    exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, response.getStatus());
                    exchange.getMessage().setBody(response.readEntity(String.class));
                })
            .doCatch(Exception.class)
                .log("Error processing request ${header.requestID}: ${exception.message}")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(502))
                .setBody(simple("{\"error\": \"Gateway error: ${exception.message}\"}"))
            .end();
    }
}