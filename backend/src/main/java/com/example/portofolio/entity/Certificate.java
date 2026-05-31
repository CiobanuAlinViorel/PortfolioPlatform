package com.example.portofolio.entity;

import com.example.portofolio.entity.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDate;

/**
 * Certifications of a person
 */
@Entity
@Table(name = "certificate", indexes = {
        @Index(name = "idx_certificate_personal_category", columnList = "personal_id, category_id"),
        @Index(name = "idx_certificate_provider_date", columnList = "provider, issue_date"),
        @Index(name = "idx_certificate_verified_relevance", columnList = "is_verified, relevance_score"),
        @Index(name = "idx_certificate_expiry", columnList = "expiry_date, has_expiry")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = true,onlyExplicitlyIncluded = true)
public class Certificate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    private Personal personal;

    @Column(nullable = false, length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CertificationCategory certificationCategory;

    @Column(nullable = false, length = 150)
    private String provider;

    @Column(name = "credential_id", unique = true, length = 100)
    private String credentialId;

    @Column(name = "certificate_url", length = 500)
    private String certificateUrl;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Builder.Default
    @Column(name = "has_expiry")
    private Boolean hasExpiry = false;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String score;

    @Min(0)
    @Max(100)
    @Builder.Default
    @Column(name = "relevance_score")
    private Integer relevanceScore = 50;

    @Builder.Default
    @Column(name = "is_verified")
    private Boolean verified = false;
    
}