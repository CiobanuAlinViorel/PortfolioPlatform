package com.example.portfolio.skills.domain;

import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.shared.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Skills
 */
@Entity
@Table(name = "skill", indexes = {
        @Index(name = "idx_skill_personal_category", columnList = "personal_id, category_id"),
        @Index(name = "idx_skill_proficiency", columnList = "proficiency, level")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = true,onlyExplicitlyIncluded = true)
public class Skill extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    private Profile profile;


    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private SkillCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProficiencyLevel proficiency;

    @Min(1) @Max(100)
    @Builder.Default
    private Integer level = 1;

    @Column(name = "years_of_experience", precision = 4, scale = 1)
    @Builder.Default
    private BigDecimal yearsOfExperience = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "last_used_date")
    private LocalDate lastUsedDate;

    @Builder.Default
    @Column(name = "has_certification")
    private Boolean hasCertification = false;

    @Builder.Default
    @Column(name = "is_learning")
    private Boolean learning = false;

    @Column(name = "sort_order")
    private Integer sortOrder;

    // Relationships
    @OneToMany(mappedBy = "skill", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private Set<SkillTag> tags = new HashSet<>();

    @OneToMany(mappedBy = "skill", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private Set<LearningProgress> learningProgress = new HashSet<>();

    // COLLECTIONS METHODS

    public void addLearningProgress(LearningProgress learningProgress) {
        this.learningProgress.add(learningProgress);
    }

    public void removeLearningProgress(LearningProgress learningProgress) {
        this.learningProgress.remove(learningProgress);
    }

    public void addSkillTag(SkillTag skillTag) {
        this.tags.add(skillTag);
    }

    public void removeSkillTag(SkillTag skillTag) {
        this.tags.remove(skillTag);
    }
}