package com.example.portfolio.projects.dto;

import com.example.portfolio.achievement.domain.AchievementType;
import com.example.portfolio.achievement.domain.RecognitionLevel;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectAchievementRequest {
    private Long id; // ProjectAchievement id — null = create new, present = update existing

    // Link existing global Achievement (takes precedence over creation fields below)
    private Long achievementId;

    // Achievement creation/update fields
    private String title;
    private String description;
    private AchievementType achievementType;
    private LocalDate achievementDate;
    private RecognitionLevel recognitionLevel;
    private String recognizedBy;
    private String proofUrl;
    private Boolean isFeatured;
    private Integer sortOrder;

    // ProjectAchievement-specific fields
    private String impactSummary;
    private String demoUrl;
    private String metricBefore;
    private String metricAfter;
}
