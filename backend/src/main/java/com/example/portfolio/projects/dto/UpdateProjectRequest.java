package com.example.portfolio.projects.dto;

import com.example.portfolio.hobby.domain.ComplexityLevel;
import com.example.portfolio.projects.domain.ProjectStatus;
import jakarta.validation.Valid;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateProjectRequest {
    private String title;
    private String description;
    private String longDescription;
    private ProjectStatus status;
    private ComplexityLevel complexity;
    private String demoUrl;
    private String githubUrl;
    private Integer year;
    private LocalDate completionDate;
    private Double developmentTime;
    private Integer sortOrder;
    private List<String> tags;

    // If provided, replaces existing category (link by id or find-or-create by name)
    private Long categoryId;
    private String categoryName;

    // If null, the sub-collection is left unchanged; if provided, it is merged
    private List<ProjectFeatureRequest> features;
    private List<ProjectChallengeRequest> challenges;
    @Valid
    private List<ProjectMediaRequest> media;
    private ProjectMetricsRequest metrics;
    private List<ProjectAchievementRequest> achievements;
    private List<ProjectSkillRequest> skills;
}
