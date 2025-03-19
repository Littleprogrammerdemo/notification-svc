package app.web.mapper;

import app.model.Notification;
import app.model.NotificationPreference;
import app.model.NotificationType;
import app.web.dto.NotificationPreferenceResponse;
import app.web.dto.NotificationResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public static NotificationType fromNotificationTypeRequest(NotificationType dto) {

        return switch (dto) {
            case EMAIL -> NotificationType.EMAIL;
        };
    }

    public static NotificationPreferenceResponse fromNotificationPreference(NotificationPreference entity) {

        return NotificationPreferenceResponse.builder()
                .id(entity.getId())
                .type(entity.getType())
                .contactInfo(entity.getContactInfo())
                .enabled(entity.isNotificationsEnabled())
                .userId(entity.getUserId())
                .build();
    }

    public static NotificationResponse fromNotification(Notification entity) {

        return NotificationResponse.builder()
                .subject(entity.getSubject())
                .status(entity.getStatus())
                .type(entity.getType())
                .build();
    }
}
