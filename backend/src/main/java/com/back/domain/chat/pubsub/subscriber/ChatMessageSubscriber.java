package com.back.domain.chat.pubsub.subscriber;

import com.back.domain.chat.dto.ChatMessagePubSubPayload;
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
public class ChatMessageSubscriber implements MessageListener {

    private final ChatWebsocketService chatWebsocketService;
    private final ObjectMapper redisObjectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            ChatMessagePubSubPayload payload =
                    redisObjectMapper.readValue(message.getBody(), ChatMessagePubSubPayload.class);

            log.debug("Received chat message: chatRoomId={}, messageId={}",
                    payload.chatRoomId(), payload.message().id());

            chatWebsocketService.broadcastMessage(
                    payload.chatRoomId(),
                    payload.message()
            );

        } catch (Exception e) {
            log.error("Failed to process chat message from Redis", e);
        }
    }
}
