package com.cfanalyzer.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class CodeExtractor {
    
    public String extractCode(WebDriver driver, int contestId, long submissionId) {
        String baseUrl = (contestId > 100000) ? "https://codeforces.com/gym/" : "https://codeforces.com/contest/";
        String url = baseUrl + contestId + "/submission/" + submissionId;
        
        try {
            driver.get(url);
            
            // Chỉ đợi tối đa 5 giây cho code xuất hiện (vì dùng trình duyệt thật nên thường rất nhanh)
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            
            // Thử lấy code ngay lập tức
            wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.id("program-source-text")),
                ExpectedConditions.presenceOfElementLocated(By.className("prettyprint"))
            ));
            
            Document doc = Jsoup.parse(driver.getPageSource());
            Element codePre = doc.selectFirst("pre#program-source-text, pre.prettyprint");
            
            if (codePre != null) {
                return codePre.text();
            }
        } catch (Exception e) {
            // Nếu lỗi do Cloudflare chặn giữa chừng
            if (driver.getPageSource().contains("Verify you are human")) {
                System.out.println("Gặp Cloudflare! Hãy nhấn xác nhận trên trình duyệt...");
                try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
            }
        }
        return null;
    }
}
