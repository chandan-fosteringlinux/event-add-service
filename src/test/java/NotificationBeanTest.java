
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.indigo.notification.notificationService.NotificationBean;
import com.indigo.notification.notificationService.NotificationService;
import com.indigo.notification.notificationService.NotificationServiceException;

import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
class NotificationBeanTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private Exchange exchange;

    @Mock
    private Message inMessage;

    @Mock
    private Message outMessage;

    @InjectMocks
    private NotificationBean notificationBean;

    @Test
    void testSendNotificationBeanSuccess() {
        // Prepare
        Map<String, Object> payload = Map.of("key", "value");
        Response response = mock(Response.class);
        
        when(exchange.getIn()).thenReturn(inMessage);
        when(inMessage.getBody(Map.class)).thenReturn(payload);
        when(notificationService.sendNotification(payload)).thenReturn(response);
        when(response.getStatus()).thenReturn(200);
        when(response.readEntity(String.class)).thenReturn("Success");
        when(exchange.getMessage()).thenReturn(outMessage);

        // Execute
        assertDoesNotThrow(() -> notificationBean.SendNotificationBean(exchange));

        // Verify
        verify(outMessage).setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
        verify(outMessage).setBody("Success");
    }

@Test
void testSendNotificationBeanNon200Response() {
    // Prepare
    Map<String, Object> payload = Map.of("key", "value");
    Response response = mock(Response.class);
    
    when(exchange.getIn()).thenReturn(inMessage);
    when(inMessage.getBody(Map.class)).thenReturn(payload);
    when(notificationService.sendNotification(payload)).thenReturn(response);
    when(response.getStatus()).thenReturn(500);
    when(response.readEntity(String.class)).thenReturn("Error");
    when(exchange.getMessage()).thenReturn(outMessage);

    // Execute & Verify
    NotificationServiceException exception = assertThrows(
        NotificationServiceException.class,
        () -> notificationBean.SendNotificationBean(exchange)
    );
    // Verify the original message is preserved
    assertEquals("Downstream service error. HTTP Status: 500", exception.getMessage());
}

    @Test
    void testSendNotificationBeanException() {
        // Prepare
        Map<String, Object> payload = Map.of("key", "value");
        RuntimeException cause = new RuntimeException("Network error");
        
        when(exchange.getIn()).thenReturn(inMessage);
        when(inMessage.getBody(Map.class)).thenReturn(payload);
        when(notificationService.sendNotification(payload)).thenThrow(cause);

        // Execute & Verify
        NotificationServiceException exception = assertThrows(
            NotificationServiceException.class,
            () -> notificationBean.SendNotificationBean(exchange)
        );
        assertEquals("Failed to process notification", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}