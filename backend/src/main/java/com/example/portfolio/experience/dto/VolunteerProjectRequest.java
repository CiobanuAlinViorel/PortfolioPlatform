package com.example.portfolio.experience.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VolunteerProjectRequest {
    // Present on update to match an existing link; null means "create new"
    private Long id;

    // The existing global project to link — required, this feature does not create Projects
    @NotNull
    private Long projectId;

    @NotNull
    private BigDecimal contributionPercentage;
}
