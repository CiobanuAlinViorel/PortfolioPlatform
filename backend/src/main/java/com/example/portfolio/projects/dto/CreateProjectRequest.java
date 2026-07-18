package com.example.portfolio.projects.dto;

import com.example.portfolio.hobby.domain.ComplexityLevel;
import com.example.portfolio.projects.domain.ProjectStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateProjectRequest {
    @NotBlank
    private String title;
    private String description;
    private String longDescription;
    @NotNull
    private ProjectStatus status;
    @NotNull
    private ComplexityLevel complexity;
    private String demoUrl;
    private String githubUrl;
    private Integer year;
    private LocalDate completionDate;
    private Double developmentTime;
    private Integer sortOrder;
    private List<String> tags;

    // Category — link by id or find-or-create by name
    private Long categoryId;
    private String categoryName;

    private List<ProjectFeatureRequest> features;
    private List<ProjectChallengeRequest> challenges;
    @Valid
    private List<ProjectMediaRequest> media;
    private ProjectMetricsRequest metrics; // if null, defaults are applied
    private List<ProjectAchievementRequest> achievements;
    private List<ProjectSkillRequest> skills;
}
