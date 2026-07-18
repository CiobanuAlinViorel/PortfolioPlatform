package com.example.portfolio.education.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
    private Long id;
    private String title;
    private String description;
    private String grade;
    private BigDecimal credits;
    private String semester;
    private Integer year;
    private Boolean relevant;
    private List<CourseProjectDto> projects;
}
