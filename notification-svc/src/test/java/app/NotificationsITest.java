package app;

import app.model.NotificationPreference;
import app.model.NotificationType;
import app.repository.NotificationPreferenceRepository;
import app.service.NotificationService;
import app.web.dto.UpsertNotificationPreference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class NotificationsITest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationPreferenceRepository preferenceRepository;

    @Test
    void createNewNotificationPreference_happyPath() {

        // Given
        UUID userId = UUID.randomUUID();
        UpsertNotificationPreference notificationPreference = UpsertNotificationPreference.builder()
                .userId(userId)
                .type(NotificationType.EMAIL)
                .notificationEnabled(true)
                .contactInfo("test@email.com")
                .build();

        // When
        notificationService.upsertPreference(notificationPreference);

        // Then
        List<NotificationPreference> preferences = preferenceRepository.findAll();
        assertThat(preferences).hasSize(1);
        NotificationPreference preference = preferences.get(0);
        assertEquals(userId, preference.getUserId());
        assertEquals(NotificationType.EMAIL, preference.getType());
        assertTrue(preference.isNotificationsEnabled());
        assertEquals("test@email.com", preference.getContactInfo());
    }

    @Test
    void createNewNotificationPreference_whenInvalidContactInfo_thenThrowError() {

        // Given
        UUID userId = UUID.randomUUID();
        UpsertNotificationPreference invalidNotificationPreference = UpsertNotificationPreference.builder()
                .userId(userId)
                .type(NotificationType.EMAIL)
                .notificationEnabled(true)
                .contactInfo("")  // Invalid empty contact info
                .build();

        // When / Then
        try {
            notificationService.upsertPreference(invalidNotificationPreference);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Contact information is invalid");
        }
    }

    @Test
    void updateNotificationPreference_happyPath() {

        // Given
        UUID userId = UUID.randomUUID();
        UpsertNotificationPreference notificationPreference = UpsertNotificationPreference.builder()
                .userId(userId)
                .type(NotificationType.EMAIL)
                .notificationEnabled(true)
                .contactInfo("test@email.com")
                .build();

        // Create initial preference
        notificationService.upsertPreference(notificationPreference);

        // Update the preference (with a valid change)
        UpsertNotificationPreference updatedPreference = UpsertNotificationPreference.builder()
                .userId(userId)
                .type(NotificationType.EMAIL)  // Still using EMAIL type
                .notificationEnabled(false)  // Disable notifications
                .contactInfo("test@newemail.com") // New contact info
                .build();

        // When
        notificationService.upsertPreference(updatedPreference);

        // Then
        List<NotificationPreference> preferences = preferenceRepository.findAll();
        assertThat(preferences).hasSize(1);
        NotificationPreference preference = preferences.get(0);
        assertEquals(userId, preference.getUserId());
        assertEquals(NotificationType.EMAIL, preference.getType());
        assertFalse(preference.isNotificationsEnabled());
        assertEquals("test@newemail.com", preference.getContactInfo());
    }

    @Test
    void createMultipleNotificationPreferences_happyPath() {

        // Given
        UUID userId1 = UUID.randomUUID();
        UpsertNotificationPreference notificationPreference1 = UpsertNotificationPreference.builder()
                .userId(userId1)
                .type(NotificationType.EMAIL)
                .notificationEnabled(true)
                .contactInfo("user1@email.com")
                .build();

        UUID userId2 = UUID.randomUUID();
        UpsertNotificationPreference notificationPreference2 = UpsertNotificationPreference.builder()
                .userId(userId2)
                .type(NotificationType.EMAIL) // Keep using email since you said it's the only type available
                .notificationEnabled(true)
                .contactInfo("user2@email.com")
                .build();

        // When
        notificationService.upsertPreference(notificationPreference1);
        notificationService.upsertPreference(notificationPreference2);

        // Then
        List<NotificationPreference> preferences = preferenceRepository.findAll();
        assertThat(preferences).hasSize(2);

        // Check the first user
        NotificationPreference preference1 = preferences.get(0);
        assertEquals(userId1, preference1.getUserId());
        assertEquals(NotificationType.EMAIL, preference1.getType());

        // Check the second user
        NotificationPreference preference2 = preferences.get(1);
        assertEquals(userId2, preference2.getUserId());
        assertEquals(NotificationType.EMAIL, preference2.getType()); // Keep checking for email
    }

}