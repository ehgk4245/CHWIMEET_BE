package com.back.domain.chat.controller;

import com.back.config.TestConfig;
import com.back.domain.chat.dto.CreateChatRoomReqBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@Import(TestConfig.class)
@AutoConfigureMockMvc
@Transactional
@Sql("/sql/chat.sql")
class ChatControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithUserDetails(value = "user1@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("채팅방 생성 성공")
    void createChatRoom_success() throws Exception {
        // given
        CreateChatRoomReqBody reqBody = new CreateChatRoomReqBody(4L);

        // when
        ResultActions resultActions = mvc.perform(post("/api/v1/chats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqBody)))
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.msg").value("채팅방이 생성되었습니다."))
                .andExpect(jsonPath("$.data.chatRoomId").exists());
    }

    @Test
    @WithUserDetails(value = "user1@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("이미 존재하는 채팅방일 때")
    void createChatRoom_alreadyExists() throws Exception {
        // given
        CreateChatRoomReqBody reqBody = new CreateChatRoomReqBody(1L);

        // when
        ResultActions resultActions = mvc.perform(post("/api/v1/chats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqBody)))
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.msg").value("이미 존재하는 채팅방입니다."))
                .andExpect(jsonPath("$.data.chatRoomId").exists());
    }

    @Test
    @DisplayName("로그인 안 한 상태에서 채팅방 생성 시도")
    void createChatRoom_unauthorized() throws Exception {
        // given
        CreateChatRoomReqBody reqBody = new CreateChatRoomReqBody(1L);

        // when
        ResultActions resultActions = mvc.perform(post("/api/v1/chats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqBody)))
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails(value = "user2@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("본인과 채팅방 생성 시도 - 예외 발생")
    void createChatRoom_withSelf_shouldThrow() throws Exception {
        // given
        CreateChatRoomReqBody reqBody = new CreateChatRoomReqBody(1L);

        // when
        ResultActions resultActions = mvc.perform(post("/api/v1/chats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqBody)))
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.msg").value("본인과 채팅방을 만들 수 없습니다."));
    }

    @Test
    @WithUserDetails(value = "user1@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("채팅방 목록 조회 - 검색어 없음")
    void getMyChatRooms_withoutKeyword() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(get("/api/v1/chats")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.msg").value("내 채팅방 목록"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(3))
                .andExpect(jsonPath("$.data.page.page").value(0))
                .andExpect(jsonPath("$.data.page.size").value(10))
                .andExpect(jsonPath("$.data.page.totalElements").value(3))
                .andExpect(jsonPath("$.data.page.totalPages").value(1));
    }

    @Test
    @WithUserDetails(value = "user1@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("채팅방 목록 조회 - 게시글 제목으로 검색 (텐트)")
    void getMyChatRooms_searchByPostTitle_tent() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(get("/api/v1/chats")
                        .param("page", "0")
                        .param("size", "10")
                        .param("keyword", "텐트"))
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.msg").value("내 채팅방 목록"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].post.title").value("캠핑 텐트 대여"))
                .andExpect(jsonPath("$.data.content[0].otherMember.nickname").value("kim"))
                .andExpect(jsonPath("$.data.page.totalElements").value(1));
    }

    @Test
    @WithUserDetails(value = "user1@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("채팅방 목록 조회 - 상대방 닉네임으로 검색")
    void getMyChatRooms_searchByMemberNickname() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(get("/api/v1/chats")
                        .param("page", "0")
                        .param("size", "10")
                        .param("keyword", "kim"))
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.msg").value("내 채팅방 목록"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].otherMember.nickname").value("kim"))
                .andExpect(jsonPath("$.data.content[0].post.title").value("캠핑 텐트 대여"))
                .andExpect(jsonPath("$.data.page.totalElements").value(1));
    }

    @Test
    @WithUserDetails(value = "user1@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("채팅방 목록 조회 - 페이징 테스트")
    void getMyChatRooms_pagination() throws Exception {
        // 첫 페이지 (size=2)
        ResultActions resultActions = mvc.perform(get("/api/v1/chats")
                        .param("page", "0")
                        .param("size", "2"))
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.page.page").value(0))
                .andExpect(jsonPath("$.data.page.size").value(2))
                .andExpect(jsonPath("$.data.page.totalElements").value(3))
                .andExpect(jsonPath("$.data.page.totalPages").value(2))
                .andExpect(jsonPath("$.data.page.first").value(true))
                .andExpect(jsonPath("$.data.page.last").value(false))
                .andExpect(jsonPath("$.data.page.hasNext").value(true));

        // 두 번째 페이지
        ResultActions resultActions2 = mvc.perform(get("/api/v1/chats")
                        .param("page", "1")
                        .param("size", "2"))
                .andDo(print());

        // then
        resultActions2
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.page.page").value(1))
                .andExpect(jsonPath("$.data.page.totalElements").value(3))
                .andExpect(jsonPath("$.data.page.first").value(false))
                .andExpect(jsonPath("$.data.page.last").value(true))
                .andExpect(jsonPath("$.data.page.hasNext").value(false));
    }

    @Test
    @WithUserDetails(value = "user1@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("채팅방 상세 정보 조회 성공")
    void getChatRoom_success() throws Exception {
        // given
        Long chatRoomId = 1L;
        Long otherMemberId = 2L;

        // when
        ResultActions resultActions = mvc.perform(get("/api/v1/chats/{chatRoomId}", chatRoomId))
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.msg").value("채팅방 정보"))
                .andExpect(jsonPath("$.data.id").value(chatRoomId))
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.post.title").value("캠핑 텐트 대여"))
                .andExpect(jsonPath("$.data.otherMember.id").value(otherMemberId))
                .andExpect(jsonPath("$.data.otherMember.nickname").value("kim"));
    }

    @Test
    @WithUserDetails(value = "user1@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("존재하지 않는 채팅방 조회 시도")
    void getChatRoom_notFound() throws Exception {
        // given
        Long nonExistentChatRoomId = 99999L;

        // when
        ResultActions resultActions = mvc.perform(get("/api/v1/chats/{chatRoomId}", nonExistentChatRoomId))
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 채팅방입니다."));
    }

    @Test
    @WithUserDetails(value = "user2@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("권한 없는 채팅방 조회 시도 - member2가 member1-member3 채팅방 접근")
    void getChatRoom_forbidden() throws Exception {
        // given
        Long chatRoomId = 2L;

        // when
        ResultActions resultActions = mvc.perform(get("/api/v1/chats/{chatRoomId}", chatRoomId))
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.msg").value("해당 채팅방에 접근할 수 없습니다."));
    }

}