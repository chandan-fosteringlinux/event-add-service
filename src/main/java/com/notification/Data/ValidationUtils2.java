// package com.notification.Data;

// import java.util.Set;

// import jakarta.enterprise.context.ApplicationScoped;
// import jakarta.inject.Inject;
// import jakarta.validation.ConstraintViolation;
// import jakarta.validation.Validator;
// import jakarta.ws.rs.WebApplicationException;

// @ApplicationScoped
// public class ValidationUtils2 {

//     @Inject
//     Validator validator; // ✅ non-static

//     public void validateNotificationRequest(NotificationRequest request) { // ✅ non-static
//         Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);
//         if (!violations.isEmpty()) {
//             StringBuilder sb = new StringBuilder("Validation failed:");
//             for (ConstraintViolation<NotificationRequest> v : violations) {
//                 sb.append(" ").append(v.getPropertyPath()).append(": ").append(v.getMessage()).append(";");
//             }
//             throw new WebApplicationException(sb.toString(), 400);
//         }
//     }
// }
