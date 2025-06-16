// package com.notification.processors;

// import java.util.Set;
// import java.util.stream.Collectors;

// import org.apache.camel.Exchange;
// import org.apache.camel.Processor;

// import com.notification.Data.NotificationRequest;

// import jakarta.enterprise.context.ApplicationScoped;
// import jakarta.inject.Inject;
// import jakarta.validation.ConstraintViolation;
// import jakarta.validation.Validator;
// import jakarta.ws.rs.WebApplicationException;

// @ApplicationScoped
// public class ValidationProcessor implements Processor {

//     @Inject
//     Validator validator;

//     @Override
//     public void process(Exchange exchange) {
//         NotificationRequest request = exchange.getIn().getBody(NotificationRequest.class);
//         Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);

//         if (!violations.isEmpty()) {
//             String errorMessage = violations.stream()
//                     .map(v -> v.getPropertyPath() + ": " + v.getMessage())
//                     .collect(Collectors.joining(", "));
//             throw new WebApplicationException(errorMessage, 400);
//         }
//     }
// }

