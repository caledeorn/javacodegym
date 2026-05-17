package com.cfanalyzer.crawler;

import com.cfanalyzer.model.Submission;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class SubmissionCrawler {
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Submission> crawlUserSubmissions(String handle, int maxSubmissions) {
        List<Submission> submissions = new ArrayList<>();
        // Sử dụng API chính thức của Codeforces
        String url = String.format("https://codeforces.com/api/user.status?handle=%s&from=1&count=%d", handle, maxSubmissions);
        
        System.out.println("Đang lấy danh sách ID từ API: " + url);
        
        Request request = new Request.Builder().url(url).build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("Không thể gọi API Codeforces: " + response.code());
                return submissions;
            }

            JsonNode root = objectMapper.readTree(response.body().string());
            if (!"OK".equals(root.path("status").asText())) {
                System.err.println("API báo lỗi: " + root.path("comment").asText());
                return submissions;
            }

            JsonNode results = root.path("result");
            for (JsonNode node : results) {
                long submissionId = node.path("id").asLong();
                String verdict = node.path("verdict").asText();
                String language = node.path("programmingLanguage").asText();
                
                JsonNode problem = node.path("problem");
                int contestId = problem.path("contestId").asInt();
                String problemIndex = problem.path("index").asText();
                String problemName = problem.path("name").asText();
                
                long creationTimeSeconds = node.path("creationTimeSeconds").asLong();
                LocalDateTime submittedAt = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(creationTimeSeconds), ZoneId.systemDefault());

                Submission sub = Submission.builder()
                        .cfSubmissionId(submissionId)
                        .contestId(contestId)
                        .problemIndex(problemIndex)
                        .problemId(contestId + problemIndex)
                        .problemName(problemName)
                        .verdict(verdict)
                        .language(language)
                        .cfSubmittedAt(submittedAt)
                        .isAnalyzed(false)
                        .build();
                
                submissions.add(sub);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi gọi API: " + e.getMessage());
        }
        
        return submissions;
    }
}
