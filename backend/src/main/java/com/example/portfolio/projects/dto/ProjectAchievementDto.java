package com.example.portfolio.projects.dto;

import com.example.portfolio.achievement.domain.AchievementType;
import com.example.portfolio.achievement.domain.RecognitionLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAchievementDto {
    private Long id;
    private Long achievementId;
    private String title;
    private String description;
    private AchievementType achievementType;
    private LocalDate achievementDate;
    private RecognitionLevel recognitionLevel;
    private String recognizedBy;
    private String proofUrl;
    private Boolean isFeatured;
    private String impactSummary;
    private String demoUrl;
    private String metricBefore;
    private String metricAfter;
}
