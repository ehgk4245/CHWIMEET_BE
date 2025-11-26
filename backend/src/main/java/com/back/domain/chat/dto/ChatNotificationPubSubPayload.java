package com.back.domain.chat.dto;

public record ChatNotificationPubSubPayload(
        Long memberId,
        ChatNotiDto notification
) {
}
