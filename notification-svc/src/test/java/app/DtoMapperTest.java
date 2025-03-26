package app;

import app.model.Notification;
import app.model.NotificationPreference;
import app.model.NotificationType;
import app.web.dto.NotificationPreferenceResponse;
import app.web.dto.NotificationResponse;
import app.web.mapper.DtoMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DtoMapperTest {

    @Test
    void shouldMapNotificationTypeCorrectly() {
        // Test the simple pass-through mapping of NotificationType
        assertEquals(NotificationType.EMAIL, DtoMapper.fromNotificationTypeRequest(NotificationType.EMAIL));
    }

    @Test
    void shouldMapNotificationPreferenceToResponseCorrectly() {
        // Create a sample NotificationPreference entity
        NotificationPreference entity = new NotificationPreference();
        entity.setType(NotificationType.EMAIL);
        entity.setContactInfo("user@example.com");
        entity.setNotificationsEnabled(true);

        // Map the entity to the DTO
        NotificationPreferenceResponse response = DtoMapper.fromNotificationPreference(entity);

        // Verify the mapping
        assertEquals(entity.getId(), response.getId());
        assertEquals(entity.getUserId(), response.getUserId());
        assertEquals(entity.getType(), response.getType());
        assertEquals(entity.getContactInfo(), response.getContactInfo());
        assertEquals(entity.isNotificationsEnabled(), response.isEnabled());
    }

    @Test
    void shouldMapNotificationToResponseCorrectly() {
        // Create a sample Notification entity
        Notification entity = new Notification();
        entity.setSubject("Test Subject");
        entity.setType(NotificationType.EMAIL);

        // Map the entity to the DTO
        NotificationResponse response = DtoMapper.fromNotification(entity);

        // Verify the mapping
        assertEquals(entity.getSubject(), response.getSubject());
        assertEquals(entity.getStatus(), response.getStatus());
        assertEquals(entity.getType(), response.getType());
    }
}
