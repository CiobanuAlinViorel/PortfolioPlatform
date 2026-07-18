package com.example.portfolio.experience.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerProjectDto {
    private Long id;
    private BigDecimal contributionPercentage;
    private ProjectInVolunteerDto project;
}
