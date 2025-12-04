package com.back.domain.post.controller;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.back.config.TestConfig;

@ActiveProfiles("test")
@SpringBootTest
@Import(TestConfig.class)
public class PostControllerTest {

}
