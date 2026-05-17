package com.cfanalyzer.dao;

import com.cfanalyzer.config.DatabaseConfig;
import com.cfanalyzer.model.CodeforcesUser;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {

    public CodeforcesUser save(CodeforcesUser user) throws SQLException {
        String sql = "INSERT INTO cf_users (handle, display_name, rating, max_rating, rank_title, country, organization, avatar_url, cf_registered_at, is_active, last_crawled_at, total_solved, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE display_name=?, rating=?, max_rating=?, rank_title=?, country=?, organization=?, avatar_url=?, cf_registered_at=?, is_active=?, last_crawled_at=?, total_solved=?, notes=?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, user.getHandle());
            pstmt.setString(2, user.getDisplayName());
            pstmt.setInt(3, user.getRating() != null ? user.getRating() : 0);
            pstmt.setInt(4, user.getMaxRating() != null ? user.getMaxRating() : 0);
            pstmt.setString(5, user.getRankTitle());
            pstmt.setString(6, user.getCountry());
            pstmt.setString(7, user.getOrganization());
            pstmt.setString(8, user.getAvatarUrl());
            pstmt.setTimestamp(9, user.getCfRegisteredAt() != null ? Timestamp.valueOf(user.getCfRegisteredAt()) : null);
            pstmt.setBoolean(10, user.getIsActive() != null ? user.getIsActive() : true);
            pstmt.setTimestamp(11, user.getLastCrawledAt() != null ? Timestamp.valueOf(user.getLastCrawledAt()) : null);
            pstmt.setInt(12, user.getTotalSolved() != null ? user.getTotalSolved() : 0);
            pstmt.setString(13, user.getNotes());

            // Update part
            pstmt.setString(14, user.getDisplayName());
            pstmt.setInt(15, user.getRating() != null ? user.getRating() : 0);
            pstmt.setInt(16, user.getMaxRating() != null ? user.getMaxRating() : 0);
            pstmt.setString(17, user.getRankTitle());
            pstmt.setString(18, user.getCountry());
            pstmt.setString(19, user.getOrganization());
            pstmt.setString(20, user.getAvatarUrl());
            pstmt.setTimestamp(21, user.getCfRegisteredAt() != null ? Timestamp.valueOf(user.getCfRegisteredAt()) : null);
            pstmt.setBoolean(22, user.getIsActive() != null ? user.getIsActive() : true);
            pstmt.setTimestamp(23, user.getLastCrawledAt() != null ? Timestamp.valueOf(user.getLastCrawledAt()) : null);
            pstmt.setInt(24, user.getTotalSolved() != null ? user.getTotalSolved() : 0);
            pstmt.setString(25, user.getNotes());

            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getLong(1));
                }
            }
        }
        return user;
    }

    public List<CodeforcesUser> findAll() throws SQLException {
        List<CodeforcesUser> users = new ArrayList<>();
        String sql = "SELECT * FROM cf_users";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    public Optional<CodeforcesUser> findByHandle(String handle) throws SQLException {
        String sql = "SELECT * FROM cf_users WHERE handle = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, handle);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM cf_users WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        }
    }

    private CodeforcesUser mapResultSetToUser(ResultSet rs) throws SQLException {
        return CodeforcesUser.builder()
                .id(rs.getLong("id"))
                .handle(rs.getString("handle"))
                .displayName(rs.getString("display_name"))
                .rating(rs.getInt("rating"))
                .maxRating(rs.getInt("max_rating"))
                .rankTitle(rs.getString("rank_title"))
                .country(rs.getString("country"))
                .organization(rs.getString("organization"))
                .avatarUrl(rs.getString("avatar_url"))
                .cfRegisteredAt(rs.getTimestamp("cf_registered_at") != null ? rs.getTimestamp("cf_registered_at").toLocalDateTime() : null)
                .isActive(rs.getBoolean("is_active"))
                .lastCrawledAt(rs.getTimestamp("last_crawled_at") != null ? rs.getTimestamp("last_crawled_at").toLocalDateTime() : null)
                .totalSolved(rs.getInt("total_solved"))
                .notes(rs.getString("notes"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                .build();
    }
}
