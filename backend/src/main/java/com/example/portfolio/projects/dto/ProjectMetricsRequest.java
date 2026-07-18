package com.example.portfolio.projects.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProjectMetricsRequest {
    private Long usersCount;
    private String performanceScore;
    private String codeQualityScore;
    private Long linesOfCode;
    private Integer commitsCount;
    private BigDecimal testCoveragePercentage;
}
