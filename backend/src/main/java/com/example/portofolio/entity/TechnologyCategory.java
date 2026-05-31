package com.example.portofolio.entity;

import com.example.portofolio.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Tech categories
 */
@Entity
@Table(name = "technology_category", indexes = {
        @Index(name = "idx_tech_category_hierarchy", columnList = "parent_id, sort_order")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = true,onlyExplicitlyIncluded = true)
public class TechnologyCategory extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_id")
    private Icon icon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private TechnologyCategory parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private Set<TechnologyCategory> children = new HashSet<>();

    @OneToMany(mappedBy = "technologyCategory", fetch = FetchType.LAZY)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private Set<Technology> technologies = new HashSet<>();

    @Builder.Default
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    // COLLECTION METHODS
    public void addChild(TechnologyCategory technologyCategory) {
       children.add(technologyCategory);
    }

    public void removeChild(TechnologyCategory technologyCategory) {
        children.remove(technologyCategory);
    }

    public void addTechnology(Technology technology) {
        technologies.add(technology);
    }

    public void removeTechnology(Technology technology) {
        technologies.remove(technology);
    }
}