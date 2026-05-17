package com.cfanalyzer.service;

import com.cfanalyzer.ai.GeminiApiClient;
import com.cfanalyzer.ai.CodeAnalysisPromptBuilder;
import com.cfanalyzer.ai.AnalysisResultParser;
import com.cfanalyzer.dao.AnalysisResultDao;
import com.cfanalyzer.dao.SubmissionDao;
import com.cfanalyzer.model.AnalysisResult;
import com.cfanalyzer.model.Submission;

import java.sql.SQLException;
import java.util.List;

public class AIAnalysisService {
    private final SubmissionDao submissionDao = new SubmissionDao();
    private final AnalysisResultDao analysisResultDao = new AnalysisResultDao();
    private final GeminiApiClient geminiApiClient = new GeminiApiClient();
    private final AnalysisResultParser resultParser = new AnalysisResultParser();

    public void analyzePendingSubmissions(int batchSize) {
        try {
            List<Submission> pending = submissionDao.findUnanalyzed(batchSize);
            for (Submission submission : pending) {
                try {
                    analyzeSubmission(submission);
                    // Nghỉ 4 giây giữa mỗi lần gọi API để tránh lỗi Quota Exceeded (429)
                    Thread.sleep(4000);
                } catch (Exception e) {
                    System.err.println("Lỗi khi phân tích bài nộp " + submission.getCfSubmissionId() + ": " + e.getMessage());
                    // Tiếp tục với bài nộp tiếp theo thay vì dừng toàn bộ quá trình
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void analyzeSubmission(Submission submission) throws Exception {
        if (submission.getCodeContent() == null || submission.getCodeContent().isEmpty()) {
            return;
        }

        String prompt = CodeAnalysisPromptBuilder.buildPrompt(
                submission.getLanguage(),
                submission.getProblemName(),
                submission.getProblemId(),
                submission.getCodeContent()
        );

        long startTime = System.currentTimeMillis();
        String response = geminiApiClient.analyzeCode(prompt);
        long duration = System.currentTimeMillis() - startTime;

        AnalysisResult result = resultParser.parse(response);
        result.setSubmissionId(submission.getId());
        result.setUserId(submission.getUserId());
        result.setAnalysisDurationMs((int) duration);
        result.setModelUsed("gemini-2.0-flash");

        analysisResultDao.save(result);
        submissionDao.markAsAnalyzed(submission.getId());
    }
}
