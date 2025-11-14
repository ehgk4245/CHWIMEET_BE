package com.back.domain.chat.controller;

import com.back.domain.chat.dto.ChatMessageDto;
import com.back.domain.chat.dto.SendChatMessageDto;
import com.back.domain.chat.service.ChatService;
import com.back.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
    private final ChatService chatService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/chat/{chatRoomId}")
    public void sendMessage(
            @DestinationVariable Long chatRoomId,
            @Payload SendChatMessageDto body,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        ChatMessageDto chatMessageDto = chatService.saveMessage(chatRoomId, body, securityUser.getId());

        simpMessagingTemplate.convertAndSend("/sub/chat/" + chatRoomId, chatMessageDto);
    }
}