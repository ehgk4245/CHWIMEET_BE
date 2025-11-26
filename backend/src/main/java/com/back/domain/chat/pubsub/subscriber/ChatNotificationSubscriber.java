package com.back.domain.chat.pubsub.subscriber;

import com.back.domain.chat.dto.ChatNotificationPubSubPayload;
import com.back.domain.chat.service.ChatWebsocketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatNotificationSubscriber implements MessageListener {

    private final ChatWebsocketService chatWebsocketService;
    private final ObjectMapper redisObjectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            ChatNotificationPubSubPayload payload =
                    redisObjectMapper.readValue(message.getBody(), ChatNotificationPubSubPayload.class);

            log.debug("Received chat notification: userId={}, type={}",
                    payload.memberId(), payload.notification().type());

            chatWebsocketService.notify(
                    payload.memberId(),
                    payload.notification()
            );

        } catch (Exception e) {
            log.error("Failed to process chat notification from Redis", e);
        }
    }
}
