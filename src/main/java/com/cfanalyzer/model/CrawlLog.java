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
public class CrawlLog {
    private Long id;
    private Long userId;
    private String crawlType;
    private String status;
    private Integer submissionsFound;
    private Integer submissionsNew;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Integer durationMs;
}
