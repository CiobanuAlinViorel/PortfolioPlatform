package com.example.portfolio.education.dto;

import com.example.portfolio.education.domain.EducationLevel;
import com.example.portfolio.education.domain.EducationStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateEducationRequest {
    @NotNull
    private EducationLevel level;
    @NotBlank
    private String institution;
    private String degree;
    @NotBlank
    private String fieldOfStudy;
    private String location;
    @NotNull
    private LocalDate startDate;
    private LocalDate endDate;
    @NotNull
    private EducationStatus status;
    private String gpa;
    private String description;

    @Valid
    private List<CourseRequest> courses;
    @Valid
    private List<EducationAchievementRequest> achievements;
}
