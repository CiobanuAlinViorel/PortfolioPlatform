package com.example.portfolio.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectFeatureDto {
    private Long id;
    private String title;
    private String description;
    private LocalDate implementationDate;
    private BigDecimal developmentTimeHours;
    private Integer sortOrder;
}
