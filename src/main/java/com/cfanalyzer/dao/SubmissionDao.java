package com.cfanalyzer.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.cfanalyzer.config.DatabaseConfig;
import com.cfanalyzer.model.Submission;

public class SubmissionDao {

    public void save(Submission submission) throws SQLException {
        String sql = "INSERT INTO submissions (cf_submission_id, user_id, problem_id, problem_name, contest_id, problem_index, verdict, language, time_consumed_ms, memory_consumed_kb, code_content, code_length, cf_submitted_at, is_analyzed) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE verdict=?, time_consumed_ms=?, memory_consumed_kb=?, code_content=?, code_length=?, is_analyzed=?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setLong(1, submission.getCfSubmissionId());
            pstmt.setLong(2, submission.getUserId());
            pstmt.setString(3, submission.getProblemId());
            pstmt.setString(4, submission.getProblemName());
            pstmt.setInt(5, submission.getContestId() != null ? submission.getContestId() : 0);
            pstmt.setString(6, submission.getProblemIndex());
            pstmt.setString(7, submission.getVerdict());
            pstmt.setString(8, submission.getLanguage());
            pstmt.setInt(9, submission.getTimeConsumedMs() != null ? submission.getTimeConsumedMs() : 0);
            pstmt.setInt(10, submission.getMemoryConsumedKb() != null ? submission.getMemoryConsumedKb() : 0);
            pstmt.setString(11, submission.getCodeContent());
            pstmt.setInt(12, submission.getCodeLength() != null ? submission.getCodeLength() : 0);
            pstmt.setTimestamp(13, submission.getCfSubmittedAt() != null ? Timestamp.valueOf(submission.getCfSubmittedAt()) : null);
            pstmt.setBoolean(14, submission.getIsAnalyzed() != null ? submission.getIsAnalyzed() : false);

            // Update part
            pstmt.setString(15, submission.getVerdict());
            pstmt.setInt(16, submission.getTimeConsumedMs() != null ? submission.getTimeConsumedMs() : 0);
            pstmt.setInt(17, submission.getMemoryConsumedKb() != null ? submission.getMemoryConsumedKb() : 0);
            pstmt.setString(18, submission.getCodeContent());
            pstmt.setInt(19, submission.getCodeLength() != null ? submission.getCodeLength() : 0);
            pstmt.setBoolean(20, submission.getIsAnalyzed() != null ? submission.getIsAnalyzed() : false);

            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    submission.setId(generatedKeys.getLong(1));
                }
            }
        }
    }

    public List<Submission> findByUserId(Long userId) throws SQLException {
        List<Submission> submissions = new ArrayList<>();
        String sql = "SELECT * FROM submissions WHERE user_id = ? ORDER BY cf_submitted_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    submissions.add(mapResultSetToSubmission(rs));
                }
            }
        }
        return submissions;
    }

    public List<Submission> findAll() throws SQLException {
        List<Submission> submissions = new ArrayList<>();
        String sql = "SELECT * FROM submissions ORDER BY cf_submitted_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                submissions.add(mapResultSetToSubmission(rs));
            }
        }
        return submissions;
    }

    public List<Submission> findUnanalyzed(int limit) throws SQLException {
        List<Submission> submissions = new ArrayList<>();
        String sql = "SELECT * FROM submissions WHERE is_analyzed = 0 AND verdict = 'OK' LIMIT ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    submissions.add(mapResultSetToSubmission(rs));
                }
            }
        }
        return submissions;
    }

    public void markAsAnalyzed(Long id) throws SQLException {
        String sql = "UPDATE submissions SET is_analyzed = 1 WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        }
    }

    public boolean existsByCfSubmissionId(Long cfSubmissionId) throws SQLException {
        String sql = "SELECT 1 FROM submissions WHERE cf_submission_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, cfSubmissionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Submission mapResultSetToSubmission(ResultSet rs) throws SQLException {
        return Submission.builder()
                .id(rs.getLong("id"))
                .cfSubmissionId(rs.getLong("cf_submission_id"))
                .userId(rs.getLong("user_id"))
                .problemId(rs.getString("problem_id"))
                .problemName(rs.getString("problem_name"))
                .contestId(rs.getInt("contest_id"))
                .problemIndex(rs.getString("problem_index"))
                .verdict(rs.getString("verdict"))
                .language(rs.getString("language"))
                .timeConsumedMs(rs.getInt("time_consumed_ms"))
                .memoryConsumedKb(rs.getInt("memory_consumed_kb"))
                .codeContent(rs.getString("code_content"))
                .codeLength(rs.getInt("code_length"))
                .cfSubmittedAt(rs.getTimestamp("cf_submitted_at") != null ? rs.getTimestamp("cf_submitted_at").toLocalDateTime() : null)
                .crawledAt(rs.getTimestamp("crawled_at").toLocalDateTime())
                .isAnalyzed(rs.getBoolean("is_analyzed"))
                .build();
    }
}
