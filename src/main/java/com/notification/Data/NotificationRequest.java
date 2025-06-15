// package com.notification.Data;
// import java.util.Map;

// import jakarta.validation.constraints.NotNull;
// import jakarta.validation.constraints.Size;

// public class NotificationRequest {

//     @NotNull(message = "User ID is required")
//     @Size(min = 2, max = 40, message = "User ID must be between 2 and 40 characters")
//     private String userId;

//     @NotNull(message = "Platform is required")
//     @Size(min = 1, max = 20, message = "Platform must be between 1 and 20 characters")
//     private String platform;

//     @NotNull(message = "Event name is required")
//     @Size(min = 2, max = 40, message = "Event name must be between 2 and 40 characters")
//     private String eventName;

//     @NotNull(message = "Event attributes are required")
//     private Map<String, Object> eventAttributes;

//     // Getters and Setters
//     public String getUserId() {
//         return userId;
//     }

//     public void setUserId(String userId) {
//         this.userId = userId;
//     }

//     public String getPlatform() {
//         return platform;
//     }

//     public void setPlatform(String platform) {
//         this.platform = platform;
//     }

//     public String getEventName() {
//         return eventName;
//     }

//     public void setEventName(String eventName) {
//         this.eventName = eventName;
//     }

//     public Map<String, Object> getEventAttributes() {
//         return eventAttributes;
//     }

//     public void setEventAttributes(Map<String, Object> eventAttributes) {
//         this.eventAttributes = eventAttributes;
//     }
// }
