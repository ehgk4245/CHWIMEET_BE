package com.back.domain.notification.config;

import com.back.domain.notification.common.NotificationData;
import com.back.domain.notification.common.NotificationType;
import com.back.domain.notification.mapper.NotificationDataMapper;
import com.back.domain.reservation.entity.Reservation;
import com.back.domain.reservation.repository.ReservationQueryRepository;
import com.back.domain.review.entity.Review;
import com.back.domain.review.repository.ReviewQueryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class NotificationConfig {

    @Bean
    public Map<NotificationType.GroupType, Function<List<Long>, Map<Long, ?>>> batchLoaders(
            ReservationQueryRepository reservationQueryRepository,
            ReviewQueryRepository reviewQueryRepository
    ) {
        Map<NotificationType.GroupType, Function<List<Long>, Map<Long, ?>>> batchLoaders = new HashMap<>();
        batchLoaders.put(NotificationType.GroupType.RESERVATION, targetIds ->
                reservationQueryRepository.findWithPostAndAuthorByIds(targetIds)
                        .stream().collect(Collectors.toMap(Reservation::getId, r -> r))
        );
        batchLoaders.put(NotificationType.GroupType.REVIEW, targetIds ->
                reviewQueryRepository.findWithReservationAndPostAndAuthorsByIds(targetIds)
                        .stream().collect(Collectors.toMap(Review::getId, r -> r))
        );
        return batchLoaders;
    }

    @Bean
    public Map<NotificationType, NotificationDataMapper<? extends NotificationData>> mapperRegistry(
            List<NotificationDataMapper<? extends NotificationData>> mappers
    ) {
        Map<NotificationType, NotificationDataMapper<? extends NotificationData>> map = new HashMap<>();
        for (NotificationDataMapper<?> mapper : mappers) {
            for (NotificationType type : NotificationType.values()) {
                if (mapper.supports(type)) {
                    map.put(type, mapper);
                }
            }
        }
        return map;
    }
}
