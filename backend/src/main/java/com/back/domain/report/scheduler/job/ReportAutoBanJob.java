package com.back.domain.report.scheduler.job;

import com.back.domain.report.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class ReportAutoBanJob implements Job {
    @Autowired
    private ReportService reportService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            reportService.processAutoBan();
            log.info("자동 차단 작업이 실행되었습니다.");
        } catch (Exception e) {
            log.error("자동 차단 작업 중 오류 발생", e);
            throw new JobExecutionException(e);
        }
    }
}
