package app.web.dto;

import lombok.Data;

import java.util.UUID;
@Data
public class RatingRequest {
    private UUID userId;
    private String raterUsername;
    private String postTitle;
    private int ratingValue;
}
