package com.example.portfolio.certificate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateDto {
    private Long id;
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
    private Boolean verified;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
