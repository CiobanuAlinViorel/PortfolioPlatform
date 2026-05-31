package com.example.portofolio.entity;

import com.example.portofolio.entity.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Used technologies
 */
@Entity
@Table(name = "technology", indexes = {
        @Index(name = "idx_technology_category_popularity", columnList = "category_id, popularity_score"),
        @Index(name = "idx_technology_trending", columnList = "is_trending, popularity_score")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = true,onlyExplicitlyIncluded = true)
public class Technology extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private TechnologyCategory technologyCategory;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "official_website", length = 300)
    private String officialWebsite;

    @Column(name = "documentation_url", length = 300)
    private String documentationUrl;

    @Column(length = 50)
    private String version;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Builder.Default
    @Column(name = "is_trending")
    private Boolean trending = false;

    @Min(0) @Max(100)
    @Builder.Default
    @Column(name = "popularity_score")
    private Integer popularityScore = 0;

    // Relationships
    @OneToMany(mappedBy = "technology", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private Set<TechnologyFeature> features = new HashSet<>();

    // COLLECTION METHODS
    public void addFeature(TechnologyFeature feature) {
        this.features.add(feature);
    }

    public void removeFeature(TechnologyFeature feature) {
        this.features.remove(feature);
    }
}