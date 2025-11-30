package com.back.domain.notification.service;

import com.back.domain.notification.common.NotificationData;
import com.back.domain.notification.dto.NotificationResBody;
import com.back.global.sse.EmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseNotificationService {

    private final EmitterRepository emitterRepository;
    private static final Long TIMEOUT = 60L * 1000 * 60; // 1시간


    public SseEmitter subscribe(Long memberId) {
        String emitterId = memberId + "_" + System.currentTimeMillis();
        SseEmitter emitter = createAndSaveEmitter(memberId, emitterId);

        sendInitialEvent(memberId, emitterId, emitter);
        registerEmitterCallbacks(memberId, emitterId, emitter);

        return emitter;
    }

    private SseEmitter createAndSaveEmitter(Long memberId, String emitterId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitterRepository.save(memberId, emitterId, emitter);
        return emitter;
    }

    private void sendInitialEvent(Long memberId, String emitterId, SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                    .id(emitterId)
                    .data("connected"));
        } catch (Exception e) {
            log.error("SSE 발행 중 예외 발생: memberId={}, emitterId={}, error={}",
                    memberId, emitterId, e.getMessage(), e);
        }
    }

    private void registerEmitterCallbacks(Long memberId, String emitterId, SseEmitter emitter) {
        emitter.onCompletion(() -> emitterRepository.deleteEmitter(memberId, emitterId));
        emitter.onTimeout(() -> emitterRepository.deleteEmitter(memberId, emitterId));
        emitter.onError(e -> emitterRepository.deleteEmitter(memberId, emitterId));
    }

    @Async("notificationExecutor")
    public void sendNotification(Long targetMemberId, NotificationResBody<? extends NotificationData> message) {
        Map<String, SseEmitter> emitters = emitterRepository.findEmittersByMemberId(targetMemberId);

        emitters.forEach((emitterId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .id(emitterId)
                        .data(message));
                log.debug("알림 전송 성공: memberId={}, emitterId={}", targetMemberId, emitterId);
            } catch (IOException e) {
                // 클라이언트 연결 끊김 - 정상적인 상황
                log.debug("클라이언트 연결 끊김 감지 (memberId={}, emitterId={}): {}",
                        targetMemberId, emitterId, e.getMessage());
                emitterRepository.deleteEmitter(targetMemberId, emitterId);
            } catch (Exception e) {
                // 기타 예외 - 예상치 못한 에러
                log.error("알림 전송 중 예외 발생 (memberId={}, emitterId={})",
                        targetMemberId, emitterId, e);
                emitterRepository.deleteEmitter(targetMemberId, emitterId);
            }
        });
    }
}
