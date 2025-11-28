package com.back.domain.notification.scheduler.job;

import com.back.domain.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class DeleteOldNotificationJob implements Job {

    @Autowired
    private NotificationService notificationService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("오래된 알림 삭제 진행 중...");

        int deleted = notificationService.deleteOldNotifications();

        log.info("{}개 오래된 알림 제거 완료.", deleted);
    }
}
