package com.cfanalyzer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {
    private Long id;
    private Long submissionId;
    private Long userId;
    
    private String dataStructuresUsed; // JSON string
    private String dsPrimary;
    private Integer dsComplexityScore;
    
    private String algorithmsUsed; // JSON string
    private String algoPrimary;
    private String algoCategory;
    private Integer algoComplexityScore;
    
    private String timeComplexity;
    private String spaceComplexity;
    
    private Double aiProbability;
    private String aiIndicators; // JSON string
    private String aiExplanation;
    
    private Integer codeQualityScore;
    private Integer readabilityScore;
    private Integer originalityScore;
    
    private String difficultyEstimate;
    private String overallSummary;
    
    private String rawAiResponse;
    private String modelUsed;
    private Integer tokensUsed;
    private Integer analysisDurationMs;
    private LocalDateTime analyzedAt;
}
