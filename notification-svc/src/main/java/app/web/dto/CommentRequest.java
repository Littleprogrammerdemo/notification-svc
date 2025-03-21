package app.web.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CommentRequest {
    private UUID userId;
    private String commenterUsername;
    private String postTitle;
    private String commentContent;
}
