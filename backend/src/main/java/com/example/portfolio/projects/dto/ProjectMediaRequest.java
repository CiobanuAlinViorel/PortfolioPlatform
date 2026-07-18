package com.example.portfolio.projects.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProjectMediaRequest {
    private Long id;         // null = create new, present = update existing
    private String title;
    private String description;
    private String imageUrl; // null when a file is supplied via multipart "files" — resolved before DB save
    private String altText;
    private Integer sortOrder;

    @NotNull(message = "primary is required for every media item")
    private Boolean primary;
}
