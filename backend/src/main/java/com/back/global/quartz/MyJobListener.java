package com.back.global.quartz;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

@Slf4j
public class MyJobListener implements JobListener {

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext jobExecutionContext) {
        log.info("[JOB START] jobKey={}, jobClass={}",
                jobExecutionContext.getJobDetail().getKey(),
                jobExecutionContext.getJobDetail().getJobClass().getSimpleName());
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext jobExecutionContext) {
        log.warn("[JOB VETOED] jobKey={}", jobExecutionContext.getJobDetail().getKey());
    }

    @Override
    public void jobWasExecuted(JobExecutionContext jobExecutionContext, JobExecutionException e) {
        if (e == null) {
            log.info("[JOB SUCCESS] jobKey={}", jobExecutionContext.getJobDetail().getKey());
        } else {
            log.error("[JOB FAILED] jobKey={}, error={}",
                    jobExecutionContext.getJobDetail().getKey(),
                    e.getMessage(),
                    e);
        }
    }
}
