package com.notification.notificationService;


import java.util.Map;

import jakarta.ws.rs.core.Response;

public interface NotificationService {
    Response sendNotification(Map<String, Object> payload);
}
