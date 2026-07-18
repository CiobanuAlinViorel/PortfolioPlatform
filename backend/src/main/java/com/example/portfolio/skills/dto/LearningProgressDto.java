package com.example.portfolio.skills.dto;

import com.example.portfolio.skills.domain.LearningStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningProgressDto {
    private Long id;
    private String name;
    private LearningStatus status;
    private LocalDateTime startDate;
    private LocalDateTime completionDate;
    private Integer progressPercentage;
    private BigDecimal timeSpentHours;
    private String estimatedCompletion;
    private String description;
}
