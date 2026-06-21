package com.example.portfolio.skills.dto;

import com.example.portfolio.hobby.domain.ComplexityLevel;
import com.example.portfolio.projects.domain.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectInSkillDto {
    private Long id;
    private String title;
    private String description;
    private ProjectStatus status;
    private ComplexityLevel complexity;
    private Integer year;
    private String githubUrl;
    private String demoUrl;
}
