package com.indigo.notification.notificationService;


import java.util.Map;

import jakarta.ws.rs.core.Response;

/**
 * Interface for the notification service that defines the contract for sending notifications.
 */
public interface NotificationService {
    Response sendNotification(Map<String, Object> payload);
}
