
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

import com.indigo.notification.notificationService.NotificationServiceException;

class NotificationServiceExceptionTest {

    @Test
    void testExceptionMessages() {
        // Simple message
        NotificationServiceException ex1 = new NotificationServiceException("Error");
        assertEquals("Error", ex1.getMessage());
        assertNull(ex1.getCause());

        // Message with cause
        Throwable cause = new RuntimeException("Root cause");
        NotificationServiceException ex2 = new NotificationServiceException("Error", cause);
        assertEquals("Error", ex2.getMessage());
        assertEquals(cause, ex2.getCause());
    }
}