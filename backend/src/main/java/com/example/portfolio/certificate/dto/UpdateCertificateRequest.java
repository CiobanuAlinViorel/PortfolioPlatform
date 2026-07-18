package com.example.portfolio.certificate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCertificateRequest {
    private String name;
    private String provider;
    private String credentialId;
    private String certificateUrl;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private Boolean hasExpiry;
    private String description;
    private String score;
    private Integer relevanceScore;

    // Category — link by id or find-or-create by name; sending both renames the shared category
    private Long categoryId;
    private String categoryName;
}
