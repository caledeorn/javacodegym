package com.cfanalyzer.dao;

import com.cfanalyzer.config.DatabaseConfig;
import com.cfanalyzer.model.AnalysisResult;

import java.sql.*;
import java.util.Optional;

public class AnalysisResultDao {

    public void save(AnalysisResult result) throws SQLException {
        String sql = "INSERT INTO analysis_results (submission_id, user_id, data_structures_used, ds_primary, ds_complexity_score, algorithms_used, algo_primary, algo_category, algo_complexity_score, time_complexity, space_complexity, ai_probability, ai_indicators, ai_explanation, code_quality_score, readability_score, originality_score, difficulty_estimate, overall_summary, raw_ai_response, model_used, tokens_used, analysis_duration_ms, analyzed_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE data_structures_used=?, ds_primary=?, ds_complexity_score=?, algorithms_used=?, algo_primary=?, algo_category=?, algo_complexity_score=?, time_complexity=?, space_complexity=?, ai_probability=?, ai_indicators=?, ai_explanation=?, code_quality_score=?, readability_score=?, originality_score=?, difficulty_estimate=?, overall_summary=?, raw_ai_response=?, model_used=?, tokens_used=?, analysis_duration_ms=?, analyzed_at=?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            int idx = 1;
            pstmt.setLong(idx++, result.getSubmissionId());
            pstmt.setLong(idx++, result.getUserId());
            pstmt.setString(idx++, result.getDataStructuresUsed());
            pstmt.setString(idx++, result.getDsPrimary());
            pstmt.setInt(idx++, result.getDsComplexityScore() != null ? result.getDsComplexityScore() : 0);
            pstmt.setString(idx++, result.getAlgorithmsUsed());
            pstmt.setString(idx++, result.getAlgoPrimary());
            pstmt.setString(idx++, result.getAlgoCategory());
            pstmt.setInt(idx++, result.getAlgoComplexityScore() != null ? result.getAlgoComplexityScore() : 0);
            pstmt.setString(idx++, result.getTimeComplexity());
            pstmt.setString(idx++, result.getSpaceComplexity());
            pstmt.setDouble(idx++, result.getAiProbability() != null ? result.getAiProbability() : 0.0);
            pstmt.setString(idx++, result.getAiIndicators());
            pstmt.setString(idx++, result.getAiExplanation());
            pstmt.setInt(idx++, result.getCodeQualityScore() != null ? result.getCodeQualityScore() : 0);
            pstmt.setInt(idx++, result.getReadabilityScore() != null ? result.getReadabilityScore() : 0);
            pstmt.setInt(idx++, result.getOriginalityScore() != null ? result.getOriginalityScore() : 0);
            pstmt.setString(idx++, result.getDifficultyEstimate());
            pstmt.setString(idx++, result.getOverallSummary());
            pstmt.setString(idx++, result.getRawAiResponse());
            pstmt.setString(idx++, result.getModelUsed());
            pstmt.setInt(idx++, result.getTokensUsed() != null ? result.getTokensUsed() : 0);
            pstmt.setInt(idx++, result.getAnalysisDurationMs() != null ? result.getAnalysisDurationMs() : 0);
            pstmt.setTimestamp(idx++, Timestamp.valueOf(result.getAnalyzedAt()));

            // Update part
            pstmt.setString(idx++, result.getDataStructuresUsed());
            pstmt.setString(idx++, result.getDsPrimary());
            pstmt.setInt(idx++, result.getDsComplexityScore() != null ? result.getDsComplexityScore() : 0);
            pstmt.setString(idx++, result.getAlgorithmsUsed());
            pstmt.setString(idx++, result.getAlgoPrimary());
            pstmt.setString(idx++, result.getAlgoCategory());
            pstmt.setInt(idx++, result.getAlgoComplexityScore() != null ? result.getAlgoComplexityScore() : 0);
            pstmt.setString(idx++, result.getTimeComplexity());
            pstmt.setString(idx++, result.getSpaceComplexity());
            pstmt.setDouble(idx++, result.getAiProbability() != null ? result.getAiProbability() : 0.0);
            pstmt.setString(idx++, result.getAiIndicators());
            pstmt.setString(idx++, result.getAiExplanation());
            pstmt.setInt(idx++, result.getCodeQualityScore() != null ? result.getCodeQualityScore() : 0);
            pstmt.setInt(idx++, result.getReadabilityScore() != null ? result.getReadabilityScore() : 0);
            pstmt.setInt(idx++, result.getOriginalityScore() != null ? result.getOriginalityScore() : 0);
            pstmt.setString(idx++, result.getDifficultyEstimate());
            pstmt.setString(idx++, result.getOverallSummary());
            pstmt.setString(idx++, result.getRawAiResponse());
            pstmt.setString(idx++, result.getModelUsed());
            pstmt.setInt(idx++, result.getTokensUsed() != null ? result.getTokensUsed() : 0);
            pstmt.setInt(idx++, result.getAnalysisDurationMs() != null ? result.getAnalysisDurationMs() : 0);
            pstmt.setTimestamp(idx++, Timestamp.valueOf(result.getAnalyzedAt()));

            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    result.setId(generatedKeys.getLong(1));
                }
            }
        }
    }

    public Optional<AnalysisResult> findBySubmissionId(Long submissionId) throws SQLException {
        String sql = "SELECT * FROM analysis_results WHERE submission_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, submissionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAnalysisResult(rs));
                }
            }
        }
        return Optional.empty();
    }

    private AnalysisResult mapResultSetToAnalysisResult(ResultSet rs) throws SQLException {
        return AnalysisResult.builder()
                .id(rs.getLong("id"))
                .submissionId(rs.getLong("submission_id"))
                .userId(rs.getLong("user_id"))
                .dataStructuresUsed(rs.getString("data_structures_used"))
                .dsPrimary(rs.getString("ds_primary"))
                .dsComplexityScore(rs.getInt("ds_complexity_score"))
                .algorithmsUsed(rs.getString("algorithms_used"))
                .algoPrimary(rs.getString("algo_primary"))
                .algoCategory(rs.getString("algo_category"))
                .algoComplexityScore(rs.getInt("algo_complexity_score"))
                .timeComplexity(rs.getString("time_complexity"))
                .spaceComplexity(rs.getString("space_complexity"))
                .aiProbability(rs.getDouble("ai_probability"))
                .aiIndicators(rs.getString("ai_indicators"))
                .aiExplanation(rs.getString("ai_explanation"))
                .codeQualityScore(rs.getInt("code_quality_score"))
                .readabilityScore(rs.getInt("readability_score"))
                .originalityScore(rs.getInt("originality_score"))
                .difficultyEstimate(rs.getString("difficulty_estimate"))
                .overallSummary(rs.getString("overall_summary"))
                .rawAiResponse(rs.getString("raw_ai_response"))
                .modelUsed(rs.getString("model_used"))
                .tokensUsed(rs.getInt("tokens_used"))
                .analysisDurationMs(rs.getInt("analysis_duration_ms"))
                .analyzedAt(rs.getTimestamp("analyzed_at").toLocalDateTime())
                .build();
    }
}
