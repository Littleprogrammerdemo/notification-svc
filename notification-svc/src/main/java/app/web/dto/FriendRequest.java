package app.web.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class FriendRequest {
    private UUID senderId;
    private UUID receiverId;
    private String senderUsername; // The username of the person who sent the friend request
}