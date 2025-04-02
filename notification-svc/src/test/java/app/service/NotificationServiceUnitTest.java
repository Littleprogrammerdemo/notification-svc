package app.service;

import app.model.Notification;
import app.model.NotificationPreference;
import app.model.NotificationStatus;
import app.repository.NotificationPreferenceRepository;
import app.repository.NotificationRepository;
import app.web.dto.NotificationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceUnitTest {

    @Mock
    private NotificationPreferenceRepository preferenceRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private MailSender mailSender;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void givenNotExistingNotificationPreference_whenChangeNotificationPreference_thenThrowNotificationPreferenceNullPointerException(){

        // Given
        UUID userId = UUID.randomUUID();
        boolean isNotificationEnabled = true;
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NullPointerException.class, () -> notificationService.changeNotificationPreference(userId, isNotificationEnabled));
    }

    @Test
    void givenExistingNotificationPreference_whenChangeNotificationPreference_thenNotificationPreferenceUpdatedSuccessfully(){

        // Given
        UUID userId = UUID.randomUUID();
        NotificationPreference preference = NotificationPreference.builder()
                .notificationsEnabled(false)
                .build();
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(preference));

        // When
        notificationService.changeNotificationPreference(userId, true);

        // Then
        assertTrue(preference.isNotificationsEnabled(), "Notification preference should be enabled.");
        verify(preferenceRepository, times(1)).save(preference);
    }

    @Test
    void givenNotExistingNotificationPreference_whenGetPreferenceByUserId_thenThrowException() {
        // Given
        UUID userId = UUID.randomUUID();
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NullPointerException.class, () -> notificationService.getPreferenceByUserId(userId));
    }
    @Test
    void givenExistingNotificationPreference_whenGetPreferenceByUserId_thenReturnPreference() {
        // Given
        UUID userId = UUID.randomUUID();
        NotificationPreference preference = NotificationPreference.builder()
                .userId(userId)
                .notificationsEnabled(true)
                .contactInfo("user@example.com")
                .build();
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(preference));

        // When
        NotificationPreference result = notificationService.getPreferenceByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
    }
    @Test
    void givenNotificationPreferenceDisabled_whenSendNotification_thenThrowException() {
        // Given
        UUID userId = UUID.randomUUID();
        NotificationPreference preference = NotificationPreference.builder()
                .userId(userId)
                .notificationsEnabled(false)
                .contactInfo("user@example.com")
                .build();
        NotificationRequest request = NotificationRequest.builder()
                .userId(userId)
                .subject("Test Subject")
                .body("Test Body")
                .build();
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(preference));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> notificationService.sendNotification(request));
    }

    @Test
    void givenNotifications_whenGetNotificationHistory_thenReturnHistory() {
        // Given
        UUID userId = UUID.randomUUID();
        Notification notification = Notification.builder()
                .userId(userId)
                .status(NotificationStatus.RECEIVED)
                .build();
        when(notificationRepository.findAllByUserIdAndDeletedIsFalse(userId)).thenReturn(List.of(notification));

        // When
        List<Notification> result = notificationService.getNotificationHistory(userId);

        // Then
        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUserId());
    }
    @Test
    void givenExistingPreference_whenChangeNotificationPreference_thenUpdatePreference() {
        // Given
        UUID userId = UUID.randomUUID();
        NotificationPreference preference = NotificationPreference.builder()
                .userId(userId)
                .notificationsEnabled(false)
                .build();
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(preference));

        // When
        notificationService.changeNotificationPreference(userId, true);

        // Then
        assertTrue(preference.isNotificationsEnabled());
        verify(preferenceRepository, times(1)).save(preference);
    }
    @Test
    void givenNotifications_whenClearNotifications_thenSetAllAsDeleted() {
        // Given
        UUID userId = UUID.randomUUID();
        Notification notification = Notification.builder()
                .userId(userId)
                .status(NotificationStatus.RECEIVED)
                .isDeleted(false)
                .build();
        when(notificationRepository.findAllByUserIdAndDeletedIsFalse(userId)).thenReturn(List.of(notification));

        // When
        notificationService.clearNotifications(userId);

        // Then
        assertTrue(notification.isDeleted());
        verify(notificationRepository, times(1)).save(notification);
    }
    @Test
    void givenFailedNotifications_whenRetryFailedNotifications_thenRetrySuccessfully() {
        // Given
        UUID userId = UUID.randomUUID();
        NotificationPreference preference = NotificationPreference.builder()
                .userId(userId)
                .notificationsEnabled(true)
                .contactInfo("user@example.com")
                .build();
        Notification failedNotification = Notification.builder()
                .userId(userId)
                .status(NotificationStatus.FAILED)
                .subject("Test Subject")
                .body("Test Body")
                .build();
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(preference));
        when(notificationRepository.findAllByUserIdAndStatus(userId, NotificationStatus.FAILED))
                .thenReturn(List.of(failedNotification));

        // Mock mailSender
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When
        notificationService.retryFailedNotifications(userId);

        // Then
        assertEquals(NotificationStatus.RECEIVED, failedNotification.getStatus());
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
        verify(notificationRepository, times(1)).save(failedNotification);
    }
}