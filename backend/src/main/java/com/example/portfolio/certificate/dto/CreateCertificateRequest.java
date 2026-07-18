package com.example.portfolio.certificate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCertificateRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String provider;
    private String credentialId;
    private String certificateUrl;
    @NotNull
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private Boolean hasExpiry;
    private String description;
    private String score;
    private Integer relevanceScore;

    // Category — link by id or find-or-create by name; one of the two is required
    private Long categoryId;
    private String categoryName;
}
