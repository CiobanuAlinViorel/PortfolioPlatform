package com.example.portfolio.skills.domain;

import com.example.portfolio.shared.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Skill categories
 */
@Entity
@Table(name = "skill_category", indexes = {
        @Index(name = "idx_skill_category_hierarchy", columnList = "parent_id, sort_order")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = true,onlyExplicitlyIncluded = true)
public class SkillCategory extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private SkillCategory parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private Set<SkillCategory> children = new HashSet<>();

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private Set<Skill> skills = new HashSet<>();

    @Builder.Default
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    // COLLECTIONS METHODS

    public void addSkill(Skill skill) {
        skills.add(skill);
    }

    public void removeSkill(Skill skill) {
        skills.remove(skill);
    }

    public void addChild(SkillCategory skillCategory) {
         children.add(skillCategory);
    }

    public void  removeChild(SkillCategory skillCategory) {
        children.remove(skillCategory);
    }

}