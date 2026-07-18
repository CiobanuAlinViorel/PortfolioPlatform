package com.example.portfolio.skills.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSkillCategoryRequest {
    @NotBlank
    private String name;
    private String description;
    private Integer sortOrder;
}
