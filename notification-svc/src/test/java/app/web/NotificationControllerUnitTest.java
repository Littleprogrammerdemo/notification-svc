package app.web;

import app.model.Notification;
import app.model.NotificationPreference;
import app.service.NotificationService;
import app.web.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class NotificationControllerUnitTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();
        objectMapper = new ObjectMapper();
    }


    @Test
    void givenUserId_whenGetUserNotificationPreference_thenReturnOk() throws Exception {
        UUID userId = UUID.randomUUID();
        NotificationPreference preference = new NotificationPreference();
        when(notificationService.getPreferenceByUserId(userId)).thenReturn(preference);

        mockMvc.perform(get("/api/v1/notifications/preferences")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void givenUserId_whenGetNotificationHistory_thenReturnOk() throws Exception {
        UUID userId = UUID.randomUUID();
        when(notificationService.getNotificationHistory(userId)).thenReturn(List.of(new Notification()));

        mockMvc.perform(get("/api/v1/notifications")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void givenUserIdAndEnabledStatus_whenChangeNotificationPreference_thenReturnOk() throws Exception {
        UUID userId = UUID.randomUUID();
        boolean enabled = true;
        NotificationPreference preference = new NotificationPreference();
        when(notificationService.changeNotificationPreference(userId, enabled)).thenReturn(preference);

        mockMvc.perform(put("/api/v1/notifications/preferences")
                        .param("userId", userId.toString())
                        .param("enabled", String.valueOf(enabled)))
                .andExpect(status().isOk());
    }

    @Test
    void givenUserId_whenClearNotifications_thenReturnOk() throws Exception {
        UUID userId = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/notifications")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).clearNotifications(userId);
    }

    @Test
    void givenUserId_whenRetryFailedNotifications_thenReturnOk() throws Exception {
        UUID userId = UUID.randomUUID();
        mockMvc.perform(put("/api/v1/notifications")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).retryFailedNotifications(userId);
    }

    @Test
    void givenLikeRequest_whenSendLikeNotification_thenReturnCreated() throws Exception {
        LikeRequest request = new LikeRequest();
        Notification notification = new Notification();
        when(notificationService.sendLikeNotification(request)).thenReturn(notification);

        mockMvc.perform(post("/api/v1/notifications/like")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void givenCommentRequest_whenSendCommentNotification_thenReturnCreated() throws Exception {
        CommentRequest request = new CommentRequest();
        Notification notification = new Notification();
        when(notificationService.sendCommentNotification(request)).thenReturn(notification);

        mockMvc.perform(post("/api/v1/notifications/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void givenFriendRequest_whenSendFriendRequestNotification_thenReturnCreated() throws Exception {
        FriendRequest request = new FriendRequest();
        Notification notification = new Notification();
        when(notificationService.sendFriendRequestNotification(request)).thenReturn(notification);

        mockMvc.perform(post("/api/v1/notifications/friend-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void givenRatingRequest_whenSendRatingNotification_thenReturnCreated() throws Exception {
        RatingRequest request = new RatingRequest();
        Notification notification = new Notification();
        when(notificationService.sendRatingNotification(request)).thenReturn(notification);

        mockMvc.perform(post("/api/v1/notifications/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}