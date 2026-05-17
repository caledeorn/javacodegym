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
public class UserEvaluation {
    private Long id;
    private Long userId;
    
    private String dsSkillLevel;
    private Double dsSkillScore;
    private String mostUsedDs;
    private Integer dsDiversityCount;
    
    private String algoSkillLevel;
    private Double algoSkillScore;
    private String mostUsedAlgorithms;
    private Integer algoDiversityCount;
    private String strongestCategory;
    private String weakestCategory;
    
    private String aiUsageLevel;
    private Double avgAiProbability;
    private Integer highAiSubmissionCount;
    private String aiUsageTrend;
    
    private Integer totalAnalyzed;
    private Double avgCodeQuality;
    private Double avgDifficulty;
    private String problemSolvingStyle;
    private String overallAssessment;
    private Double overallScore;
    private Double rankingPercentile;
    
    private LocalDateTime evaluatedAt;
    private LocalDateTime updatedAt;
}
