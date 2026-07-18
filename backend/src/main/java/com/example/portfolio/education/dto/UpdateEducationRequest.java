package com.example.portfolio.education.dto;

import com.example.portfolio.education.domain.EducationLevel;
import com.example.portfolio.education.domain.EducationStatus;
import jakarta.validation.Valid;
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
public class UpdateEducationRequest {
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

    // Diffed by id against existing courses: matched ids updated, unmatched ids removed, id-less entries created
    @Valid
    private List<CourseRequest> courses;
    @Valid
    private List<EducationAchievementRequest> achievements;
}
