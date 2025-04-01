package app.web.dto;

import app.model.NotificationStatus;
import app.model.NotificationType;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class NotificationResponse {

    private String subject;

    private NotificationStatus status;

    private NotificationType type;
}