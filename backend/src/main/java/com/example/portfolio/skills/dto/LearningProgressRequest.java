package com.example.portfolio.skills.dto;

import com.example.portfolio.skills.domain.LearningStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LearningProgressRequest {
    // Present on update to match an existing entry; null means "create new"
    private Long id;

    @NotBlank
    private String name;
    private LearningStatus status;
    private LocalDateTime startDate;
    private LocalDateTime completionDate;
    private Integer progressPercentage;
    private BigDecimal timeSpentHours;
    private String estimatedCompletion;
    private String description;
}
