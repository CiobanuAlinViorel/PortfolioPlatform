package com.example.portfolio.projects.dto;

import com.example.portfolio.skills.domain.LearningStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LearningProgressRequest {
    private String name;
    private LearningStatus status;
    private LocalDateTime startDate;
    private LocalDateTime completionDate;
    private Integer progressPercentage;
    private BigDecimal timeSpentHours;
    private String estimatedCompletion;
    private String description;
}
