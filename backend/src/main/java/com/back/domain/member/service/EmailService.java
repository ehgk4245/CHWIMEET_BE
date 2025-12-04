package com.back.domain.member.service;

import com.back.domain.member.repository.EmailRedisRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final EmailRedisRepository emailRedisRepository;
    private final EmailSender emailSender;

    public LocalDateTime sendVerificationCode(String email) {
        String code = generateCode();
        emailRedisRepository.saveCode(email, code);

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        emailSender.sendMailAsync(email, code);

        return expiresAt;
    }

    public void verifyCode(String email, String code) {
        String savedCode = emailRedisRepository.getCode(email);

        if (savedCode == null) {
            throw new ServiceException(HttpStatus.GONE, "인증코드가 만료되었거나 존재하지 않습니다.");
        }

        if (!savedCode.equals(code)) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "인증코드가 일치하지 않습니다.");
        }

        emailRedisRepository.deleteCode(email);
    }

    private String generateCode() {
        int code = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return String.valueOf(code);
    }
}
