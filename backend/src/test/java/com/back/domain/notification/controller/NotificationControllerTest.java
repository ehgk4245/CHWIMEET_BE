package com.back.domain.notification.controller;

import com.back.config.TestConfig;
import com.back.domain.notification.entity.Notification;
import com.back.domain.notification.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@Import(TestConfig.class)
@AutoConfigureMockMvc
@Sql({
        "/sql/categories.sql",
        "/sql/regions.sql",
        "/sql/members.sql",
        "/sql/posts.sql",
        "/sql/reservations.sql",
        "/sql/reviews.sql",
        "/sql/notifications.sql"
})
@Sql(scripts = "/sql/clean-up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class NotificationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    NotificationRepository notificationRepository;

    @Test
    @DisplayName("SSE 구독 연결 - 상태 코드 및 ContentType 확인")
    @WithUserDetails(value = "user1@example.com")
    void subscribe_shouldReturnSseEmitter() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/subscribe"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));
    }

    @Test
    @DisplayName("알림 목록 조회 - 성공")
    @WithUserDetails(value = "user1@example.com")
    void readNotifications_shouldReturnNotificationList() throws Exception {
        mockMvc.perform(get("/api/v1/notifications")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.msg").value("알림 목록 조회"))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("읽지 않은 알림 존재 여부 조회")
    @WithUserDetails(value = "user1@example.com")
    void hasUnread_shouldReturnUnreadFlag() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/unread"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.msg").value("읽지 않은 알림 존재 여부"))
                .andExpect(jsonPath("$.data.hasUnread").isBoolean());
    }

    @Test
    @DisplayName("모든 알림 읽음 처리")
    @WithUserDetails(value = "user1@example.com")
    void updateAllToRead_shouldMarkAllAsRead() throws Exception {

        List<Notification> before = notificationRepository.findByMemberId(1L);
        assertThat(before).extracting("isRead").containsExactly(false, false, true, false, false, false);

        mockMvc.perform(post("/api/v1/notifications"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        List<Notification> after = notificationRepository.findByMemberId(1L);
        assertThat(after).extracting("isRead").containsExactly(true, true, true, true, true, true);
    }

    @Test
    @DisplayName("특정 알림 읽음 처리")
    @WithUserDetails(value = "user1@example.com")
    void updateToRead_shouldMarkSingleNotificationAsRead() throws Exception {
        Notification n2 = notificationRepository.findById(2L).orElseThrow();
        assertThat(n2.getIsRead()).isFalse();

        mockMvc.perform(post("/api/v1/notifications/2"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        n2 = notificationRepository.findById(2L).orElseThrow();
        assertThat(n2.getIsRead()).isTrue();
    }
}
