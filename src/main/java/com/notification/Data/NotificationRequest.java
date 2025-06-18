package com.notification.Data;


import java.util.Map;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Represents the incoming JSON payload for notification requests.
 * Includes validation constraints for each field.
 */
@Data
public class NotificationRequest {

    @NotNull
    @Size(min = 10, max = 10)
    private String userId;

    @NotNull
    @Pattern(regexp = "^[a-z]+([A-Z][a-z]+)*$", message = "must be in camel case (e.g., webApp)")
    private String platform;

    @NotNull
    private String eventName;

    @NotNull
    private Map<String, Object> eventAttributes;
}
