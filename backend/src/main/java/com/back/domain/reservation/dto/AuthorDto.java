package com.back.domain.reservation.dto;

import com.back.domain.member.entity.Member;

public record AuthorDto(
        Long id,
        String nickname,
        String profileImgUrl
) {
    public AuthorDto(Member member) {
        this (
                member.getId(),
                member.getNickname(),
                member.getProfileImgUrl()
        );
    }
}
