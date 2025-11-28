package com.back.domain.report.scheduler;

import com.back.domain.report.scheduler.job.ReportAutoBanJob;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportAutoBanScheduler {
    private final Scheduler scheduler;

    @PostConstruct
    public void init() {
        try {
            JobDetail jobDetail = JobBuilder.newJob(ReportAutoBanJob.class)
                    .withIdentity("ReportAutoBanJob", "report")
                    .withDescription("신고 5건 이상 컨텐츠 자동 차단 작업")
                    .storeDurably()
                    .build();

            // 트리거 정의 - 매일 오후 5시 실행
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("ReportAutoBanTrigger", "report")
                    .withSchedule(
                            CronScheduleBuilder.dailyAtHourAndMinute(17, 0)
//                            SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(30).repeatForever()
                    )
                    .forJob(jobDetail)
                    .build();

            // 스케줄러에 작업과 트리거 등록
            if (!scheduler.checkExists(jobDetail.getKey())) {
                scheduler.scheduleJob(jobDetail, trigger);
                log.info("스케쥴러 등록 완료: 신고 자동 제재 작업이 매일 오후 5시에 실행됩니다.");
            } else {
                log.info("스케쥴러 이미 등록됨: 신고 자동 제재 작업이 이미 등록되어 있습니다.");
            }
        } catch (SchedulerException e) {
            log.error("스케쥴러 등록 실패", e);
        }
    }
}
