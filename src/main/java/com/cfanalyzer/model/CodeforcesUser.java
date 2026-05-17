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
public class CodeforcesUser {
    private Long id;
    private String handle;
    private String displayName;
    private Integer rating;
    private Integer maxRating;
    private String rankTitle;
    private String country;
    private String organization;
    private String avatarUrl;
    private LocalDateTime cfRegisteredAt;
    private Boolean isActive;
    private LocalDateTime lastCrawledAt;
    private Integer totalSolved;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
