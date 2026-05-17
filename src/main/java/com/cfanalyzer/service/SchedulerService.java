package com.cfanalyzer.service;

import com.cfanalyzer.config.AppConfig;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

public class SchedulerService {
    private Scheduler scheduler;

    public void start() {
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();

            String cronExpression = AppConfig.get("crawl.cron", "0 0 2 * * ?");
            
            JobDetail job = JobBuilder.newJob(CrawlJob.class)
                    .withIdentity("crawlJob", "group1")
                    .build();

            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("crawlTrigger", "group1")
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                    .build();

            scheduler.scheduleJob(job, trigger);
            System.out.println("Scheduler started with cron: " + cronExpression);

        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown();
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
