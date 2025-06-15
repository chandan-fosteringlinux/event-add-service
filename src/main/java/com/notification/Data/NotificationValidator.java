// package com.notification.Data;
// import java.util.Map;
// import java.util.Set;

// import jakarta.enterprise.context.ApplicationScoped;
// import jakarta.inject.Inject;
// import jakarta.validation.ConstraintViolation;
// import jakarta.validation.Validator;
// import jakarta.ws.rs.WebApplicationException;
// import jakarta.ws.rs.core.Response;

// @ApplicationScoped
// public class NotificationValidator {

//     @Inject
//     Validator validator;

//     public void validate(NotificationRequest request) {
//         Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);

//         if (!violations.isEmpty()) {
//             // Collect all validation messages
//             StringBuilder errorMessage = new StringBuilder();
//             for (ConstraintViolation<NotificationRequest> violation : violations) {
//                 errorMessage.append(violation.getMessage()).append("; ");
//             }

//             // Throw HTTP 400 with combined error message
//             throw new WebApplicationException(
//                 Response.status(Response.Status.BAD_REQUEST)
//                         .entity(Map.of("error", "Validation failure: " + errorMessage.toString().trim()))
//                         .build()
//             );
//         }
//     }
// }
