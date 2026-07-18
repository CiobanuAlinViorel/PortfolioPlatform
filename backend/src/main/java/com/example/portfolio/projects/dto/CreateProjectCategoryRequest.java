package com.example.portfolio.projects.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateProjectCategoryRequest {
    @NotBlank
    private String name;
}
