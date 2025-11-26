package com.back.domain.chat.dto;

import java.time.LocalDateTime;

public record ChatRoomListDto(
        Long id,
        LocalDateTime createdAt,
        ChatPostDto post,
        OtherMemberDto otherMember,

        String lastMessage,
        LocalDateTime lastMessageTime,
        Integer unreadCount
) {
    public ChatRoomListDto withUnreadCount(Integer unreadCount, String profileImgUrl) {
        return new ChatRoomListDto(
                this.id,
                this.createdAt,
                this.post,
                new OtherMemberDto(this.otherMember.id(), this.otherMember.nickname(), profileImgUrl),
                this.lastMessage,
                this.lastMessageTime,
                unreadCount
        );
    }
}

