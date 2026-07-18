package com.example.portfolio.skills.dto;

import com.example.portfolio.skills.domain.ProficiencyLevel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreateSkillRequest {
    @NotBlank
    private String name;

    // Category — link by id or find-or-create by name
    private Long categoryId;
    private String categoryName;

    private ProficiencyLevel proficiency;
    private Integer level;
    private BigDecimal yearsOfExperience;
    private String description;
    private LocalDate lastUsedDate;
    private Boolean hasCertification;
    private Boolean learning;
    private Integer sortOrder;

    @Valid
    private List<SkillTagRequest> tags;
    @Valid
    private List<LearningProgressRequest> learningProgresses;
}
