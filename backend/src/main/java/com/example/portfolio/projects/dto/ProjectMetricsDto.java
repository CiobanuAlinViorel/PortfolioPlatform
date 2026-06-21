package com.example.portfolio.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMetricsDto {
    private Long usersCount;
    private String performanceScore;
    private String codeQualityScore;
    private Long linesOfCode;
    private Integer commitsCount;
    private BigDecimal testCoveragePercentage;
}
