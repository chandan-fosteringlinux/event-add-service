package com.notification.notificationService;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
 public class NotificationServiceException extends RuntimeException {
    public NotificationServiceException(String message) {
        super(message);
    }

    public NotificationServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}