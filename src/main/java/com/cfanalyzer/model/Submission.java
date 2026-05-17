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
public class Submission {
    private Long id;
    private Long cfSubmissionId;
    private Long userId;
    private String problemId;
    private String problemName;
    private Integer contestId;
    private String problemIndex;
    private String verdict;
    private String language;
    private Integer timeConsumedMs;
    private Integer memoryConsumedKb;
    private String codeContent;
    private Integer codeLength;
    private LocalDateTime cfSubmittedAt;
    private LocalDateTime crawledAt;
    private Boolean isAnalyzed;
}
