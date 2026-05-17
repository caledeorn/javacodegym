package com.cfanalyzer.crawler;

import com.cfanalyzer.config.AppConfig;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import java.io.File;

public class EdgeDriverManager {
    private static WebDriver driver;
    
    public static WebDriver getDriver() {
        if (driver == null) {
            String edgeDriverPath = AppConfig.get("edge.driver.path");
            if (edgeDriverPath != null && !edgeDriverPath.isEmpty()) {
                File driverFile = new File(edgeDriverPath);
                if (driverFile.exists()) {
                    System.setProperty("webdriver.edge.driver", edgeDriverPath);
                }
            }
            
            EdgeOptions options = new EdgeOptions();
            
            // CHẾ ĐỘ "MƯỢN" TRÌNH DUYỆT ĐANG CHẠY
            // Selenium sẽ kết nối vào cổng 9222 của cửa sổ Edge bạn tự mở
            options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");

            try {
                driver = new EdgeDriver(options);
            } catch (Exception e) {
                System.err.println("KHÔNG THỂ KẾT NỐI: Bạn cần mở Edge với cổng debug trước! (Xem hướng dẫn)");
                throw e;
            }
        }
        return driver;
    }
    
    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
