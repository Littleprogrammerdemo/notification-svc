package app.web;

import app.model.Notification;
import app.model.NotificationPreference;
import app.web.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NotificationControllerApiTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void givenUserId_whenGetNotificationHistory_thenReturnOk() {
        UUID userId = UUID.randomUUID();
        ResponseEntity<List> response = restTemplate.exchange(
                "/api/v1/notifications?userId=" + userId,
                HttpMethod.GET,
                null,
                List.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void givenUserId_whenClearNotifications_thenReturnOk() {
        UUID userId = UUID.randomUUID();
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/notifications?userId=" + userId,
                HttpMethod.DELETE,
                null,
                Void.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

}