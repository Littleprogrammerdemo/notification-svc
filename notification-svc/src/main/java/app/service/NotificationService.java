package app.service;

import app.model.Notification;
import app.model.NotificationPreference;
import app.model.NotificationStatus;
import app.model.NotificationType;
import app.repository.NotificationPreferenceRepository;
import app.repository.NotificationRepository;
import app.web.dto.*;
import app.web.mapper.DtoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class NotificationService {

    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationRepository notificationRepository;
    private final MailSender mailSender;

    @Autowired
    public NotificationService(NotificationPreferenceRepository preferenceRepository, NotificationRepository notificationRepository, MailSender mailSender) {
        this.preferenceRepository = preferenceRepository;
        this.notificationRepository = notificationRepository;
        this.mailSender = mailSender;
    }


    public NotificationPreference upsertPreference(UpsertNotificationPreference dto) {

        Optional<NotificationPreference> userNotificationPreferenceOptional = preferenceRepository.findByUserId(dto.getUserId());

        // If exists - update it
        if (userNotificationPreferenceOptional.isPresent()) {
            NotificationPreference preference = userNotificationPreferenceOptional.get();
            preference.setContactInfo(dto.getContactInfo());
            preference.setNotificationsEnabled(dto.isNotificationEnabled());
            preference.setType(DtoMapper.fromNotificationTypeRequest(dto.getType()));
            preference.setUpdatedOn(LocalDateTime.now());
            return preferenceRepository.save(preference);
        }
        // If not exist - create it
        NotificationPreference notificationPreference = NotificationPreference.builder()
                .userId(dto.getUserId())
                .type(DtoMapper.fromNotificationTypeRequest(dto.getType()))
                .notificationsEnabled(dto.isNotificationEnabled())
                .contactInfo(dto.getContactInfo())
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        return preferenceRepository.save(notificationPreference);
    }

    public NotificationPreference getPreferenceByUserId(UUID userId) {

        return preferenceRepository.findByUserId(userId).orElseThrow(() -> new NullPointerException("Notification preference for user id %s was not found.".formatted(userId)));
    }

    public Notification sendNotification(NotificationRequest notificationRequest) {

        UUID userId = notificationRequest.getUserId();
        NotificationPreference userPreference = getPreferenceByUserId(userId);

        if (!userPreference.isNotificationsEnabled()) {
            throw new IllegalArgumentException("User with id %s does not allow to receive notifications.".formatted(userId));
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(userPreference.getContactInfo());
        message.setSubject(notificationRequest.getSubject());
        message.setText(notificationRequest.getBody());

        Notification notification = Notification.builder()
                .subject(notificationRequest.getSubject())
                .body(notificationRequest.getBody())
                .userId(userId)
                .isDeleted(false)
                .type(NotificationType.EMAIL)
                .build();

        try {
            mailSender.send(message);
            notification.setStatus(NotificationStatus.RECEIVED);
        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            log.warn("There was an issue sending an email to %s due to %s.".formatted(userPreference.getContactInfo(), e.getMessage()));
        }

        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationHistory(UUID userId) {

        return notificationRepository.findAllByUserIdAndDeletedIsFalse(userId);
    }

    public NotificationPreference changeNotificationPreference(UUID userId, boolean notificationsEnabled) {
        NotificationPreference notificationPreference = getPreferenceByUserId(userId);
        notificationPreference.setNotificationsEnabled(notificationsEnabled);
        return preferenceRepository.save(notificationPreference);
    }

    public void clearNotifications(UUID userId) {

        List<Notification> notifications = getNotificationHistory(userId);

        notifications.forEach(notification -> {
            notification.setDeleted(true);
            notificationRepository.save(notification);
        });
    }

    public void retryFailedNotifications(UUID userId) {

        NotificationPreference userPreference = getPreferenceByUserId(userId);
        if (!userPreference.isNotificationsEnabled()) {
            throw new IllegalArgumentException("User with id %s does not allow to receive notifications.".formatted(userId));
        }

        List<Notification> failedNotifications = notificationRepository.findAllByUserIdAndStatus(userId, NotificationStatus.FAILED);
        failedNotifications = failedNotifications.stream().filter(notification ->  !notification.isDeleted()).toList();

        for (Notification notification : failedNotifications) {

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(userPreference.getContactInfo());
            message.setSubject(notification.getSubject());
            message.setText(notification.getBody());

            try {
                mailSender.send(message);
                notification.setStatus(NotificationStatus.RECEIVED);
            } catch (Exception e) {
                notification.setStatus(NotificationStatus.FAILED);
                log.warn("There was an issue sending an email to %s due to %s.".formatted(userPreference.getContactInfo(), e.getMessage()));
            }

            notificationRepository.save(notification);
        }
    }
    public Notification sendLikeNotification(LikeRequest likeRequestDTO) {
        UUID userId = likeRequestDTO.getUserId();
        NotificationPreference userPreference = getPreferenceByUserId(userId);

        if (!userPreference.isNotificationsEnabled()) {
            throw new IllegalArgumentException("User with id %s does not allow to receive notifications.".formatted(userId));
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(userPreference.getContactInfo());
        message.setSubject("New Like on your Post");
        message.setText("User %s liked your post: %s".formatted(likeRequestDTO.getLikerUsername(), likeRequestDTO.getPostTitle()));

        Notification notification = Notification.builder()
                .subject("New Like")
                .body("User %s liked your post: %s".formatted(likeRequestDTO.getLikerUsername(), likeRequestDTO.getPostTitle()))
                .userId(userId)
                .isDeleted(false)
                .type(NotificationType.EMAIL)
                .build();

        try {
            mailSender.send(message);
            notification.setStatus(NotificationStatus.RECEIVED);
        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            log.warn("There was an issue sending a like notification to %s due to %s.".formatted(userPreference.getContactInfo(), e.getMessage()));
        }

        return notificationRepository.save(notification);
    }

    public Notification sendCommentNotification(CommentRequest commentRequestDTO) {
        UUID userId = commentRequestDTO.getUserId();
        NotificationPreference userPreference = getPreferenceByUserId(userId);

        if (!userPreference.isNotificationsEnabled()) {
            throw new IllegalArgumentException("User with id %s does not allow to receive notifications.".formatted(userId));
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(userPreference.getContactInfo());
        message.setSubject("New Comment on your Post");
        message.setText("User %s commented on your post: %s".formatted(commentRequestDTO.getCommenterUsername(), commentRequestDTO.getPostTitle()));

        Notification notification = Notification.builder()
                .subject("New Comment")
                .body("User %s commented on your post: %s".formatted(commentRequestDTO.getCommenterUsername(), commentRequestDTO.getPostTitle()))
                .userId(userId)
                .isDeleted(false)
                .type(NotificationType.EMAIL)
                .build();

        try {
            mailSender.send(message);
            notification.setStatus(NotificationStatus.RECEIVED);
        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            log.warn("There was an issue sending a comment notification to %s due to %s.".formatted(userPreference.getContactInfo(), e.getMessage()));
        }

        return notificationRepository.save(notification);
    }

    public Notification sendFriendRequestNotification(FriendRequest friendRequestDTO) {
        UUID userId = friendRequestDTO.getReceiverId();
        NotificationPreference userPreference = getPreferenceByUserId(userId);

        if (!userPreference.isNotificationsEnabled()) {
            throw new IllegalArgumentException("User with id %s does not allow to receive notifications.".formatted(userId));
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(userPreference.getContactInfo());
        message.setSubject("New Friend Request");
        message.setText("User %s sent you a friend request.".formatted(friendRequestDTO.getSenderUsername()));

        Notification notification = Notification.builder()
                .subject("New Friend Request")
                .body("User %s sent you a friend request.".formatted(friendRequestDTO.getSenderUsername()))
                .userId(userId)
                .isDeleted(false)
                .type(NotificationType.EMAIL)
                .build();

        try {
            mailSender.send(message);
            notification.setStatus(NotificationStatus.RECEIVED);
        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            log.warn("There was an issue sending a friend request notification to %s due to %s.".formatted(userPreference.getContactInfo(), e.getMessage()));
        }

        return notificationRepository.save(notification);
    }
    public Notification sendRatingNotification(RatingRequest ratingDTO) {
        UUID userId = ratingDTO.getUserId();
        NotificationPreference userPreference = getPreferenceByUserId(userId);

        if (!userPreference.isNotificationsEnabled()) {
            throw new IllegalArgumentException("User with id %s does not allow to receive notifications.".formatted(userId));
        }

        // Construct the email message
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(userPreference.getContactInfo());
        message.setSubject("New Rating on your Post");
        message.setText("User %s rated your post '%s' with a rating of %d.".formatted(ratingDTO.getRaterUsername(), ratingDTO.getPostTitle(), ratingDTO.getRatingValue()));

        // Create the notification object
        Notification notification = Notification.builder()
                .subject("New Rating")
                .body("User %s rated your post '%s' with a rating of %d.".formatted(ratingDTO.getRaterUsername(), ratingDTO.getPostTitle(), ratingDTO.getRatingValue()))
                .userId(userId)
                .isDeleted(false)
                .type(NotificationType.EMAIL)
                .build();

        // Try sending the email
        try {
            mailSender.send(message);
            notification.setStatus(NotificationStatus.RECEIVED);
        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            log.warn("There was an issue sending a rating notification to %s due to %s.".formatted(userPreference.getContactInfo(), e.getMessage()));
        }

        return notificationRepository.save(notification);
    }
}