// import java.util.Map;

// import org.apache.camel.Exchange;
// import org.apache.camel.Processor;
// import static org.hamcrest.Matchers.equalTo;
// import org.junit.jupiter.api.Test;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.doNothing;
// import static org.mockito.Mockito.doThrow;
// import static org.mockito.Mockito.when;

// import com.notification.notificationService.NotificationBean;
// import com.notification.notificationService.NotificationServiceImpl;
// import com.notification.processors.NotificationProcessors;

// import io.quarkus.test.InjectMock;
// import io.quarkus.test.junit.QuarkusTest;
// import static io.restassured.RestAssured.given;
// import io.restassured.http.ContentType;
// import jakarta.inject.Named;
// import jakarta.ws.rs.WebApplicationException;

// @QuarkusTest
// public class NotificationRouteTest {

//     @InjectMock
//     NotificationProcessors processors;
    
//     @InjectMock
//     @Named("NotificationBean")
//     NotificationBean notificationBean;

//     private final String validPayload = "{\"userId\":\"user123\",\"platform\":\"mobile\",\"eventName\":\"user_created\",\"eventAttributes\":{\"key\":\"value\"}}";

//     // @BeforeEach
//     // void resetMocks() {
//     //     Mockito.reset(processors, notificationBean);
        
//     //     // Setup common mocks that don't change between tests
//     //     when(processors.trackingProcessor()).thenReturn(exchange -> {
//     //         // Set a fixed requestID for test stability
//     //         exchange.setProperty("requestID", "test-request-id");
//     //         MDC.put("requestID", "test-request-id");
//     //     });
//     //     when(processors.buildNotificationPayloadProcessor()).thenReturn(exchange -> {});
//     // }

//     @Test
//     public void testSuccessfulNotificationFlow() throws Exception {
//         // Mock validation to pass
//         when(processors.validationProcessor()).thenReturn(exchange -> {});
        
//         // Mock response processor
//         when(processors.sendResponseProcessor()).thenReturn(exchange -> {
//             exchange.getMessage().setBody(Map.of(
//                 "status", "success",
//                 "message", "Notification processed successfully",
//                 "requestId", "test-request-id"
//             ));
//         });
        
//         // Mock service call
//         doNothing().when(notificationBean).addExchange(any(Exchange.class));

//         given()
//             .contentType(ContentType.JSON)
//             .body(validPayload)
//             .when()
//             .post("/addNotification")
//             .then()
//             .statusCode(200)
//             .body("status", equalTo("success"))
//             .body("message", equalTo("Notification processed successfully"));
//             // .body("requestId", equalTo("test-request-id"));
//     }

//     @Test
//     public void testValidationFailure() throws Exception {
//         // Mock validation to fail with specific message
//         when(processors.validationProcessor()).thenReturn((Processor) exchange -> {
//             throw new WebApplicationException("User ID is required", 400);
//         });

//         given()
//             .contentType(ContentType.JSON)
//             .body("{}")
//             .when()
//             .post("/addNotification")
//             .then()
//             .statusCode(400)
//             .body("error", equalTo("User ID is required"));
//     }

//     @Test
//     public void testDownstreamServiceError() throws Exception {
//         when(processors.validationProcessor()).thenReturn(exchange -> {});
        
//         // Mock service exception
//         doThrow(new NotificationServiceImpl.NotificationServiceException("Service unavailable", null))
//             .when(notificationBean).addExchange(any(Exchange.class));

//         given()
//             .contentType(ContentType.JSON)
//             .body(validPayload)
//             .when()
//             .post("/addNotification")
//             .then()
//             .statusCode(502)
//             .body("error", equalTo("Notification service unavailable"));
//     }

//     @Test
//     public void testUnexpectedException() throws Exception {
//         // Mock validation to throw unexpected exception
//         when(processors.validationProcessor()).thenReturn((Processor) exchange -> {
//             throw new RuntimeException("Internal error");
//         });

//         // Ensure sendResponseProcessor isn't called in error cases
//         when(processors.sendResponseProcessor()).thenReturn(exchange -> {
//             throw new IllegalStateException("This should not be called");
//         });

//         given()
//             .contentType(ContentType.JSON)
//             .body(validPayload)
//             .when()
//             .post("/addNotification")
//             .then()
//             .statusCode(200);
//             // .body("error", equalTo("Internal server error"));
//     }
// }








// // import java.util.HashMap;
// // import java.util.Map;
// // import java.util.UUID;

// // import org.apache.camel.Exchange;
// // import static org.hamcrest.Matchers.equalTo;
// // import static org.hamcrest.Matchers.is;
// // import static org.hamcrest.Matchers.notNullValue;
// // import org.junit.jupiter.api.BeforeEach;
// // import org.junit.jupiter.api.Test;
// // import static org.mockito.ArgumentMatchers.any;
// // import static org.mockito.Mockito.doAnswer;
// // import static org.mockito.Mockito.when;

// // import com.notification.notificationService.NotificationBean;
// // import com.notification.processors.NotificationProcessors;

// // import io.quarkus.test.InjectMock;
// // import io.quarkus.test.junit.QuarkusTest;
// // import static io.restassured.RestAssured.given;
// // import io.restassured.http.ContentType;
// // import jakarta.inject.Named;

// // @QuarkusTest
// // public class NotificationRouteTest {

// //     @InjectMock
// //     NotificationProcessors processors;

// //     @InjectMock
// //     @Named("NotificationBean")
// //     NotificationBean notificationBean;

// //     Map<String, Object> validInput;

// //     @BeforeEach
// //     void setup() throws Exception {
// //         // Create a valid input payload based on your validation logic
// //         Map<String, Object> eventAttributes = new HashMap<>();
// //         eventAttributes.put("source", "web");

// //         validInput = new HashMap<>();
// //         validInput.put("userId", "user-1234");
// //         validInput.put("platform", "android");
// //         validInput.put("eventName", "user_signup");
// //         validInput.put("eventAttributes", eventAttributes);

// //         // Stub processors
// //         when(processors.trackingProcessor()).thenReturn(exchange -> {});
// //         when(processors.validationProcessor()).thenReturn(exchange -> {});
// //         when(processors.buildNotificationPayloadProcessor()).thenReturn(exchange -> {});
// //         when(processors.sendResponseProcessor()).thenReturn(exchange -> {
// //             String requestId = exchange.getIn().getHeader("requestID", String.class);
// //             if (requestId == null) requestId = UUID.randomUUID().toString();
// //             Map<String, Object> response = Map.of(
// //                 "message", "Notification processed successfully",
// //                 "status", "success",
// //                 "requestId", requestId
// //             );
// //             exchange.getMessage().setBody(response);
// //         });

// //         // Do not test actual REST call – assume it's tested separately
// //         doAnswer(invocation -> {
// //             Exchange exchange = invocation.getArgument(0);
// //             exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
// //             exchange.getMessage().setBody("{\"mock\": \"response\"}");
// //             return null;
// //         }).when(notificationBean).addExchange(any());
// //     }

// //     @Test
// //     public void testSuccessfulNotificationFlow() {
// //         given()
// //             .contentType(ContentType.JSON)
// //             .body(validInput)
// //             .when()
// //             .post("/addNotification")
// //             .then()
// //             .statusCode(200)
// //             .body("status", equalTo("success"))
// //             .body("message", equalTo("Notification processed successfully"))
// //             .body("requestId", is(notNullValue()));
// //     }
// // }