package com.example.portfolio.education.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseProjectRequest {
    // Present on update to match an existing link; null means "create new"
    private Long id;

    // The existing global project to link — required, this feature does not create Projects
    @NotNull
    private Long projectId;

    private String grade;
    private Integer contributionPercentage;
}
