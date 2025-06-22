import java.util.Properties;

import org.apache.camel.Exchange;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.component.kafka.consumer.KafkaManualCommit;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spi.Registry;
import org.apache.camel.support.SimpleRegistry;
import org.apache.camel.test.junit5.CamelTestSupport;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.indigo.notification.notificationService.NotificationBean;
import com.indigo.notification.processors.NotificationProcessors;
import com.indigo.notification.route.NotificationRoute;

public class NotificationRouteTest extends CamelTestSupport {

    private static final String TEST_JSON = """
        {
          "requestId":"abc",
          "messageId":123,
          "templateName":"NPSSurvey",
          "applicationId":1,
          "applicationName":"TestApp",
          "templateId":100,
          "priorityQueue":"MEDIUM",
          "content":"test",
          "subject":"test",
          "vfContent":"vf",
          "bulkContent":"bulk",
          "channel":"Push",
          "businessName":"Biz",
          "enrichmentRequired":false,
          "timeStamp":"2025-06-18T11:08:39.928+00:00",
          "bulkMessage":false,
          "whatsappMediaEnabled":false,
          "attributes":{"mobileNumber":"7889679089"},
          "pushRecipients":[{"appuid":"uid123","userId":"user123","platform":"iOS"}]
        }
    """;

    private NotificationRoute route;

    // Create mock NotificationBean
    @Override
    protected Registry createCamelRegistry() throws Exception {
        SimpleRegistry registry = new SimpleRegistry();
        registry.bind("NotificationBean", mock(NotificationBean.class));
        return registry;
    }

    // Override properties
    @Override
    protected Properties useOverridePropertiesWithPropertiesComponent() {
        Properties props = new Properties();
        props.setProperty("kafka.request.topic.uri", "direct:test-input");
        props.setProperty("kafka.status.topic.uri", "mock:status-topic");
        return props;
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        route = new NotificationRoute();
        NotificationProcessors mockProcessors = mock(NotificationProcessors.class);
        
        // Stub processor methods
        when(mockProcessors.OriginalRequestProcessor2()).thenReturn(exchange -> {});
        when(mockProcessors.OriginalRequestProcessor()).thenReturn(exchange -> {});
        when(mockProcessors.buildNotificationPayloadProcessor()).thenReturn(exchange -> {});
        when(mockProcessors.sendResponseProcessor()).thenReturn(exchange -> {});
        when(mockProcessors.ExceptionResponseProcessor()).thenReturn(exchange -> {});
        
        route.processors = mockProcessors;
        return route;
    }

    @BeforeEach
    public void overrideKafkaRoute() throws Exception {
        // Replace all status endpoints using their IDs
        AdviceWith.adviceWith(context, "kafka-validation-route", a -> {
            a.replaceFromWith("direct:test-input");
            
            // Replace main status endpoint
            a.weaveById("mainStatusEndpoint").replace().to("mock:status-topic");
            
            // Replace exception handler endpoints
            a.weaveById("mismatchInputStatusEndpoint").replace().to("mock:status-topic");
            a.weaveById("beanValidationStatusEndpoint").replace().to("mock:status-topic");
            a.weaveById("notificationServiceStatusEndpoint").replace().to("mock:status-topic");
            a.weaveById("genericExceptionStatusEndpoint").replace().to("mock:status-topic");
            a.weaveById("jsonParseStatusEndpoint").replace().to("mock:status-topic");
        });
        context.start();
    }

    @Test
    public void testNotificationRouteWithMockInput() throws Exception {
        MockEndpoint mockStatus = getMockEndpoint("mock:status-topic");
        mockStatus.expectedMessageCount(1);
        template.sendBody("direct:test-input", TEST_JSON);
        mockStatus.assertIsSatisfied();
        Exchange exchange = mockStatus.getExchanges().get(0);
        assertNotNull(exchange.getMessage().getBody());
    }
    
    @Test
    public void testInvalidJsonHandling() throws Exception {
        MockEndpoint mockStatus = getMockEndpoint("mock:status-topic");
        mockStatus.expectedMessageCount(1);
        template.sendBody("direct:test-input", "INVALID_JSON");
        mockStatus.assertIsSatisfied();
        Exchange exchange = mockStatus.getExchanges().get(0);
        assertNotNull(exchange.getMessage().getBody());
    }

    @Test
public void testManualCommitBlockExecution() throws Exception {
    MockEndpoint mockStatus = getMockEndpoint("mock:status-topic");
    mockStatus.expectedMessageCount(1);

    // Create a mock KafkaManualCommit
    KafkaManualCommit mockManualCommit = mock(KafkaManualCommit.class);

    // Send message with KafkaManualCommit header
    template.send("direct:test-input", exchange -> {
        exchange.getIn().setBody(TEST_JSON);
        exchange.getIn().setHeader(KafkaConstants.MANUAL_COMMIT, mockManualCommit);
        exchange.getIn().setHeader(KafkaConstants.PARTITION, 0);
    });

    mockStatus.assertIsSatisfied();

    // Verify that commit() was invoked
    verify(mockManualCommit).commit();
}


}