package com.cfanalyzer.service;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class CrawlJob implements Job {
    private final CrawlerService crawlerService = new CrawlerService();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("Starting scheduled crawl job...");
        crawlerService.crawlAllUsers();
        System.out.println("Scheduled crawl job finished.");
    }
}
