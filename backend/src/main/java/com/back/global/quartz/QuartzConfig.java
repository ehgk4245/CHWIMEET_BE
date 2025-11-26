package com.back.global.quartz;

import org.quartz.Scheduler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class QuartzConfig {

    @Bean
    public MyJobListener myJobListener() {
        return new MyJobListener();
    }

    @Bean
    public MyTriggerListener myTriggerListener() {
        return new MyTriggerListener();
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(
            ApplicationContext applicationContext,
            MyJobListener jobListener,
            MyTriggerListener triggerListener
    ) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();

        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        factory.setJobFactory(jobFactory);

        factory.setGlobalJobListeners(jobListener);
        factory.setGlobalTriggerListeners(triggerListener);

        factory.setOverwriteExistingJobs(true);
        factory.setWaitForJobsToCompleteOnShutdown(true);
        return factory;
    }

    @Bean
    public Scheduler scheduler(SchedulerFactoryBean schedulerFactoryBean) {
        return schedulerFactoryBean.getScheduler();
    }
}