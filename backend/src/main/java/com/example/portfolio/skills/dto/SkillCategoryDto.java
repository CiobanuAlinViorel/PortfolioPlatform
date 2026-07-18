package com.example.portfolio.skills.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillCategoryDto {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private Integer sortOrder;
}
