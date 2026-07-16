package com.example.portfolio.education.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementInEducationDto {
    private Long achievementId;
    private String achievementTitle;
    private Long courseId;
    private String courseTitle;
    private String topic;
    private BigDecimal grade;
    private BigDecimal maxGrade;
    private Integer credits;
    private String teacherOrSupervisor;
    private String institutionName;
    private Integer semester;
    private Integer academicYear;
}
