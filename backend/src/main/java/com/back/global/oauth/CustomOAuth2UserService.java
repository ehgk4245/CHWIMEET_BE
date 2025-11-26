package com.back.global.oauth;

import com.back.domain.member.common.MemberRole;
import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String providerTypeCode = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
        OAuth2UserInfo userInfo = getOAuth2UserInfo(providerTypeCode, oAuth2User.getAttributes());

        Member member = memberRepository.findByEmail(userInfo.getEmail()).orElse(null);
        if (member == null) {
            this.joinMember(userInfo.getEmail(), userInfo.getNickname());
        }
        return oAuth2User;
    }

    private OAuth2UserInfo getOAuth2UserInfo(String providerTypeCode, Map<String, Object> attributes) {
        if ("KAKAO".equalsIgnoreCase(providerTypeCode)) {
            return new KakaoUserInfo(attributes);
        }
        throw new OAuth2AuthenticationException("지원하지 않는 로그인 방식입니다: " + providerTypeCode);
    }


    private void joinMember(String username, String nickname) {
        String newPassword = passwordEncoder.encode("");
        memberRepository.save(new Member(username, newPassword, nickname, MemberRole.USER));
    }
}
