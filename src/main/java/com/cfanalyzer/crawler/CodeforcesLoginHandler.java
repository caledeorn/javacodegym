package com.cfanalyzer.crawler;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CodeforcesLoginHandler {
    
    public static boolean login(WebDriver driver) {
        // Ở chế độ Remote Debugging, chúng ta ưu tiên người dùng tự đăng nhập 
        // để tránh bị Cloudflare phát hiện hành vi tự động điền.
        if (isLoggedIn(driver)) {
            return true;
        }
        
        System.out.println("Vui lòng đăng nhập Codeforces trên cửa sổ trình duyệt Edge đang mở...");
        driver.get("https://codeforces.com/enter");
        
        // Đợi người dùng đăng nhập thủ công trong tối đa 2 phút
        for (int i = 0; i < 120; i++) {
            if (isLoggedIn(driver)) {
                System.out.println("Đã nhận diện đăng nhập thành công!");
                return true;
            }
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
        }
        
        return false;
    }
    
    public static boolean isLoggedIn(WebDriver driver) {
        try {
            // Kiểm tra sự hiện diện của link Logout hoặc link hồ sơ cá nhân
            return driver.findElements(By.xpath("//a[contains(@href, '/logout')]")).size() > 0 ||
                   driver.findElements(By.xpath("//a[contains(@href, '/profile/')]")).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
