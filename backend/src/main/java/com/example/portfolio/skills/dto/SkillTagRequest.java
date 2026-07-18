package com.example.portfolio.skills.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SkillTagRequest {
    // Present on update to match an existing tag; null means "create new"
    private Long id;

    @NotBlank
    private String tagName;
}
