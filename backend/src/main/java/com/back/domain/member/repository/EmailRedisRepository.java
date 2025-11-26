package com.back.domain.member.repository;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class EmailRedisRepository {

    private static final String REDIS_KEY_PREFIX = "email:verify:signup:";
    private static final long TTL_SECONDS = 5 * 60L;

    private final StringRedisTemplate stringRedisTemplate;

    public void saveCode(String email, String code) {
        String key = buildKey(email);
        stringRedisTemplate.opsForValue()
                .set(key, code, TTL_SECONDS, TimeUnit.SECONDS);
    }

    @Nullable
    public String getCode(String email) {
        return stringRedisTemplate.opsForValue().get(buildKey(email));
    }

    public void deleteCode(String email) {
        stringRedisTemplate.delete(buildKey(email));
    }

    private String buildKey(String email) {
        return REDIS_KEY_PREFIX + email;
    }
}
