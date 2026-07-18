package com.example.portfolio.projects.dto;

import com.example.portfolio.skills.domain.ProficiencyLevel;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ProjectSkillRequest {
    // Link existing skill — if provided, all skill creation fields below are ignored
    private Long skillId;

    // Skill creation fields (used when skillId is null)
    private String skillName;
    private Long skillCategoryId;
    private String skillCategoryName;
    private ProficiencyLevel proficiency;
    private Integer level;
    private BigDecimal yearsOfExperience;
    private String skillDescription;
    private LocalDate lastUsedDate;
    private Boolean hasCertification;
    private Boolean learning;
    private Integer sortOrder;
    private List<LearningProgressRequest> learningProgresses;

    // Required — percentage/ratio of how much this skill was used in the project
    private BigDecimal usage;
}
