package com.back.domain.chat.chat.controller;

import com.back.domain.category.category.entity.Category;
import com.back.domain.category.category.repository.CategoryRepository;
import com.back.domain.chat.chat.dto.CreateChatRoomReqBody;
import com.back.domain.chat.chat.repository.ChatRoomRepository;
import com.back.domain.chat.chat.service.ChatService;
import com.back.domain.member.member.common.MemberRole;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.post.common.ReceiveMethod;
import com.back.domain.post.common.ReturnMethod;
import com.back.domain.post.entity.Post;
import com.back.domain.post.repository.PostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ChatControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ChatService chatService;

    private Post post;

    @BeforeEach
    void setUp() {
        chatRoomRepository.deleteAll();
        postRepository.deleteAll();
        memberRepository.deleteAll();

         memberRepository.save(Member.builder()
                .email("user1@test.com")
                .password("1234")
                .name("홍길동")
                .phoneNumber("010-1111-1111")
                .address1("서울시 강남구")
                .address2("테헤란로 123")
                .nickname("hong")
                .isBanned(false)
                .role(MemberRole.USER)
                .profileImgUrl(null)
                .build()
        );

        Member member2 = memberRepository.save(Member.builder()
                .email("user2@test.com")
                .password("1234")
                .name("김철수")
                .phoneNumber("010-2222-2222")
                .address1("서울시 서초구")
                .address2("서초대로 456")
                .nickname("kim")
                .isBanned(false)
                .role(MemberRole.USER)
                .profileImgUrl(null)
                .build()
        );

        Category category = categoryRepository.save(Category.builder()
                .name("노트북")
                .parent(null)
                .build());

        post = postRepository.save(Post.builder()
                .title("테스트 게시글 1")
                .content("테스트용 게시글 내용입니다.")
                .receiveMethod(ReceiveMethod.DELIVERY)
                .returnMethod(ReturnMethod.DELIVERY)
                .deposit(10000)
                .fee(5000)
                .author(member2)
                .category(category)
                .build()
        );
    }

    @Test
    @WithUserDetails(value = "user1@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("채팅방 생성 성공")
    void test1_createChatRoom_success() throws Exception {
        // given
        Long postId = post.getId();
        CreateChatRoomReqBody reqBody = new CreateChatRoomReqBody(postId);

        // when
        ResultActions resultActions = mvc.perform(post("/api/v1/chats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqBody)))
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("채팅방이 생성되었습니다."))
                .andExpect(jsonPath("$.chatRoomId").exists());
    }

    @Test
    @WithUserDetails(value = "user1@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("이미 존재하는 채팅방일 때")
    void test2_createChatRoom_alreadyExists() throws Exception {
        // given
        Long postId = post.getId();
        CreateChatRoomReqBody reqBody = new CreateChatRoomReqBody(postId);

        // 먼저 채팅방 생성
        chatService.createChatRoom(postId, memberRepository.findByEmail("user1@test.com").get().getId());

        // when
        ResultActions resultActions = mvc.perform(post("/api/v1/chats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqBody)))
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("이미 존재하는 채팅방입니다."))
                .andExpect(jsonPath("$.chatRoomId").exists());
    }

    @Test
    @DisplayName("로그인 안 한 상태에서 채팅방 생성 시도")
    void test3_createChatRoom_unauthorized() throws Exception {
        // given
        Long postId = post.getId();
        CreateChatRoomReqBody reqBody = new CreateChatRoomReqBody(postId);

        // when
        ResultActions resultActions = mvc.perform(post("/api/v1/chats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqBody)))
                .andDo(print());

        // then
        resultActions
                .andExpect(status().isUnauthorized());
    }
}
