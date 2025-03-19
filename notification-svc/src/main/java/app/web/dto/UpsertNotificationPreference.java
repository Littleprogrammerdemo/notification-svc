package app.web.dto;

import app.model.NotificationType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;
@Data
@Builder
public class UpsertNotificationPreference {

    @NotNull
    private UUID userId;

    private boolean notificationEnabled;

    @NotNull
    private NotificationType type;

    private String contactInfo;

    // Public constructor
    public UpsertNotificationPreference(@NotNull UUID userId, boolean notificationEnabled, @NotNull NotificationType type, String contactInfo) {
        this.userId = userId;
        this.notificationEnabled = notificationEnabled;
        this.type = type;
        this.contactInfo = contactInfo;
    }
}
