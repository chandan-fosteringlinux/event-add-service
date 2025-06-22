
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.indigo.notification.client.NotificationClient;
import com.indigo.notification.notificationService.NotificationServiceException;
import com.indigo.notification.notificationService.NotificationServiceImpl;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void testSendNotificationSuccess() {
        // Prepare
        Map<String, Object> payload = Map.of("key", "value");
        Response response = mock(Response.class);
        when(notificationClient.sendNotification(payload)).thenReturn(response);

        // Execute
        Response result = notificationService.sendNotification(payload);

        // Verify
        assertSame(response, result);
    }

    @Test
    void testSendNotificationWebApplicationException() {
        // Prepare
        Map<String, Object> payload = Map.of("key", "value");
        WebApplicationException exception = new WebApplicationException("Server error");
        when(notificationClient.sendNotification(payload)).thenThrow(exception);

        // Execute & Verify
        NotificationServiceException ex = assertThrows(
            NotificationServiceException.class,
            () -> notificationService.sendNotification(payload)
        );
        assertEquals("Failed to call notification service", ex.getMessage());
        assertEquals(exception, ex.getCause());
    }

    @Test
    void testSendNotificationProcessingException() {
        // Prepare
        Map<String, Object> payload = Map.of("key", "value");
        ProcessingException exception = new ProcessingException("Network issue");
        when(notificationClient.sendNotification(payload)).thenThrow(exception);

        // Execute & Verify
        NotificationServiceException ex = assertThrows(
            NotificationServiceException.class,
            () -> notificationService.sendNotification(payload)
        );
        assertEquals("Failed to call notification service", ex.getMessage());
        assertEquals(exception, ex.getCause());
    }
}