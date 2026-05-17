package com.cfanalyzer.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.openqa.selenium.WebDriver;

import com.cfanalyzer.config.AppConfig;
import com.cfanalyzer.crawler.CodeExtractor;
import com.cfanalyzer.crawler.CodeforcesLoginHandler;
import com.cfanalyzer.crawler.EdgeDriverManager;
import com.cfanalyzer.crawler.SubmissionCrawler;
import com.cfanalyzer.dao.CrawlLogDao;
import com.cfanalyzer.dao.SubmissionDao;
import com.cfanalyzer.dao.UserDao;
import com.cfanalyzer.model.CodeforcesUser;
import com.cfanalyzer.model.CrawlLog;
import com.cfanalyzer.model.Submission;

public class CrawlerService {
    private final UserDao userDao = new UserDao();
    private final SubmissionDao submissionDao = new SubmissionDao();
    private final CrawlLogDao crawlLogDao = new CrawlLogDao();
    private final SubmissionCrawler submissionCrawler = new SubmissionCrawler();
    private final CodeExtractor codeExtractor = new CodeExtractor();
    private final Random random = new Random();

    public void crawlAllUsers() {
        try {
            List<CodeforcesUser> users = userDao.findAll();
            for (CodeforcesUser user : users) {
                if (user.getIsActive()) {
                    crawlUser(user.getHandle());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void crawlUser(String handle) {
        WebDriver driver = null;
        CrawlLog log = CrawlLog.builder()
                .crawlType("MANUAL")
                .status("RUNNING")
                .startedAt(LocalDateTime.now())
                .build();
        
        try {
            CodeforcesUser user = userDao.findByHandle(handle).orElse(null);
            if (user != null) log.setUserId(user.getId());

            // 1. ĐĂNG NHẬP TRƯỚC (Dùng Selenium)
            // Việc này đảm bảo chúng ta có Session để xem được mã nguồn
            driver = EdgeDriverManager.getDriver();
            if (!CodeforcesLoginHandler.login(driver)) {
                throw new Exception("Đăng nhập thất bại hoặc trình duyệt chưa sẵn sàng!");
            }

            // 2. LẤY DANH SÁCH BÀI NỘP TỪ API (Nhanh và chính xác)
            int maxSubmissions = AppConfig.getInt("crawl.max.submissions", 200);
            List<Submission> submissions = submissionCrawler.crawlUserSubmissions(handle, maxSubmissions);
            log.setSubmissionsFound(submissions.size());
            
            // 3. CÀO MÃ NGUỒN CHO CÁC BÀI MỚI
            int newCount = 0;
            for (Submission sub : submissions) {
                sub.setUserId(user.getId());
                
                // Kiểm tra bài đã tồn tại chưa
                if (submissionDao.existsByCfSubmissionId(sub.getCfSubmissionId())) {
                    continue;
                }

                // Dùng trình duyệt đã đăng nhập ở bước 1 để lấy code
                String code = codeExtractor.extractCode(driver, sub.getContestId(), sub.getCfSubmissionId());
                if (code != null) {
                    sub.setCodeContent(code);
                    sub.setCodeLength(code.length());
                    submissionDao.save(sub);
                    newCount++;
                    
                    System.out.println("Đã cào thành công bài: " + sub.getCfSubmissionId());
                    
                    // Tăng độ trễ lên 2-5 giây để an toàn hơn
                    int minDelay = AppConfig.getInt("crawl.delay.min.ms", 2000);
                    int maxDelay = AppConfig.getInt("crawl.delay.max.ms", 5000);
                    Thread.sleep(minDelay + random.nextInt(maxDelay - minDelay + 1));
                }
            }
            
            log.setSubmissionsNew(newCount);
            log.setStatus("SUCCESS");
            
            // Update user last crawled at
            if (user != null) {
                user.setLastCrawledAt(LocalDateTime.now());
                userDao.save(user);
            }

        } catch (Exception e) {
            log.setStatus("FAILED");
            log.setErrorMessage(e.getMessage());
            e.printStackTrace();
        } finally {
            log.setFinishedAt(LocalDateTime.now());
            log.setDurationMs((int) java.time.Duration.between(log.getStartedAt(), log.getFinishedAt()).toMillis());
            try {
                crawlLogDao.save(log);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
