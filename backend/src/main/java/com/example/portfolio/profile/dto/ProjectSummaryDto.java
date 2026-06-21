package com.example.portfolio.profile.dto;

import com.example.portfolio.projects.domain.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSummaryDto {
    private Long id;
    private String title;
    private String description;
    private ProjectStatus status;
    private String demoUrl;
    private String githubUrl;
    private Integer year;
}
