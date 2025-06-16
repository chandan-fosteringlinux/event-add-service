package com.notification.Data;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class ValidationUtils {

    private static final Pattern USER_ID_PATTERN = Pattern.compile("^.{2,40}$");
    private static final Pattern PLATFORM_PATTERN = Pattern.compile("^.{1,20}$");
    private static final Pattern EVENT_NAME_PATTERN = Pattern.compile("^.{2,40}$");

    private static final Set<String> ALLOWED_FIELDS = Set.of("userId", "platform", "eventName", "eventAttributes");

    public static void validateNotificationRequest(Map<String, Object> payload) {
        if (payload == null) {
            throw new WebApplicationException("Request body cannot be empty", 400);
        }

        // Reject unknown fields
        for (String key : payload.keySet()) {
            if (!ALLOWED_FIELDS.contains(key)) {
                throw new WebApplicationException("Invalid fields in request" + key, 400);
            }
        }

        validateField(payload, "userId", "User ID is required", USER_ID_PATTERN, "User ID is not valid (must be 2–40 characters)");
        validateField(payload, "platform", "Platform is required", PLATFORM_PATTERN, "Platform is not valid (must be 1–20 characters)");
        validateField(payload, "eventName", "Event name is required", EVENT_NAME_PATTERN, "Event name is not valid (must be 2–40 characters)");

        Object eventAttrs = payload.get("eventAttributes");
        if (!(eventAttrs instanceof Map)) {
            throw new WebApplicationException("Event attributes must be a valid JSON object", 400);
        }
    }

    private static void validateField(Map<String, Object> payload, String field, String requiredMessage, Pattern pattern, String patternMessage) {
        if (!payload.containsKey(field)) {
            throw new WebApplicationException(requiredMessage, 400);
        }

        Object value = payload.get(field);
        if (value == null || !(value instanceof String) || ((String) value).isBlank()) {
            throw new WebApplicationException(requiredMessage, 400);
        }

        String stringValue = (String) value;
        if (!pattern.matcher(stringValue).matches()) {
            throw new WebApplicationException(patternMessage, 400);
        }
    }
}
