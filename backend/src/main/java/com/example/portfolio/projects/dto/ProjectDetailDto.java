package com.example.portfolio.projects.dto;

import com.example.portfolio.hobby.domain.ComplexityLevel;
import com.example.portfolio.projects.domain.ProjectStatus;
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
public class ProjectDetailDto {
    private Long id;
    private String title;
    private String description;
    private String longDescription;
    private ProjectStatus status;
    private ComplexityLevel complexity;
    private Integer year;
    private List<String> tags;
    private String githubUrl;
    private String demoUrl;
    private LocalDate completionDate;
    private String categoryName;
    private Double developmentTime;
    private List<ProjectFeatureDto> features;
    private List<ProjectChallengeDto> challenges;
    private List<ProjectImageDto> images;
    private List<SkillInProjectDto> skills;
    private ProjectMetricsDto metrics;
}
