package com.indigo.notification.Data;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Represents the full notification request payload.
 * Includes validation constraints for userId, platform, templateName, and attributes.
 */
@Data
public class NotificationRequest {

    private String requestId;
    private Integer messageId;

    @NotNull(message = "templateName must not be null")
    private String templateName;

    private Integer applicationId;
    private String applicationName;
    private Integer templateId;
    private String priorityQueue;

    private String content;
    private String subject;
    private String vfContent;
    private String bulkContent;

    private String channel;
    private String businessName;

    private Boolean enrichmentRequired;
    private String timeStamp;

    private Boolean bulkMessage;
    private Boolean whatsappMediaEnabled;

    private Object recipients;
    private Object ccRecipients;
    private Object bccRecipients;
    private Object attachments;
    private Object whatsappAttachment;

    @NotNull(message = "attribute object must not be null")
    @Size(min = 1, message = "attributes map must not be empty")
    private Map<String, Object> attributes;

    @NotNull(message = "pushRecipients list must not be null")
    @Size(min = 1, message = "pushRecipients list must contain at least one recipient")
    @Valid
    private List<@Valid PushRecipient> pushRecipients;

    /**
     * Inner class representing an individual push recipient.
     * Includes validation for userId and platform.
     */
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