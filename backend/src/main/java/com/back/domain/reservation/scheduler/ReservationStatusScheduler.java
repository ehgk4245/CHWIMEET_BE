package com.back.domain.reservation.scheduler;

import com.back.domain.reservation.scheduler.job.ReservationStatusJob;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationStatusScheduler {
    private final Scheduler scheduler;

    @PostConstruct
    public void init() {
        try {
            // Job 정의
            JobDetail jobDetail = JobBuilder.newJob(ReservationStatusJob.class)
                    .withIdentity("reservationStatusJob", "reservation")
                    .withDescription("예약 상태 자동 업데이트 작업")
                    .storeDurably()
                    .build();

            // 트리거 정의 - 매일 오후 5시 실행
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("reservationStatusTrigger", "reservation")
                    .withSchedule(
                             CronScheduleBuilder.dailyAtHourAndMinute(17, 0)
//                            SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(30).repeatForever()
                    )
                    .forJob(jobDetail)
                    .build();

            // 스케줄러에 작업과 트리거 등록
            if (!scheduler.checkExists(jobDetail.getKey())) {
                scheduler.scheduleJob(jobDetail, trigger);
                log.info("스케쥴러 등록 완료: 예약 상태 자동 업데이트 작업이 매일 오후 5시에 실행됩니다.");
            } else {
                log.info("스케쥴러 이미 등록됨: 예약 상태 자동 업데이트 작업이 이미 등록되어 있습니다.");
            }
        } catch (SchedulerException e) {
            log.error("스케쥴러 등록 실패", e);
        }
    }
}
