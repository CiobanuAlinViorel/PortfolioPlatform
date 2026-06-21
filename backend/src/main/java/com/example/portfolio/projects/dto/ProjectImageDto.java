package com.example.portfolio.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectImageDto {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private String altText;
    private Integer sortOrder;
    private Boolean primary;
}
