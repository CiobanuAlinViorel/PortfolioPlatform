package com.example.portfolio.achievement.dto;

import com.example.portfolio.achievement.domain.AchievementType;
import com.example.portfolio.achievement.domain.RecognitionLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAchievementRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotNull
    private AchievementType achievementType;
    @NotNull
    private LocalDate achievementDate;
    private RecognitionLevel recognitionLevel;
    private String recognizedBy;
    private String proofUrl;
    private Boolean isFeatured;
    private Integer sortOrder;
}
