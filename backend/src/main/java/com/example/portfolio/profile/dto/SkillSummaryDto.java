package com.example.portfolio.profile.dto;

import com.example.portfolio.skills.domain.ProficiencyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillSummaryDto {
    private Long id;
    private String name;
    private String categoryName;
    private ProficiencyLevel proficiency;
    private Integer level;
}
