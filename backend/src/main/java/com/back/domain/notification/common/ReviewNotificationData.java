package com.back.domain.notification.common;

public record ReviewNotificationData(
        ReviewInfo reviewInfo,
        PostInfo postInfo
) implements NotificationData{

    public record ReviewInfo(
            Long id,
            Author author
    ) {}

    public record PostInfo(
            Long id,
            String title,
            Author author
    ) {}
}
