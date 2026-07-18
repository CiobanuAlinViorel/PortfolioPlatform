package com.example.portfolio.education.dto;

import com.example.portfolio.achievement.domain.AchievementType;
import com.example.portfolio.achievement.domain.RecognitionLevel;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EducationAchievementRequest {
    private Long id; // EducationAchievement id — null = create new, present = update existing

    // Link existing global Achievement (takes precedence over creation fields below)
    private Long achievementId;

    // Achievement creation/update fields (used when achievementId is null)
    private String achievementTitle;
    private String achievementDescription;
    private AchievementType achievementType;
    private LocalDate achievementDate;
    private RecognitionLevel recognitionLevel;
    private String recognizedBy;
    private String proofUrl;
    private Boolean isFeatured;
    private Integer sortOrder;

    // Optional link to one of this education's courses
    private Long courseId;

    // EducationAchievement-specific fields
    private String topic;
    private BigDecimal grade;
    private BigDecimal maxGrade;
    private Integer credits;
    private String teacherOrSupervisor;
    private String institutionName;
    private Integer semester;
    private Integer academicYear;
}
