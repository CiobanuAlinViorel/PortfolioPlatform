package com.example.portfolio.experience.dto;

import com.example.portfolio.experience.domain.ImpactLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerResponsibilityDto {
    private Long id;
    private String description;
    private ImpactLevel impactLevel;
    private Integer sortOrder;
}
