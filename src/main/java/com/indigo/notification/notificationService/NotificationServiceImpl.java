package com.indigo.notification.notificationService;

import java.util.Map;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.indigo.notification.client.NotificationClient;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/**
 * Implementation of the `NotificationService` interface that makes
 * an HTTP call to the downstream notification client.
 */
@ApplicationScoped
public class NotificationServiceImpl implements NotificationService {

    @Inject
    @RestClient
    NotificationClient notificationClient;
    

    @Override
    public Response sendNotification(Map<String, Object> payload) {
        try {
            return notificationClient.sendNotification(payload);
        } catch (WebApplicationException | ProcessingException e) {
            throw new NotificationServiceException("Failed to call notification service", e);
        }
    }
}