package com.example.portfolio.experience.domain;

import com.example.portfolio.shared.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Volunteer responsibilities
 */
@Entity
@Table(name = "volunteer_responsibility")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = true,onlyExplicitlyIncluded = true)
public class VolunteerResponsibility extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_experience_id", nullable = false)
    private VolunteerExperience volunteerExperience;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "impact_level", length = 20)
    private ImpactLevel impactLevel = ImpactLevel.MEDIUM;

    @Builder.Default
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}