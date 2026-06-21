package com.example.portfolio.skills.dto;

import com.example.portfolio.skills.domain.ProficiencyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillDetailDto {
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
    private List<String> tags;
    private List<ProjectInSkillDto> projects;
}
