package com.example.portfolio.achievement.dto;

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
public class AchievementDto {
    private Long id;
    private String title;
    private String description;
    private AchievementType achievementType;
    private LocalDate achievementDate;
    private RecognitionLevel recognitionLevel;
    private String recognizedBy;
    private String proofUrl;
    private Boolean isFeatured;
    private Integer sortOrder;
}
