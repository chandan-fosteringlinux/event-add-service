// package com.notification.route;

// import java.util.List;
// import java.util.Set;
// import java.util.stream.Collectors;

// import org.apache.camel.Exchange;
// import org.apache.camel.builder.RouteBuilder;
// import org.apache.camel.model.rest.RestBindingMode;

// import com.notification.Data.ErrorResponse;
// import com.notification.Data.NotificationRequest;

// import jakarta.enterprise.context.ApplicationScoped;
// import jakarta.validation.ConstraintViolation;
// import jakarta.validation.ConstraintViolationException;
// import jakarta.ws.rs.core.MediaType;

// @ApplicationScoped
// public class NotificationRoutecustom extends RouteBuilder {

//     // @Inject
//     // NotificationProcessors processors;

//     @Override
//     public void configure() {
//         restConfiguration()
//             .bindingMode(RestBindingMode.json)
//             .enableCORS(true);

//         // Global validation error handler
//         onException(ConstraintViolationException.class)
//             .handled(true)
//             .process(this::handleValidationErrors)
//             .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
//             .setHeader(Exchange.CONTENT_TYPE, constant(MediaType.APPLICATION_JSON))
//             .marshal().json();

//         // REST endpoint
//         rest("/addNotification")
//             .post()
//             .consumes(MediaType.APPLICATION_JSON)
//             .produces(MediaType.APPLICATION_JSON)
//             .type(NotificationRequest.class) // Bind to DTO
//             .description("Send notification to UPSHOT")
//             .to("direct:callAddNotification");

//         // Processing route
//         from("direct:callAddNotification")
//             .routeId("notification-processing-route")
//             .log("Request received: ${body}")
//             // .process(processors.buildNotificationPayloadProcessor())
//             .log("Sending to UPSHOT: ${body}");
//             // .to("bean:NotificationBean?method=addExchange")
//             // .log("UPSHOT response: ${header.CamelHttpResponseCode} - ${body}");
//     }

//     // Handle validation errors
//     private void handleValidationErrors(Exchange exchange) {
//         ConstraintViolationException exception = exchange.getProperty(
//             Exchange.EXCEPTION_CAUGHT, 
//             ConstraintViolationException.class
//         );
        
//         Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
//         List<String> errors = violations.stream()
//             .map(v -> v.getPropertyPath() + ": " + v.getMessage())
//             .collect(Collectors.toList());

//         ErrorResponse errorResponse = new ErrorResponse(
//             "VALIDATION_FAILED",
//             "Input validation failed",
//             errors
//         );
        
//         exchange.getMessage().setBody(errorResponse);
//     }
// }