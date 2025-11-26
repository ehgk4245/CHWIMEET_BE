package com.back.global.oauth;

import com.back.domain.member.entity.Member;
import com.back.domain.member.repository.MemberRepository;
import com.back.domain.member.service.AuthTokenService;
import com.back.domain.member.service.RefreshTokenStore;
import com.back.global.web.CookieHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final MemberRepository memberRepository;
    private final AuthTokenService authTokenService;
    private final RefreshTokenStore refreshTokenStore;
    private final CookieHelper cookieHelper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String providerTypeCode = oauthToken.getAuthorizedClientRegistrationId().toUpperCase();

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        OAuth2UserInfo userInfo = getOAuth2UserInfo(providerTypeCode, oAuth2User.getAttributes());

        log.info("OAuth2 로그인 성공: provider={}, email={}, nickname={}",
                userInfo.getProvider(), userInfo.getEmail(), userInfo.getNickname());

        // 1) DB에서 Member 조회 (CustomOAuth2UserService 에서 이미 join 해뒀다는 가정)
        Member member = memberRepository.findByEmail(userInfo.getEmail())
                .orElseThrow(() -> new IllegalStateException("OAuth2 Member를 찾을 수 없습니다. email=" + userInfo.getEmail()));

        log.info("OAuth2 로그인 성공: memberId={}, email={}", member.getId(), member.getEmail());
        String accessToken = authTokenService.genAccessToken(member);
        String refreshToken = authTokenService.issueRefresh(member);

        cookieHelper.setCookie("accessToken", accessToken);
        cookieHelper.setCookie("refreshToken", refreshToken);
        log.debug("JWT 토큰 생성 완료 및 쿠키 설정 완료");

        String redicetUrl = "/";
        String stateParam = request.getParameter("state");

        if(stateParam != null) {
            String decodedStateParam = new String(Base64.getUrlDecoder().decode(stateParam), StandardCharsets.UTF_8);

            redicetUrl = decodedStateParam.split("#", 2)[0];
        }

        response.sendRedirect(redicetUrl);
    }

    private OAuth2UserInfo getOAuth2UserInfo(String providerTypeCode, Map<String, Object> attributes) {
        if ("KAKAO".equalsIgnoreCase(providerTypeCode)) {
            return new KakaoUserInfo(attributes);
        }
        throw new OAuth2AuthenticationException("지원하지 않는 로그인 방식입니다: " + providerTypeCode);
    }
}
