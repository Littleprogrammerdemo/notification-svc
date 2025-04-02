package app.web.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
public class LikeRequest {
    private UUID userId; // The user who receives the notification
    private String likerUsername;
    private String postTitle;
}
