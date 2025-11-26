package com.back.domain.reservation.scheduler.job;

import com.back.domain.reservation.service.ReservationService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class ReservationStatusJob implements Job {
    @Autowired
    private ReservationService reservationService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            reservationService.updateReservationStatuses();
        } catch (Exception e) {
            log.error("예약 상태 자동 업데이트 중 오류 발생", e);
            throw new JobExecutionException(e);
        }
    }
}
