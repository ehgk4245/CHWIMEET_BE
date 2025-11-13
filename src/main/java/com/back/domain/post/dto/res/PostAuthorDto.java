package com.back.domain.post.dto.res;

import com.back.domain.member.member.entity.Member;
import lombok.Builder;

@Builder
public record PostAuthorDto(
        Long id,
        String nickname,
        String profileImgUrl
) {
    public static PostAuthorDto from(Member member) {
        return PostAuthorDto.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .profileImgUrl(member.getProfileImgUrl())
                .build();
    }
}
