package com.back.domain.post.scheduler;

import com.back.domain.post.scheduler.job.PostEmbeddingJob;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostEmbeddingScheduler {
    private final Scheduler scheduler;

    @PostConstruct
    public void init() {
        try {
            JobDetail jobDetail = JobBuilder.newJob(PostEmbeddingJob.class)
                    .withIdentity("PostEmbeddingJob", "post")
                    .withDescription("게시글 임베딩 작업")
                    .storeDurably()
                    .build();

            // 트리거 정의 - 매일 정각 마다 진행
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("PostEmbeddingTrigger", "post")
                    .withSchedule(
                            CronScheduleBuilder.cronSchedule("0 0 * * * ?")
//                            SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(30).repeatForever()
                    )
                    .forJob(jobDetail)
                    .build();

            // 스케줄러에 작업과 트리거 등록
            if (!scheduler.checkExists(jobDetail.getKey())) {
                scheduler.scheduleJob(jobDetail, trigger);
                log.info("스케쥴러 등록 완료: 게시글 임베딩 작업이 정각마다 실행됩니다.");
            } else {
                log.info("스케쥴러 이미 등록됨: 게시글 임베딩 작업이 이미 등록되어 있습니다.");
            }
        } catch (SchedulerException e) {
            log.error("스케쥴러 등록 실패", e);
        }
    }
}
