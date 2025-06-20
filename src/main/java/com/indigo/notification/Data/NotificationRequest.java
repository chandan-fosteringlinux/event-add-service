package com.indigo.notification.Data;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationRequest {

    @NotNull(message = "templateName must not be null")
    private String templateName;

    @NotNull(message = "attribute object must not be null")
    @Size(min = 1, message = "attributes map must not be empty")
    private Map<String, Object> attributes;

    @NotNull(message = "pushRecipients list must not be null")
    @Size(min = 1, message = "pushRecipients list must contain at least one recipient")
    @Valid
    private List<@Valid PushRecipient> pushRecipients;

    @Data
    public static class PushRecipient {

        @NotNull(message = "appuId must not be null")
        @Size(min = 1, message = "appuid must be a non-empty string")
        private String appuid;

        @NotNull(message = "userId must not be null")
        @Size(min = 1, message = "userId must be a non-empty string")
        private String userId;

        @NotNull(message = "platform must not be null")
        @Size(min = 1, message = "platform must be a non-empty string")
        private String platform;
    }
}