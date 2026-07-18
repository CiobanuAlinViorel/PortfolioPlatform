package com.example.portfolio.education.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CourseRequest {
    // Present on update to match an existing course; null means "create new"
    private Long id;

    @NotBlank
    private String title;
    private String description;
    private String grade;
    private BigDecimal credits;
    private String semester;
    private Integer year;
    private Boolean relevant;

    // Diffed by id against existing links: matched ids updated, unmatched ids removed, id-less entries created
    @Valid
    private List<CourseProjectRequest> projects;
}
