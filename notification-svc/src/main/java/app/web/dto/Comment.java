package app.web.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class Comment {
    private UUID userId;
    private String commenterUsername;
    private String postTitle;
    private String commentContent;
}
