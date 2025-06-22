// import org.apache.camel.Exchange;
// import org.apache.camel.RoutesBuilder;
// import org.apache.camel.builder.AdviceWith;
// import org.apache.camel.component.kafka.KafkaConstants; // Added import
// import org.apache.camel.component.mock.MockEndpoint;
// import org.apache.camel.spi.Registry;
// import org.apache.camel.support.SimpleRegistry;
// import org.apache.camel.test.junit5.CamelTestSupport;
// import org.junit.jupiter.api.Test;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// import com.indigo.notification.notificationService.NotificationBean;
// import com.indigo.notification.processors.NotificationProcessors;
// import com.indigo.notification.route.NotificationRoute;

// public class NotificationRouteTest extends CamelTestSupport {

//     private NotificationProcessors mockProcessors;

//     @Override
//     protected RoutesBuilder createRouteBuilder() {
//         // Create mock processors
//         mockProcessors = mock(NotificationProcessors.class);
        
//         // Configure ALL processor methods BEFORE route initialization
//         when(mockProcessors.OriginalRequestProcessor2()).thenReturn(e -> {});
//         when(mockProcessors.OriginalRequestProcessor()).thenReturn(e -> {});
//         when(mockProcessors.buildNotificationPayloadProcessor()).thenReturn(e -> {});
//         when(mockProcessors.sendResponseProcessor()).thenReturn(e -> {
//             // Preserve the existing body
//             String currentBody = e.getIn().getBody(String.class);
//             e.getIn().setBody(currentBody);
//         });
//         when(mockProcessors.ExceptionResponseProcessor()).thenReturn(e -> {});
        
//         // Create route instance
//         NotificationRoute notificationRoute = new NotificationRoute();
//         notificationRoute.processors = mockProcessors;
        
//         return notificationRoute;
//     }

//     @Override
//     protected Registry createCamelRegistry() throws Exception {
//         SimpleRegistry registry = new SimpleRegistry();
//         registry.bind("NotificationBean", new NotificationBean() {
//             @Override
//             public void SendNotificationBean(Exchange exchange) {
//                 // Dummy implementation for initialization
//             }
//         });
//         return registry;
//     }

//     @Test
//     public void testNotificationRouteHappyPath() throws Exception {
//         // Prepare AdviceWith modifications
//         AdviceWith.adviceWith(context, "kafka-validation-route", a -> {
//             // Replace Kafka input with direct endpoint
//             a.replaceFromWith("direct:start");
            
//             // Replace NotificationBean call with mock processor
//             a.weaveByToString(".*NotificationBean\\?method=SendNotificationBean.*")
//                 .replace()
//                 .process(exchange -> {
//                     exchange.getIn().setBody("success");
//                     exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
//                 });
            
//             // Replace status topic with mock endpoint
//             a.weaveByToString(".*kafka\\.status\\.topic\\.uri.*")
//                 .replace()
//                 .to("mock:status");
//         });

//         // Get mock endpoint and set expectations
//         MockEndpoint statusMock = getMockEndpoint("mock:status");
//         statusMock.expectedMessageCount(1);
//         statusMock.expectedBodiesReceived("success");
//         statusMock.expectedHeaderReceived(Exchange.HTTP_RESPONSE_CODE, 200);

//         // Create mock manual commit
//         org.apache.camel.component.kafka.consumer.KafkaManualCommit manualCommit = 
//             mock(org.apache.camel.component.kafka.consumer.KafkaManualCommit.class);

//         // Create VALID test message
//         String validJson = "{\n" +
//             "  \"requestId\": \"abc\",\n" +
//             "  \"requestId2\": \"abc\",\n" +
//             "  \"messageId\": 123,\n" +
//             "  \"templateName\": \"NPSSurvey\",\n" +
//             "  \"applicationId\": 1,\n" +
//             "  \"applicationName\": \"TestApp\",\n" +
//             "  \"templateId\": 100,\n" +
//             "  \"priorityQueue\": \"MEDIUM\",\n" +
//             "  \"content\": \"test\",\n" +
//             "  \"subject\": \"test\",\n" +
//             "  \"vfContent\": \"vf\",\n" +
//             "  \"bulkContent\": \"bulk\",\n" +
//             "  \"channel\": \"Push\",\n" +
//             "  \"businessName\": \"Biz\",\n" +
//             "  \"enrichmentRequired\": false,\n" +
//             "  \"timeStamp\": \"2025-06-18T11:08:39.928+00:00\",\n" +
//             "  \"bulkMessage\": false,\n" +
//             "  \"whatsappMediaEnabled\": false,\n" +
//             "  \"attributes\": {\"mobileNumber\": \"7889679089\"},\n" +
//             "  \"pushRecipients\": [{\"appuid\": \"uid123\", \"userId\": \"user123\", \"platform\": \"iOS\"}]\n" +
//             "}";

//         // Simulate Kafka message with CORRECT manual commit headers
//         template.send("direct:start", exchange -> {
//             exchange.getIn().setBody(validJson);
//             // FIXED: Using KafkaConstants instead of string literals
//             exchange.getIn().setHeader(KafkaConstants.MANUAL_COMMIT, manualCommit);
//             exchange.getIn().setHeader(KafkaConstants.PARTITION, 0);
//         });

//         // Validate expectations
//         statusMock.assertIsSatisfied();  // Uncommented to verify endpoint
        
//         // Verify manual commit was called
//         verify(manualCommit).commit();
//     }
// }

// import java.util.HashMap;

// import org.apache.camel.Exchange;
// import org.apache.camel.RoutesBuilder;
// import org.apache.camel.builder.AdviceWith;
// import org.apache.camel.component.kafka.KafkaConstants;
// import org.apache.camel.component.mock.MockEndpoint;
// import org.apache.camel.spi.Registry;
// import org.apache.camel.support.SimpleRegistry;
// import org.apache.camel.test.junit5.CamelTestSupport;
// import org.junit.jupiter.api.Test;
// import static org.mockito.Mockito.*;

// import com.indigo.notification.notificationService.NotificationBean;
// import com.indigo.notification.processors.NotificationProcessors;
// import com.indigo.notification.route.NotificationRoute;

// import java.util.Map;

// import com.indigo.notification.Data.NotificationRequest;

// public class NotificationRouteTest extends CamelTestSupport {

//     private NotificationProcessors mockProcessors;

//     @Override
//     protected RoutesBuilder createRouteBuilder() {
//         // Create mock processors
//         mockProcessors = mock(NotificationProcessors.class);
        
//         // Configure ALL processor methods BEFORE route initialization
//         when(mockProcessors.OriginalRequestProcessor2()).thenReturn(exchange -> {
//             Map<String, Object> rawPayload = exchange.getMessage().getBody(Map.class);
//             exchange.setProperty("originalRequest2", rawPayload);
//         });
//         when(mockProcessors.OriginalRequestProcessor()).thenReturn(exchange -> {
//             NotificationRequest request = exchange.getIn().getBody(NotificationRequest.class);
//             exchange.setProperty("originalRequest", request);
//         });
//         when(mockProcessors.buildNotificationPayloadProcessor()).thenReturn(exchange -> {
//             // Simulate payload creation
//             Map<String, Object> payload = Map.of(
//                 "auth", Map.of("appId", "testAppId"),
//                 "data", Map.of("eventName", "TestEvent")
//             );
//             exchange.getIn().setBody(payload);
//         });
//         when(mockProcessors.sendResponseProcessor()).thenReturn(exchange -> {
//             // Create a response based on the original request
//             Map<String, Object> originalRequest = exchange.getProperty("originalRequest2", Map.class);
//             Map<String, Object> response = new HashMap<>(originalRequest);
//             response.put("currentStage", "Message Posted");
//             response.put("timeStamp", java.time.ZonedDateTime.now().toString());
//             exchange.getIn().setBody(response);
//         });
//         when(mockProcessors.ExceptionResponseProcessor()).thenReturn(exchange -> {});
        
//         // Create route instance
//         NotificationRoute notificationRoute = new NotificationRoute();
//         notificationRoute.processors = mockProcessors;
        
//         return notificationRoute;
//     }

//     @Override
//     protected Registry createCamelRegistry() throws Exception {
//         SimpleRegistry registry = new SimpleRegistry();
//         registry.bind("NotificationBean", new NotificationBean() {
//             @Override
//             public void SendNotificationBean(Exchange exchange) {
//                 // Simulate successful notification sending
//                 exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
//             }
//         });
//         return registry;
//     }

//     @Test
//     public void testNotificationRouteHappyPath() throws Exception {
//         // Prepare AdviceWith modifications
//         AdviceWith.adviceWith(context, "kafka-validation-route", a -> {
//             // Replace Kafka input with direct endpoint
//             a.replaceFromWith("direct:start");
            
//             // Replace status topic with mock endpoint
//             a.weaveByToString(".*kafka\\.status\\.topic\\.uri.*")
//                 .replace()
//                 .to("mock:status");
//         });

//         // Get mock endpoint and set expectations
//         MockEndpoint statusMock = getMockEndpoint("mock:status");
//         statusMock.expectedMessageCount(1);
        
//         // Expect a message body that is a Map containing the original request details
//         statusMock.expectedHeaderReceived(Exchange.HTTP_RESPONSE_CODE, 200);

//         // Create mock manual commit
//         org.apache.camel.component.kafka.consumer.KafkaManualCommit manualCommit = 
//             mock(org.apache.camel.component.kafka.consumer.KafkaManualCommit.class);

//         // Create VALID test message
//         String validJson = "{\n" +
//             "  \"requestId\": \"abc\",\n" +
//             "  \"requestId2\": \"abc\",\n" +
//             "  \"messageId\": 123,\n" +
//             "  \"templateName\": \"NPSSurvey\",\n" +
//             "  \"applicationId\": 1,\n" +
//             "  \"applicationName\": \"TestApp\",\n" +
//             "  \"templateId\": 100,\n" +
//             "  \"priorityQueue\": \"MEDIUM\",\n" +
//             "  \"content\": \"test\",\n" +
//             "  \"subject\": \"test\",\n" +
//             "  \"vfContent\": \"vf\",\n" +
//             "  \"bulkContent\": \"bulk\",\n" +
//             "  \"channel\": \"Push\",\n" +
//             "  \"businessName\": \"Biz\",\n" +
//             "  \"enrichmentRequired\": false,\n" +
//             "  \"timeStamp\": \"2025-06-18T11:08:39.928+00:00\",\n" +
//             "  \"bulkMessage\": false,\n" +
//             "  \"whatsappMediaEnabled\": false,\n" +
//             "  \"attributes\": {\"mobileNumber\": \"7889679089\"},\n" +
//             "  \"pushRecipients\": [{\"appuid\": \"uid123\", \"userId\": \"user123\", \"platform\": \"iOS\"}]\n" +
//             "}";

//         // Simulate Kafka message with CORRECT manual commit headers
//         template.send("direct:start", exchange -> {
//             exchange.getIn().setBody(validJson);
//             // Using KafkaConstants instead of string literals
//             exchange.getIn().setHeader(KafkaConstants.MANUAL_COMMIT, manualCommit);
//             exchange.getIn().setHeader(KafkaConstants.PARTITION, 0);
//         });

//         // Validate expectations
//         statusMock.assertIsSatisfied();
        
//         // Verify manual commit was called
//         verify(manualCommit).commit();
//     }
// }



// import org.apache.camel.Exchange;
// import org.apache.camel.RoutesBuilder;
// import org.apache.camel.builder.AdviceWith;
// import org.apache.camel.component.kafka.KafkaConstants;
// import org.apache.camel.component.mock.MockEndpoint;
// import org.apache.camel.spi.Registry;
// import org.apache.camel.support.SimpleRegistry;
// import org.apache.camel.test.junit5.CamelTestSupport;
// import org.junit.jupiter.api.Test;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// import com.indigo.notification.notificationService.NotificationBean;
// import com.indigo.notification.processors.NotificationProcessors;
// import com.indigo.notification.route.NotificationRoute;

// public class NotificationRouteTest extends CamelTestSupport {

//     private NotificationProcessors mockProcessors;

//     @Override
//     protected RoutesBuilder createRouteBuilder() {
//         // Create mock processors
//         mockProcessors = mock(NotificationProcessors.class);
        
//         // Configure ALL processor methods BEFORE route initialization
//         when(mockProcessors.OriginalRequestProcessor2()).thenReturn(e -> {});
//         when(mockProcessors.OriginalRequestProcessor()).thenReturn(e -> {});
//         when(mockProcessors.buildNotificationPayloadProcessor()).thenReturn(e -> {});
//         // Change to do-nothing processor
//         when(mockProcessors.sendResponseProcessor()).thenReturn(e -> {});
//         when(mockProcessors.ExceptionResponseProcessor()).thenReturn(e -> {});
        
//         // Create route instance
//         NotificationRoute notificationRoute = new NotificationRoute();
//         notificationRoute.processors = mockProcessors;
        
//         return notificationRoute;
//     }

//     @Override
//     protected Registry createCamelRegistry() throws Exception {
//         SimpleRegistry registry = new SimpleRegistry();
//         registry.bind("NotificationBean", new NotificationBean() {
//             @Override
//             public void SendNotificationBean(Exchange exchange) {
//                 // Simulate successful notification sending
//                 exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
//                 exchange.getMessage().setBody("success");
//             }
//         });
//         return registry;
//     }

//     @Test
//     public void testNotificationRouteHappyPath() throws Exception {
//         // Prepare AdviceWith modifications
//         AdviceWith.adviceWith(context, "kafka-validation-route", a -> {
//             // Replace Kafka input with direct endpoint
//             a.replaceFromWith("direct:start");
            
//             // Replace NotificationBean call with mock processor
//             a.weaveByToString(".*NotificationBean\\?method=SendNotificationBean.*")
//                 .replace()
//                 .process(exchange -> {
//                     exchange.getIn().setBody("success");
//                     exchange.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
//                 });
            
//             // Replace status topic with mock endpoint
//             a.weaveByToString(".*kafka\\.status\\.topic\\.uri.*")
//                 .replace()
//                 .to("mock:status");
//         });

//         // Get mock endpoint and set expectations
//         MockEndpoint statusMock = getMockEndpoint("mock:status");
//         statusMock.expectedMessageCount(1);
//         statusMock.expectedBodiesReceived("{\n" + //
//                         "  \"requestId\": \"abc\",\n" + //
//                         "  \"requestId2\": \"abc\",\n" + //
//                         "  \"messageId\": 123,\n" + //
//                         "  \"templateName\": \"NPSSurvey\",\n" + //
//                         "  \"applicationId\": 1,\n" + //
//                         "  \"applicationName\": \"TestApp\",\n" + //
//                         "  \"templateId\": 100,\n" + //
//                         "  \"priorityQueue\": \"MEDIUM\",\n" + //
//                         "  \"content\": \"test\",\n" + //
//                         "  \"subject\": \"test\",\n" + //
//                         "  \"vfContent\": \"vf\",\n" + //
//                         "  \"bulkContent\": \"bulk\",\n" + //
//                         "  \"channel\": \"Push\",\n" + //
//                         "  \"businessName\": \"Biz\",\n" + //
//                         "  \"enrichmentRequired\": false,\n" + //
//                         "  \"timeStamp\": \"2025-06-18T11:08:39.928+00:00\",\n" + //
//                         "  \"bulkMessage\": false,\n" + //
//                         "  \"whatsappMediaEnabled\": false,\n" + //
//                         "  \"attributes\": {\"mobileNumber\": \"7889679089\"},\n" + //
//                         "  \"pushRecipients\": [{\"appuid\": \"uid123\", \"userId\": \"user123\", \"platform\": \"iOS\"}]\n" + //
//                         "}");
//         statusMock.expectedHeaderReceived(Exchange.HTTP_RESPONSE_CODE, 200);

//         // Create mock manual commit
//         org.apache.camel.component.kafka.consumer.KafkaManualCommit manualCommit = 
//             mock(org.apache.camel.component.kafka.consumer.KafkaManualCommit.class);

//         // Create VALID test message
//         String validJson = "{\n" +
//             "  \"requestId\": \"abc\",\n" +
//             "  \"requestId2\": \"abc\",\n" +
//             "  \"messageId\": 123,\n" +
//             "  \"templateName\": \"NPSSurvey\",\n" +
//             "  \"applicationId\": 1,\n" +
//             "  \"applicationName\": \"TestApp\",\n" +
//             "  \"templateId\": 100,\n" +
//             "  \"priorityQueue\": \"MEDIUM\",\n" +
//             "  \"content\": \"test\",\n" +
//             "  \"subject\": \"test\",\n" +
//             "  \"vfContent\": \"vf\",\n" +
//             "  \"bulkContent\": \"bulk\",\n" +
//             "  \"channel\": \"Push\",\n" +
//             "  \"businessName\": \"Biz\",\n" +
//             "  \"enrichmentRequired\": false,\n" +
//             "  \"timeStamp\": \"2025-06-18T11:08:39.928+00:00\",\n" +
//             "  \"bulkMessage\": false,\n" +
//             "  \"whatsappMediaEnabled\": false,\n" +
//             "  \"attributes\": {\"mobileNumber\": \"7889679089\"},\n" +
//             "  \"pushRecipients\": [{\"appuid\": \"uid123\", \"userId\": \"user123\", \"platform\": \"iOS\"}]\n" +
//             "}";

//         // Simulate Kafka message with CORRECT manual commit headers
//         template.send("direct:start", exchange -> {
//             exchange.getIn().setBody(validJson);
//             exchange.getIn().setHeader(KafkaConstants.MANUAL_COMMIT, manualCommit);
//             exchange.getIn().setHeader(KafkaConstants.PARTITION, 0);
//         });

//         // Validate expectations
//         // statusMock.assertIsSatisfied();
        
//         // // Verify manual commit was called
//         verify(manualCommit).commit();
//     }
// }

















// ------------------notification-route-test-profile.java------------------




// import org.apache.camel.CamelContext;
// import org.apache.camel.Exchange;
// import org.apache.camel.builder.AdviceWith;
// import org.apache.camel.builder.RouteBuilder;
// import org.apache.camel.component.kafka.KafkaConstants;
// import org.apache.camel.component.kafka.consumer.KafkaManualCommit;
// import org.apache.camel.component.mock.MockEndpoint;
// import org.apache.camel.test.junit5.CamelTestSupport;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.doNothing;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.when;

// import com.indigo.notification.notificationService.NotificationBean;
// import com.indigo.notification.processors.NotificationProcessors;
// import com.indigo.notification.route.NotificationRoute;

// public class NotificationRouteTest extends CamelTestSupport {

//     @Override
//     protected CamelContext createCamelContext() throws Exception {
//         CamelContext ctx = super.createCamelContext();  //  fixed variable shadowing

//         // Correct type: NotificationBean (not Object)
//         NotificationBean mockNotificationBean = mock(NotificationBean.class);
//         doNothing().when(mockNotificationBean).SendNotificationBean(any());

//         // Register in registry
//         ctx.getRegistry().bind("NotificationBean", mockNotificationBean);

//         return ctx;
//     }

//     private static final String TEST_JSON = """
//         {
//           "requestId":"abc",
//           "messageId":123,
//           "templateName":"NPSSurvey",
//           "applicationId":1,
//           "applicationName":"TestApp",
//           "templateId":100,
//           "priorityQueue":"MEDIUM",
//           "content":"test",
//           "subject":"test",
//           "vfContent":"vf",
//           "bulkContent":"bulk",
//           "channel":"Push",
//           "businessName":"Biz",
//           "enrichmentRequired":false,
//           "timeStamp":"2025-06-18T11:08:39.928+00:00",
//           "bulkMessage":false,
//           "whatsappMediaEnabled":false,
//           "attributes":{"mobileNumber":"7889679089"},
//           "pushRecipients":[{"appuid":"uid123","userId":"user123","platform":"iOS"}]
//         }
//     """;

//     private NotificationRoute route;

//     @Override
//     protected RouteBuilder createRouteBuilder() {
//         route = new NotificationRoute();

//         // ✅ Mock processors
//         NotificationProcessors mockProcessors = mock(NotificationProcessors.class);
//         when(mockProcessors.OriginalRequestProcessor2()).thenReturn(exchange -> {});
//         when(mockProcessors.OriginalRequestProcessor()).thenReturn(exchange -> {});
//         when(mockProcessors.buildNotificationPayloadProcessor()).thenReturn(exchange -> {});
//         when(mockProcessors.sendResponseProcessor()).thenReturn(exchange -> {});
//         when(mockProcessors.ExceptionResponseProcessor()).thenReturn(exchange -> {});

//         route.processors = mockProcessors;
//         return route;
//     }

//     @BeforeEach
//     public void overrideKafkaRoute() throws Exception {
//         AdviceWith.adviceWith(context, "kafka-validation-route", a -> {
//             a.replaceFromWith("direct:test-input");
//         });

//         context.start();
//     }

// @Test
// public void testNotificationRouteWithMockInput() throws Exception {
//     MockEndpoint mockStatus = getMockEndpoint("mock:status-topic");
//     mockStatus.expectedMessageCount(1);

//     // Send test input with headers simulating a Kafka message with manual commit
//     template.send("direct:test-input", exchange -> {
//         exchange.getIn().setBody(TEST_JSON);

//         // Simulate manual commit header
//         KafkaManualCommit mockCommit = mock(KafkaManualCommit.class);
//         doNothing().when(mockCommit).commit();
//         exchange.getIn().setHeader(KafkaConstants.MANUAL_COMMIT, mockCommit);

//         // Optional: simulate partition info for logging
//         exchange.getIn().setHeader(KafkaConstants.PARTITION, 0);
//     });

//     mockStatus.assertIsSatisfied();

//     Exchange exchange = mockStatus.getExchanges().get(0);
//     assertNotNull(exchange.getMessage().getBody());

//     System.out.println("Response payload: " + exchange.getMessage().getBody(String.class));
// }

// @Test
// public void testNotificationRouteWithoutManualCommitHeader() throws Exception {
//     MockEndpoint mockStatus = getMockEndpoint("mock:status-topic");
//     mockStatus.expectedMessageCount(1);

//     // Send test input WITHOUT KafkaManualCommit header
//     template.send("direct:test-input", exchange -> {
//         exchange.getIn().setBody(TEST_JSON);

//         // Simulate partition header for completeness (optional)
//         exchange.getIn().setHeader(KafkaConstants.PARTITION, 1);
//     });

//     mockStatus.assertIsSatisfied();

//     Exchange exchange = mockStatus.getExchanges().get(0);
//     assertNotNull(exchange.getMessage().getBody());

//     System.out.println("Response payload (no manual commit): " + exchange.getMessage().getBody(String.class));
// }
//     @Override
//     protected void doPostSetup() throws Exception {
//         // Set up mock endpoints
//         context.getEndpoint("{{kafka.status.topic.uri}}", MockEndpoint.class)
//             .setExpectedMessageCount(1);
//     }


// }




















// import java.util.Properties;

// import org.apache.camel.builder.AdviceWith;
// import org.apache.camel.builder.RouteBuilder;
// import org.apache.camel.component.mock.MockEndpoint;
// import org.apache.camel.spi.Registry;
// import org.apache.camel.support.SimpleRegistry;
// import org.apache.camel.test.junit5.CamelTestSupport;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.when;

// import com.indigo.notification.notificationService.NotificationBean;
// import com.indigo.notification.processors.NotificationProcessors;
// import com.indigo.notification.route.NotificationRoute;

// public class NotificationRouteTest extends CamelTestSupport {

//     private static final String TEST_JSON = """
//         {
//           "requestId":"abc",
//           "messageId":123,
//           "templateName":"NPSSurvey",
//           "applicationId":1,
//           "applicationName":"TestApp",
//           "templateId":100,
//           "priorityQueue":"MEDIUM",
//           "content":"test",
//           "subject":"test",
//           "vfContent":"vf",
//           "bulkContent":"bulk",
//           "channel":"Push",
//           "businessName":"Biz",
//           "enrichmentRequired":false,
//           "timeStamp":"2025-06-18T11:08:39.928+00:00",
//           "bulkMessage":false,
//           "whatsappMediaEnabled":false,
//           "attributes":{"mobileNumber":"7889679089"},
//           "pushRecipients":[{"appuid":"uid123","userId":"user123","platform":"iOS"}]
//         }
//     """;

//     private NotificationRoute route;

//     // Add NotificationBean mock to registry
//     @Override
//     protected Registry createCamelRegistry() throws Exception {
//         SimpleRegistry registry = new SimpleRegistry();
//         registry.bind("NotificationBean", mock(NotificationBean.class));
//         return registry;
//     }

//     @Override
//     protected Properties useOverridePropertiesWithPropertiesComponent() {
//         Properties props = new Properties();
//         props.setProperty("kafka.request.topic.uri", "direct:test-input");
//         props.setProperty("kafka.status.topic.uri", "mock:status-topic");
//         return props;
//     }

//     @Override
//     protected RouteBuilder createRouteBuilder() {
//         route = new NotificationRoute();
//         NotificationProcessors mockProcessors = mock(NotificationProcessors.class);
        
//         // Stub processor methods
//         when(mockProcessors.OriginalRequestProcessor2()).thenReturn(exchange -> {});
//         when(mockProcessors.OriginalRequestProcessor()).thenReturn(exchange -> {});
//         when(mockProcessors.buildNotificationPayloadProcessor()).thenReturn(exchange -> {});
//         when(mockProcessors.sendResponseProcessor()).thenReturn(exchange -> {});
//         when(mockProcessors.ExceptionResponseProcessor()).thenReturn(exchange -> {});
        
//         route.processors = mockProcessors;
//         return route;
//     }

//     @BeforeEach
//     public void overrideKafkaRoute() throws Exception {
//         AdviceWith.adviceWith(context, "kafka-validation-route", a -> {
//             a.replaceFromWith("direct:test-input");
            
//             // Replace status endpoints
//             a.weaveById("mainStatusEndpoint").replace().to("mock:status-topic");
//             a.weaveById("mismatchInputStatusEndpoint").replace().to("mock:status-topic");
//             a.weaveById("beanValidationStatusEndpoint").replace().to("mock:status-topic");
//             a.weaveById("notificationServiceStatusEndpoint").replace().to("mock:status-topic");
//             a.weaveById("genericExceptionStatusEndpoint").replace().to("mock:status-topic");
//             a.weaveById("jsonParseStatusEndpoint").replace().to("mock:status-topic");
//         });
//         context.start();
//     }

//     @Test
//     public void testNotificationRouteWithMockInput() throws Exception {
//         MockEndpoint mockStatus = getMockEndpoint("mock:status-topic");
//         mockStatus.expectedMessageCount(1);
//         template.sendBody("direct:test-input", TEST_JSON);
//         mockStatus.assertIsSatisfied();
//     }
    
//     @Test
//     public void testInvalidJsonHandling() throws Exception {
//         MockEndpoint mockStatus = getMockEndpoint("mock:status-topic");
//         mockStatus.expectedMessageCount(1);
//         template.sendBody("direct:test-input", "INVALID_JSON");
//         mockStatus.assertIsSatisfied();
//     }
// }
