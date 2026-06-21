package com.example.portfolio.projects.dto;

import com.example.portfolio.skills.domain.ProficiencyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillInProjectDto {
    private Long id;
    private String name;
    private ProficiencyLevel proficiency;
    private BigDecimal usage;
}
