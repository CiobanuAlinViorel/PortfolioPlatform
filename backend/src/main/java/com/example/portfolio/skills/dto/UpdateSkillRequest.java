package com.example.portfolio.skills.dto;

import com.example.portfolio.skills.domain.ProficiencyLevel;
import jakarta.validation.Valid;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateSkillRequest {
    private String name;

    // Category — link by id or find-or-create by name; sending both renames the shared category
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

    // Diffed by id against existing rows: matched ids are updated, unmatched ids removed, id-less entries created
    @Valid
    private List<SkillTagRequest> tags;
    @Valid
    private List<LearningProgressRequest> learningProgresses;
}
