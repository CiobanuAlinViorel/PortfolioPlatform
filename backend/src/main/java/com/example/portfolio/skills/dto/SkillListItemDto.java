package com.example.portfolio.skills.dto;

import com.example.portfolio.skills.domain.ProficiencyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillListItemDto {
    private Long id;
    private String name;
    private String categoryName;
    private ProficiencyLevel proficiency;
    private Integer level;
    private BigDecimal yearsOfExperience;
    private String description;
    private LocalDate lastUsedDate;
    private Boolean hasCertification;
    private Boolean learning;
}
