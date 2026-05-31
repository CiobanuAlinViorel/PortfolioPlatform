package com.example.portofolio.entity;

import com.example.portofolio.entity.base.BaseEntity;
import com.example.portofolio.entity.enums.ImpactLevel;
import com.example.portofolio.entity.Interest;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Recent discoveries
 */
@Entity
@Table(name = "recent_discovery", indexes = {
        @Index(name = "idx_discovery_interest_date", columnList = "interest_id, discovery_date"),
        @Index(name = "idx_discovery_impact_date", columnList = "impact_level, discovery_date")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = true)
public class RecentDiscovery extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_id", nullable = false)
    private Interest interest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hobby_id")
    private Hobby hobby;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(name = "discovery_date")
    private LocalDate discoveryDate = LocalDate.now();

    @Column(length = 200)
    private String source;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "impact_level", length = 20)
    private ImpactLevel impactLevel = ImpactLevel.MEDIUM;
}