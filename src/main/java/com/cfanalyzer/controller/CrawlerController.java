package com.cfanalyzer.controller;

import com.cfanalyzer.crawler.CodeforcesLoginHandler;
import com.cfanalyzer.crawler.EdgeDriverManager;
import com.cfanalyzer.dao.CrawlLogDao;
import com.cfanalyzer.model.CrawlLog;
import com.cfanalyzer.service.CrawlerService;
import com.cfanalyzer.util.AlertUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.openqa.selenium.WebDriver;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class CrawlerController {

    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;
    @FXML private TableView<CrawlLog> logTable;
    @FXML private TableColumn<CrawlLog, Long> logIdColumn;
    @FXML private TableColumn<CrawlLog, Long> userIdColumn;
    @FXML private TableColumn<CrawlLog, String> typeColumn;
    @FXML private TableColumn<CrawlLog, String> logStatusColumn;
    @FXML private TableColumn<CrawlLog, Integer> foundColumn;
    @FXML private TableColumn<CrawlLog, Integer> newColumn;
    @FXML private TableColumn<CrawlLog, LocalDateTime> startedColumn;
    @FXML private TextArea logArea;

    private final CrawlLogDao crawlLogDao = new CrawlLogDao();
    private final CrawlerService crawlerService = new CrawlerService();
    private final ObservableList<CrawlLog> logList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        logIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("crawlType"));
        logStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        foundColumn.setCellValueFactory(new PropertyValueFactory<>("submissionsFound"));
        newColumn.setCellValueFactory(new PropertyValueFactory<>("submissionsNew"));
        startedColumn.setCellValueFactory(new PropertyValueFactory<>("startedAt"));

        loadLogs();
    }

    private void loadLogs() {
        try {
            List<CrawlLog> logs = crawlLogDao.findAll();
            logList.setAll(logs);
            logTable.setItems(logList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCrawlAll() {
        Task<Void> crawlTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Đang bắt đầu crawl...");
                crawlerService.crawlAllUsers();
                return null;
            }

            @Override
            protected void succeeded() {
                updateMessage("Hoàn thành crawl.");
                loadLogs();
                AlertUtil.showInfo("Thông báo", "Quá trình crawl đã hoàn thành.");
            }

            @Override
            protected void failed() {
                updateMessage("Crawl thất bại.");
                loadLogs();
                AlertUtil.showError("Lỗi", "Quá trình crawl gặp lỗi.");
            }
        };

        statusLabel.textProperty().bind(crawlTask.messageProperty());
        new Thread(crawlTask).start();
    }

    @FXML
    private void handleTestConnection() {
        Task<Boolean> testTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                updateMessage("Đang khởi tạo trình duyệt...");
                WebDriver driver = EdgeDriverManager.getDriver();
                updateMessage("Đang kiểm tra đăng nhập...");
                return CodeforcesLoginHandler.login(driver);
            }

            @Override
            protected void succeeded() {
                if (getValue()) {
                    updateMessage("Kết nối và đăng nhập thành công.");
                    AlertUtil.showInfo("Thành công", "Đã kết nối và đăng nhập Codeforces thành công.");
                } else {
                    updateMessage("Đăng nhập thất bại.");
                    AlertUtil.showError("Lỗi", "Không thể đăng nhập Codeforces. Kiểm tra lại credentials.");
                }
            }

            @Override
            protected void failed() {
                updateMessage("Lỗi kết nối.");
                AlertUtil.showError("Lỗi", "Không thể kết nối với EdgeDriver.");
            }
        };

        statusLabel.textProperty().bind(testTask.messageProperty());
        new Thread(testTask).start();
    }
    
    private void appendLog(String message) {
        Platform.runLater(() -> logArea.appendText(LocalDateTime.now() + ": " + message + "\n"));
    }
}
