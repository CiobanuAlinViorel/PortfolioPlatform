package com.example.portfolio.certificate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificationCategoryDto {
    private Long id;
    private String name;
    private String description;
    private String industry;
    private Integer sortOrder;
}
