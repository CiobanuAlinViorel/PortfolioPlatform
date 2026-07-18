package com.example.portfolio.skills.dto;

import lombok.Data;

@Data
public class UpdateSkillCategoryRequest {
    private String name;
    private String description;
    private Integer sortOrder;
}
