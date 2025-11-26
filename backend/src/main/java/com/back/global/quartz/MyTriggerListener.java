package com.back.global.quartz;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.TriggerListener;

@Slf4j
public class MyTriggerListener implements TriggerListener {

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext jobExecutionContext) {
        log.info("[TRIGGER FIRED] triggerKey={}, jobKey={}",
                trigger.getKey(), jobExecutionContext.getJobDetail().getKey());
    }

    // 특정 조건에서 Job 실행 방지
    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext jobExecutionContext) {
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        log.error("[TRIGGER MISFIRED] triggerKey={}", trigger.getKey());
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext jobExecutionContext, Trigger.CompletedExecutionInstruction completedExecutionInstruction) {
        log.info("[TRIGGER COMPLETE] triggerKey={}, jobKey={}",
                trigger.getKey(), jobExecutionContext.getJobDetail().getKey());
    }
}
