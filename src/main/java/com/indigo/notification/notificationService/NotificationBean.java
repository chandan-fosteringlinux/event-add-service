package com.indigo.notification.notificationService;

import java.util.Map;

import org.apache.camel.Exchange;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.Response;

/**
 * CDI Bean that acts as a Camel `bean:` endpoint to send notifications.
 * Uses the `NotificationService` to send requests and handle the HTTP response.
 */
@ApplicationScoped
@Named("NotificationBean")
public class NotificationBean {

    @Inject
    NotificationService notificationService;

    public void SendNotificationBean(Exchange exchange) {
    @SuppressWarnings("unchecked")
    Map<String, Object> payload = exchange.getIn().getBody(Map.class);

    try (Response response = notificationService.sendNotification(payload)) {
        int status = response.getStatus();
        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, status);
        exchange.getMessage().setBody(response.readEntity(String.class));

        if (status != 200) {
                // Throw custom exception for non-200 responses
                throw new NotificationServiceException("Downstream service error. HTTP Status: " + status);
            }
    }
    catch (Exception e) {
        // Handle exceptions from the notification service
        throw new NotificationServiceException("Failed to process notification", e);
    }
}

}