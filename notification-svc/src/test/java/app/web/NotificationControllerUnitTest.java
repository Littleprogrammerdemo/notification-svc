package app.web;

import app.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();
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

}
