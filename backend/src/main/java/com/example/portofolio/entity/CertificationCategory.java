package com.example.portofolio.entity;

import com.example.portofolio.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Certification categories
 */
@Entity
@Table(name = "certification_category", indexes = {
        @Index(name = "idx_cert_category_industry", columnList = "industry, sort_order")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = true,onlyExplicitlyIncluded = true)
public class CertificationCategory extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_id")
    private Icon icon;

    @Column(length = 100)
    private String industry;

    @Builder.Default
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @OneToMany(mappedBy = "certificationCategory", cascade = CascadeType.DETACH, fetch = FetchType.LAZY)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private Set<Certificate> certificates = new HashSet<>();

    // COLLECTION METHODS
    public void addCertificate(Certificate c) {
        this.certificates.add(c);
    }

    public void removeCertificate(Certificate c) {
        this.certificates.remove(c);
    }
}
