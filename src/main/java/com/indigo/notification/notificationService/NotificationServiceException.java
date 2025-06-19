package com.indigo.notification.notificationService;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Custom runtime exception to represent errors during notification service operations.
 */
@ApplicationScoped
 public class NotificationServiceException extends RuntimeException {
    public NotificationServiceException(String message) {
        super(message);
    }

    public NotificationServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}