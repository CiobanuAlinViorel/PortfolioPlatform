package com.example.portfolio.education.dto;

import com.example.portfolio.education.domain.EducationLevel;
import com.example.portfolio.education.domain.EducationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EducationDetailDto {
    private Long id;
    private EducationLevel level;
    private String institution;
    private String degree;
    private String fieldOfStudy;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private EducationStatus status;
    private String gpa;
    private String description;
    private List<CourseDto> courses;
    private List<AchievementInEducationDto> achievements;
}
