package com.example.portfolio.projects.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProjectFeatureRequest {
    private Long id; // null = create new, present = update existing
    private String title;
    private String description;
    private LocalDate implementationDate;
    private BigDecimal developmentTimeHours;
    private Integer sortOrder;
}
