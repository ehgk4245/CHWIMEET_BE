package com.back.domain.chat.pubsub.publisher;

import com.back.domain.chat.dto.ChatMessageDto;
import com.back.domain.chat.dto.ChatMessagePubSubPayload;
import com.back.domain.chat.service.ChatWebsocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessagePublisher {

    private final ChatWebsocketService chatWebsocketService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic chatMessageTopic;

    public void publish(Long chatRoomId, ChatMessageDto dto) {
        try {
            ChatMessagePubSubPayload payload = new ChatMessagePubSubPayload(chatRoomId, dto);

            redisTemplate.convertAndSend(chatMessageTopic.getTopic(), payload);
            log.debug("Published chat message to room {}: {}", chatRoomId, dto.id());

        } catch (Exception e) {
            log.error("Failed to publish chat message: chatRoomId={}, messageId={}",
                    chatRoomId, dto.id(), e);
            chatWebsocketService.broadcastMessage(chatRoomId, dto);
        }
    }
}
