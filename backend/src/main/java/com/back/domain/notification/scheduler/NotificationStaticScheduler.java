package com.back.domain.notification.scheduler;

import com.back.domain.notification.scheduler.job.DeleteOldNotificationJob;
import org.quartz.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationStaticScheduler {

    @Bean
    public JobDetail deleteOldNotificationJobDetail() {
        return JobBuilder.newJob(DeleteOldNotificationJob.class)
                .withIdentity("deleteOldNotificationJob", "maintenance")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger deleteOldNotificationTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(deleteOldNotificationJobDetail())
                .withIdentity("deleteOldNotificationTrigger", "maintenance")
                .withSchedule(
                        CronScheduleBuilder.cronSchedule("0 0 17 * * ?")
                )
                .build();
    }

    @Bean
    public CommandLineRunner registerJobs(
            Scheduler scheduler,
            JobDetail deleteOldNotificationJobDetail,
            Trigger deleteOldNotificationTrigger
    ) {
        return args -> {
            scheduler.scheduleJob(deleteOldNotificationJobDetail, deleteOldNotificationTrigger);
        };
    }
}
