package com.back.domain.chat.dto;

public record ChatMessagePubSubPayload(
        Long chatRoomId,
        ChatMessageDto message
) {
}
