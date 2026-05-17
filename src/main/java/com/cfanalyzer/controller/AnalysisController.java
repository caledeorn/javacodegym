package com.cfanalyzer.controller;

import com.cfanalyzer.dao.AnalysisResultDao;
import com.cfanalyzer.dao.SubmissionDao;
import com.cfanalyzer.model.AnalysisResult;
import com.cfanalyzer.model.Submission;
import com.cfanalyzer.service.AIAnalysisService;
import com.cfanalyzer.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AnalysisController {

    @FXML private TableView<Submission> submissionTable;
    @FXML private TableColumn<Submission, Long> subIdColumn;
    @FXML private TableColumn<Submission, Long> userIdSubColumn;
    @FXML private TableColumn<Submission, String> problemColumn;
    @FXML private TableColumn<Submission, String> verdictColumn;
    @FXML private TableColumn<Submission, String> langColumn;
    @FXML private TableColumn<Submission, Boolean> analyzedColumn;
    @FXML private TableColumn<Submission, LocalDateTime> submittedAtColumn;
    
    @FXML private TabPane detailTabPane;
    @FXML private TextArea analysisDetailArea;
    @FXML private TextArea sourceCodeArea;
    @FXML private Label statusLabel;

    private final SubmissionDao submissionDao = new SubmissionDao();
    private final AnalysisResultDao analysisResultDao = new AnalysisResultDao();
    private final AIAnalysisService aiAnalysisService = new AIAnalysisService();
    private final ObservableList<Submission> submissionList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        subIdColumn.setCellValueFactory(new PropertyValueFactory<>("cfSubmissionId"));
        userIdSubColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        problemColumn.setCellValueFactory(new PropertyValueFactory<>("problemName"));
        verdictColumn.setCellValueFactory(new PropertyValueFactory<>("verdict"));
        langColumn.setCellValueFactory(new PropertyValueFactory<>("language"));
        analyzedColumn.setCellValueFactory(new PropertyValueFactory<>("isAnalyzed"));
        submittedAtColumn.setCellValueFactory(new PropertyValueFactory<>("cfSubmittedAt"));

        submissionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showAnalysisResult(newSelection);
            }
        });

        loadSubmissions();
    }

    private void loadSubmissions() {
        try {
            // Tải tất cả bài nộp để người dùng thấy được kết quả crawl
            List<Submission> submissions = submissionDao.findAll();
            submissionList.setAll(submissions);
            submissionTable.setItems(submissionList);
            
            System.out.println("Đã tải " + submissions.size() + " bài nộp vào bảng phân tích.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        loadSubmissions();
    }

    @FXML
    private void handleAnalyzeSelected() {
        Submission selected = submissionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Cảnh báo", "Vui lòng chọn một bài nộp từ danh sách trước khi thực hiện phân tích.");
            return;
        }

        if (selected.getIsAnalyzed()) {
            boolean reAnalyze = AlertUtil.showConfirm("Xác nhận", "Bài này đã được phân tích. Bạn có muốn phân tích lại không?");
            if (!reAnalyze) return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                int maxRetries = 3;
                int retryCount = 0;
                boolean success = false;

                while (retryCount < maxRetries && !success) {
                    try {
                        updateMessage("Đang phân tích (Lần " + (retryCount + 1) + "): " + selected.getCfSubmissionId());
                        aiAnalysisService.analyzeSubmission(selected);
                        success = true;
                    } catch (Exception e) {
                        String msg = e.getMessage() != null ? e.getMessage() : "";
                        if (msg.contains("429")) {
                            retryCount++;
                            if (retryCount >= maxRetries) {
                                throw new Exception("Đã thử lại " + maxRetries + " lần nhưng vẫn hết hạn mức API.");
                            }
                            for (int i = 60; i > 0; i--) {
                                updateMessage("Hết hạn mức (Lần " + retryCount + "). Thử lại sau " + i + "s...");
                                Thread.sleep(1000);
                            }
                        } else {
                            throw e;
                        }
                    }
                }
                return null;
            }

            @Override
            protected void succeeded() {
                updateMessage("Phân tích hoàn tất.");
                selected.setIsAnalyzed(true); // Cập nhật trạng thái cho đối tượng hiện tại
                loadSubmissions();
                showAnalysisResult(selected);
                AlertUtil.showInfo("Thành công", "Đã phân tích xong bài nộp " + selected.getCfSubmissionId());
            }

            @Override
            protected void failed() {
                updateMessage("Phân tích thất bại.");
                Throwable e = getException();
                e.printStackTrace();
                AlertUtil.showError("Lỗi", "Không thể phân tích: " + e.getMessage());
            }
        };

        statusLabel.textProperty().bind(task.messageProperty());
        new Thread(task).start();
    }

    private void showAnalysisResult(Submission submission) {
        // Luôn hiển thị mã nguồn trước
        if (submission.getCodeContent() != null && !submission.getCodeContent().isEmpty()) {
            sourceCodeArea.setText(submission.getCodeContent());
        } else {
            sourceCodeArea.setText("Mã nguồn chưa được crawl hoặc không khả dụng.");
        }

        if (!submission.getIsAnalyzed()) {
            analysisDetailArea.setText("Bài nộp này chưa được phân tích AI.");
            return;
        }

        try {
            Optional<AnalysisResult> resultOpt = analysisResultDao.findBySubmissionId(submission.getId());
            if (resultOpt.isPresent()) {
                AnalysisResult result = resultOpt.get();
                StringBuilder sb = new StringBuilder();
                sb.append("Tóm tắt: ").append(result.getOverallSummary()).append("\n\n");
                sb.append("Thuật toán chính: ").append(result.getAlgoPrimary()).append(" (").append(result.getAlgoCategory()).append(")\n");
                sb.append("CTDL chính: ").append(result.getDsPrimary()).append("\n");
                sb.append("Độ phức tạp thời gian: ").append(result.getTimeComplexity()).append("\n");
                sb.append("Độ phức tạp không gian: ").append(result.getSpaceComplexity()).append("\n");
                sb.append("Khả năng dùng AI: ").append(result.getAiProbability() * 100).append("%\n");
                sb.append("Giải thích AI: ").append(result.getAiExplanation()).append("\n");
                sb.append("Chất lượng code: ").append(result.getCodeQualityScore()).append("/100\n");
                sb.append("Độ dễ đọc: ").append(result.getReadabilityScore()).append("/100\n");
                sb.append("Độ độc đáo: ").append(result.getOriginalityScore()).append("/100\n");
                sb.append("Độ khó ước tính: ").append(result.getDifficultyEstimate()).append("\n");
                
                analysisDetailArea.setText(sb.toString());
            } else {
                analysisDetailArea.setText("Không tìm thấy kết quả phân tích trong CSDL.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            analysisDetailArea.setText("Lỗi khi tải kết quả phân tích.");
        }
    }
}
