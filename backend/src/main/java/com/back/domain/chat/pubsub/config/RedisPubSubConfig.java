package com.back.domain.chat.pubsub.config;

import com.back.domain.chat.pubsub.subscriber.ChatMessageSubscriber;
import com.back.domain.chat.pubsub.subscriber.ChatNotificationSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class RedisPubSubConfig {

    private final ChatMessageSubscriber chatMessageSubscriber;
    private final ChatNotificationSubscriber chatNotificationSubscriber;

    @Bean
    public ChannelTopic chatMessageTopic() {
        return new ChannelTopic("chat:messages");
    }

    @Bean
    public ChannelTopic chatNotificationTopic() {
        return new ChannelTopic("chat:notifications");
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);

        container.addMessageListener(chatMessageSubscriber, chatMessageTopic());
        container.addMessageListener(chatNotificationSubscriber, chatNotificationTopic());

        return container;
    }
}
