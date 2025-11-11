package com.back.domain.post.post.controller;

import com.back.domain.post.post.common.ReceiveMethod;
import com.back.domain.post.post.common.ReturnMethod;
import com.back.domain.post.post.dto.PostCreateReqBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("게시글 등록 실패")
    void createPost_fail() throws Exception {
        //given
        PostCreateReqBody reqBody = PostCreateReqBody.builder()
                .title("") // 제목 누락
                .content("맥북입니다.")
                .receiveMethod(ReceiveMethod.DIRECT)
                .returnMethod(ReturnMethod.DIRECT)
                .regionIds(List.of(1L))
                .categoryId(1L)
                .deposit(10000)
                .fee(3000)
                .options(List.of())
                .images(List.of())
                .build();

        //when && then
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").exists());


    }

}