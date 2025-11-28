package com.back.domain.notification.mapper;

import com.back.domain.notification.common.Author;
import com.back.domain.notification.common.NotificationType;
import com.back.domain.notification.common.ReviewNotificationData;
import com.back.domain.notification.entity.Notification;
import com.back.domain.review.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewNotificationMapper implements NotificationDataMapper<ReviewNotificationData> {

    @Override
    public boolean supports(NotificationType type) {
        return type.getGroupType() == NotificationType.GroupType.REVIEW;
    }

    @Override
    public ReviewNotificationData map(Object entity, Notification notification) {
        Review review = (Review) entity;
        return new ReviewNotificationData(
                new ReviewNotificationData.ReviewInfo(
                        review.getId(),
                        new Author(
                                review.getReservation().getAuthor().getId(),
                                review.getReservation().getAuthor().getNickname()
                        )
                ),
                new ReviewNotificationData.PostInfo(
                        review.getReservation().getPost().getId(),
                        review.getReservation().getPost().getTitle(),
                        new Author(
                                review.getReservation().getPost().getAuthor().getId(),
                                review.getReservation().getPost().getAuthor().getNickname()
                        )
                )
        );
    }
}

