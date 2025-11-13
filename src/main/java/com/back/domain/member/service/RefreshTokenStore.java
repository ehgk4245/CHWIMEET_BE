package com.back.domain.member.service;

import com.back.standard.util.jwt.JwtUt;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RefreshTokenStore {
    private final StringRedisTemplate redis;

    private String keyRefresh(String jti) { return "refresh:" + jti; }
    private String keyUserSessions(long userId) { return "user:sessions:" + userId; }
    private String keyAuthVer(long userId) { return "authver:" + userId; }

    // 저장 (TTL 부여)
    public void saveRefresh(String jti, long userId, Duration ttl, String payloadJson) {
        redis.opsForValue().set(keyRefresh(jti), payloadJson, ttl);
        redis.opsForSet().add(keyUserSessions(userId), jti);
    }

    public @Nullable String findRefreshPayload(String jti) {
        return redis.opsForValue().get(keyRefresh(jti));
    }

    public void deleteRefresh(String jti, long userId) {
        redis.delete(keyRefresh(jti));
        redis.opsForSet().remove(keyUserSessions(userId), jti);
    }

    public void revoke(String refreshPlain) {
        if (refreshPlain == null || refreshPlain.isBlank()) return;

        String jti = JwtUt.sha256(refreshPlain); // 저장 규칙과 동일하게
        String kRefresh = keyRefresh(jti);

        // RT:{jti} -> userId 읽기 (없을 수 있음)
        String userIdStr = redis.opsForValue().get(kRefresh);
        // 먼저 jti 키 삭제
        redis.delete(kRefresh);

        // 역집합에서 제거
        if (userIdStr != null) {
            long userId = Long.parseLong(userIdStr);
            redis.opsForSet().remove(keyUserSessions(userId), jti);
        }
    }


    public void revokeAllForUser(long userId) {
        String k = keyUserSessions(userId);
        Set<String> all = redis.opsForSet().members(k);
        if (all != null) {
            for (String jti : all) redis.delete(keyRefresh(jti));
            redis.delete(k);
        }
    }

    // 권한 버전
    public long getAuthVersion(long userId) {
        String v = redis.opsForValue().get(keyAuthVer(userId));
        return (v == null) ? 1L : Long.parseLong(v);
    }

    public void bumpAuthVersion(long userId) {
        redis.opsForValue().increment(keyAuthVer(userId)); // 없다면 1 생성
    }
}
