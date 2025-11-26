package com.back.domain.chat.pubsub.publisher;

import com.back.domain.chat.dto.ChatNotiDto;
import com.back.domain.chat.dto.ChatNotificationPubSubPayload;
import com.back.domain.chat.service.ChatWebsocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatNotificationPublisher {

    private final ChatWebsocketService chatWebsocketService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic chatNotificationTopic;

    public void publish(Long memberId, ChatNotiDto dto) {
        try {
            ChatNotificationPubSubPayload payload = new ChatNotificationPubSubPayload(memberId, dto);

            redisTemplate.convertAndSend(chatNotificationTopic.getTopic(), payload);
            log.debug("Published chat notification to member {}: {}", memberId, dto.type());

        } catch (Exception e) {
            log.error("Failed to publish chat notification: memberId={}, type={}",
                    memberId, dto.type(), e);
            chatWebsocketService.notify(memberId, dto);
        }
    }
}
