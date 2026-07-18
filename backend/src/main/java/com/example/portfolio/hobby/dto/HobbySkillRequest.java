package com.example.portfolio.hobby.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HobbySkillRequest {
    // Present on update to match an existing link; null means "create new"
    private Long id;

    // The existing global skill to link — required, this feature does not create Skills
    @NotNull
    private Long skillId;

    @NotNull
    private BigDecimal usagePercentage;
}
