package com.example.portfolio.education.dto;

import com.example.portfolio.projects.domain.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectInCourseDto {
    private Long id;
    private String title;
    private String description;
    private ProjectStatus status;
    private Integer year;
    private List<String> tags;
    private String githubUrl;
    private String demoUrl;
}
