package com.example.portfolio.education.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseProjectDto {
    private Long id;
    private String grade;
    private Integer contributionPercentage;
    private ProjectInCourseDto project;
}
