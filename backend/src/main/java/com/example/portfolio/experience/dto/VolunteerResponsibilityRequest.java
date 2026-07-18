package com.example.portfolio.experience.dto;

import com.example.portfolio.experience.domain.ImpactLevel;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VolunteerResponsibilityRequest {
    // Present on update to match an existing responsibility; null means "create new"
    private Long id;

    @NotBlank
    private String description;
    private ImpactLevel impactLevel;
    private Integer sortOrder;
}
