package com.cfanalyzer.dao;

import com.cfanalyzer.config.DatabaseConfig;
import com.cfanalyzer.model.CrawlLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CrawlLogDao {

    public void save(CrawlLog log) throws SQLException {
        String sql = "INSERT INTO crawl_logs (user_id, crawl_type, status, submissions_found, submissions_new, error_message, started_at, finished_at, duration_ms) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            if (log.getUserId() != null) pstmt.setLong(1, log.getUserId()); else pstmt.setNull(1, Types.BIGINT);
            pstmt.setString(2, log.getCrawlType());
            pstmt.setString(3, log.getStatus());
            pstmt.setInt(4, log.getSubmissionsFound() != null ? log.getSubmissionsFound() : 0);
            pstmt.setInt(5, log.getSubmissionsNew() != null ? log.getSubmissionsNew() : 0);
            pstmt.setString(6, log.getErrorMessage());
            pstmt.setTimestamp(7, log.getStartedAt() != null ? Timestamp.valueOf(log.getStartedAt()) : Timestamp.valueOf(java.time.LocalDateTime.now()));
            pstmt.setTimestamp(8, log.getFinishedAt() != null ? Timestamp.valueOf(log.getFinishedAt()) : null);
            pstmt.setInt(9, log.getDurationMs() != null ? log.getDurationMs() : 0);

            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    log.setId(generatedKeys.getLong(1));
                }
            }
        }
    }

    public List<CrawlLog> findAll() throws SQLException {
        List<CrawlLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM crawl_logs ORDER BY started_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                logs.add(mapResultSetToCrawlLog(rs));
            }
        }
        return logs;
    }

    private CrawlLog mapResultSetToCrawlLog(ResultSet rs) throws SQLException {
        return CrawlLog.builder()
                .id(rs.getLong("id"))
                .userId(rs.getObject("user_id") != null ? rs.getLong("user_id") : null)
                .crawlType(rs.getString("crawl_type"))
                .status(rs.getString("status"))
                .submissionsFound(rs.getInt("submissions_found"))
                .submissionsNew(rs.getInt("submissions_new"))
                .errorMessage(rs.getString("error_message"))
                .startedAt(rs.getTimestamp("started_at").toLocalDateTime())
                .finishedAt(rs.getTimestamp("finished_at") != null ? rs.getTimestamp("finished_at").toLocalDateTime() : null)
                .durationMs(rs.getInt("duration_ms"))
                .build();
    }
}
