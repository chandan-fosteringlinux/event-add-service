
import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.bean.validator.BeanValidationException;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.indigo.notification.Data.NotificationRequest;
import com.indigo.notification.Data.NotificationRequest.PushRecipient;
import com.indigo.notification.processors.NotificationProcessors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;

@ExtendWith(MockitoExtension.class)
class NotificationProcessorsTest {

    @Mock
    private Exchange exchange;

    @Mock
    private Message message;

    @InjectMocks
    private NotificationProcessors processors;

    @Captor
    private ArgumentCaptor<Object> bodyCaptor;

    @BeforeEach
    void setUp() throws Exception {
        // Set fields using reflection
        setField(processors, "appId", "test-app-id");
        setField(processors, "accountId", "test-account-id");
        setField(processors, "apiKey", "test-api-key");
        setField(processors, "vendorName", "TestVendor");
        setField(processors, "currentTopic", "status-topic");
        setField(processors, "logMessage", "Test log message");
    }

    private void setField(Object target, String fieldName, Object value) 
            throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void testOriginalRequestProcessor2() {
        // Prepare mocks for this specific test
        when(exchange.getMessage()).thenReturn(message);
        Map<String, Object> testPayload = Map.of("key1", "value1", "key2", 123);
        when(message.getBody(Map.class)).thenReturn(testPayload);

        // Execute
        assertDoesNotThrow(() -> processors.OriginalRequestProcessor2().process(exchange));

        // Verify
        verify(exchange).setProperty(eq("originalRequest2"), eq(testPayload));
    }

    @Test
    void testOriginalRequestProcessor() {
        // Prepare mocks for this specific test
        when(exchange.getIn()).thenReturn(message);
        NotificationRequest testRequest = createTestNotificationRequest();
        when(message.getBody(NotificationRequest.class)).thenReturn(testRequest);

        // Execute
        assertDoesNotThrow(() -> processors.OriginalRequestProcessor().process(exchange));

        // Verify
        verify(exchange).setProperty(eq("originalRequest"), eq(testRequest));
    }

    @Test
    void testBuildNotificationPayloadProcessor() {
        // Prepare mocks for this specific test
        when(exchange.getIn()).thenReturn(message);
        NotificationRequest testRequest = createTestNotificationRequest();
        when(exchange.getProperty("originalRequest", NotificationRequest.class))
            .thenReturn(testRequest);

        // Execute
        assertDoesNotThrow(() -> processors.buildNotificationPayloadProcessor().process(exchange));

        // Verify
        verify(message).setBody(bodyCaptor.capture());
        Map<?, ?> payload = (Map<?, ?>) bodyCaptor.getValue();

        // Verify auth section
        Map<?, ?> auth = (Map<?, ?>) payload.get("auth");
        assertEquals("test-app-id", auth.get("appId"));
        assertEquals("test-account-id", auth.get("accountId"));
        assertEquals("test-api-key", auth.get("apiKey"));

        // Verify data section
        Map<?, ?> data = (Map<?, ?>) payload.get("data");
        assertEquals("uid123", data.get("appuid"));
        assertEquals("user123", data.get("userId"));
        assertEquals("iOS", data.get("platform"));
        assertEquals("NPSSurvey", data.get("eventName"));
        assertEquals(Map.of("mobileNumber", "7889679089"), data.get("eventAttributes"));
    }

    @Test
    void testExceptionResponseProcessor() {
        // Prepare mocks for this specific test
        when(exchange.getMessage()).thenReturn(message);
        
        BeanValidationException exception = mock(BeanValidationException.class);
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        
        when(exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class))
            .thenReturn(exception);
        when(exception.getConstraintViolations()).thenReturn(Set.of(violation));
        when(violation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("fieldName");
        when(violation.getMessage()).thenReturn("must not be null");

        // Execute
        assertDoesNotThrow(() -> processors.ExceptionResponseProcessor().process(exchange));

        // Verify
        verify(message).setBody(bodyCaptor.capture());
        Map<?, ?> response = (Map<?, ?>) bodyCaptor.getValue();
        assertEquals("fieldName: must not be null", response.get("message"));
        assertEquals("fail", response.get("status"));
    }

    @Test
    void testExceptionResponseProcessorWithOtherException() {
        // Prepare mocks for this specific test
        when(exchange.getMessage()).thenReturn(message);
        
        Exception exception = new RuntimeException("Generic error");
        when(exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class))
            .thenReturn(exception);

        // Execute
        assertDoesNotThrow(() -> processors.ExceptionResponseProcessor().process(exchange));

        // Verify
        verify(message).setBody(bodyCaptor.capture());
        Map<?, ?> response = (Map<?, ?>) bodyCaptor.getValue();
        assertEquals("Validation error", response.get("message"));
        assertEquals("fail", response.get("status"));
    }

    @Test
    void testSendResponseProcessor() {
        // Prepare mocks for this specific test
        when(exchange.getIn()).thenReturn(message); // FIXED: Use getIn() instead of getMessage()
        
        Map<String, Object> originalRequest = Map.of(
            "requestId", "abc",
            "messageId", 123,
            "recipient", Map.of("userId", "user123")
        );
        when(exchange.getProperty("originalRequest2", Map.class))
            .thenReturn(originalRequest);

        // Execute
        assertDoesNotThrow(() -> processors.sendResponseProcessor().process(exchange));

        // Verify
        verify(message).setBody(bodyCaptor.capture());
        Map<?, ?> response = (Map<?, ?>) bodyCaptor.getValue();
        
        // Verify original values are preserved
        assertEquals("abc", response.get("requestId"));
        assertEquals(123, response.get("messageId"));
        
        // Verify added fields
        assertEquals("Message Posted to TestVendor", response.get("currentStage"));
        assertEquals("status-topic", response.get("currentTopic"));
        assertEquals("Test log message", response.get("log"));
        
        // Verify vendor information
        Map<?, ?> vendor = (Map<?, ?>) response.get("vendor");
        assertEquals("TestVendor", vendor.get("name"));
        
        // Verify timestamp format
        String timestamp = (String) response.get("timeStamp");
        assertNotNull(ZonedDateTime.parse(timestamp));
    }

    @Test
    void testSendResponseProcessorWithoutRecipient() {
        // Prepare mocks for this specific test
        when(exchange.getIn()).thenReturn(message); // FIXED: Use getIn() instead of getMessage()
        
        Map<String, Object> originalRequest = Map.of(
            "requestId", "abc",
            "messageId", 123
        );
        when(exchange.getProperty("originalRequest2", Map.class))
            .thenReturn(originalRequest);

        // Execute
        assertDoesNotThrow(() -> processors.sendResponseProcessor().process(exchange));

        // Verify
        verify(message).setBody(bodyCaptor.capture());
        Map<?, ?> response = (Map<?, ?>) bodyCaptor.getValue();
        
        // Verify vendor information with unknown recipient
        Map<?, ?> vendor = (Map<?, ?>) response.get("vendor");
        Map<?, ?> idMap = (Map<?, ?>) vendor.get("id");
        assertEquals("unknown", idMap.keySet().iterator().next());
    }

    @Test
    void testSendResponseProcessorWithNullOriginalRequest() {
        // Prepare mocks for this specific test
        when(exchange.getProperty("originalRequest2", Map.class)).thenReturn(null);

        // Execute & Verify
        assertThrows(IllegalStateException.class, () -> 
            processors.sendResponseProcessor().process(exchange)
        );
    }

    private NotificationRequest createTestNotificationRequest() {
        NotificationRequest request = new NotificationRequest();
        request.setTemplateName("NPSSurvey");
        
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("mobileNumber", "7889679089");
        request.setAttributes(attributes);
        
        List<PushRecipient> recipients = new ArrayList<>();
        PushRecipient recipient = new PushRecipient();
        recipient.setAppuid("uid123");
        recipient.setUserId("user123");
        recipient.setPlatform("iOS");
        recipients.add(recipient);
        request.setPushRecipients(recipients);
        
        return request;
    }
}