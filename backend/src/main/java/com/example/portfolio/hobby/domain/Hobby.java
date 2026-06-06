package com.example.portfolio.hobby.domain;

import com.example.portfolio.profile.domain.Personal;
import com.example.portfolio.shared.base.BaseEntity;
import com.example.portfolio.experience.domain.ImpactLevel;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Hobbies of a person
 */
@Entity
@Table(name = "hobby")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = true)
public class Hobby extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    private Personal personal;

    @Column(name = "why_interested", columnDefinition = "TEXT")
    private String whyInterested;

    @Column(name = "name", columnDefinition = "TEXT")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private HobbyCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level")
    private ActivityLevel activityLevel;

    @Column(name = "years_active")
    private Long yearsActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "complexity_level")
    private ComplexityLevel complexityLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "impact_on_work")
    private ImpactLevel impactOnWork;

    @Column(name = "favorite_aspect", columnDefinition = "TEXT")
    private String favoriteAspect;

    // Relationships

    @OneToMany(mappedBy = "hobby", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private Set<HobbySkill> hobbySkills = new HashSet<>();

    // INTERACTION WITH THE COLLECTION FROM ENTITY



    public void addHobbySkill(HobbySkill hobbySkill) {
        this.hobbySkills.add(hobbySkill);
    }

    public void removeHobbySkill(HobbySkill hobbySkill) {
        this.hobbySkills.remove(hobbySkill);
    }
}