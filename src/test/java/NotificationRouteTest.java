
// import io.quarkus.test.junit.QuarkusTest;

// @QuarkusTest // Or use @QuarkusTest if in Quarkus context
// @ExtendWith(MockitoExtension.class)
// public class NotificationRouteTest {

//     @InjectMocks
//     NotificationRoute route;

//     @Mock
//     NotificationProcessors processors;

//     @Test
//     public void testConfigureRouteLoadsSuccessfully() throws Exception {
//         DefaultCamelContext context = new DefaultCamelContext();
//         context.addRoutes(route);
//         context.start();
//         assertTrue(context.getRouteDefinition("kafka-validation-route") != null);
//         context.stop();
//     }

//     @Test
//     public void testExceptionHandlingForInvalidJson() throws Exception {
//         // Setup route and send invalid JSON
//         // Use AdviceWithRouteBuilder to inject test logic or replace endpoints with mocks
//     }

//     // You can test processors individually as well
//     @Test
//     public void testOriginalRequestProcessorCalled() throws Exception {
//         Exchange mockExchange = new DefaultExchange(new DefaultCamelContext());
//         Map<String, Object> body = Map.of("someKey", "someValue");
//         mockExchange.getIn().setBody(body);

//         Processor processor = route.processors.OriginalRequestProcessor2();
//         processor.process(mockExchange);

//         // Assert properties or output
//         assertEquals(body, mockExchange.getProperty("originalRequest2"));
//     }
// }
