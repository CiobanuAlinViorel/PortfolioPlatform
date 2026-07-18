package com.example.portfolio.projects.domain;

import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.shared.base.BaseEntity;
import com.example.portfolio.shared.converter.StringListConverter;
import com.example.portfolio.hobby.domain.ComplexityLevel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Projects
 */
@Entity
@Table(name = "project")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = true)
public class  Project extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    private Profile profile;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "long_description", columnDefinition = "TEXT")
    private String longDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ComplexityLevel complexity;

    @Column(name = "demo_url", length = 500)
    private String demoUrl;

    @Column(name = "github_url", length = 500)
    private String githubUrl;

    private Integer year;

    @Column(name = "completion_date")
    private LocalDate completionDate;
    @Column(name = "development_time")
    private Double developmentTime;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Convert(converter = StringListConverter.class)
    @Column(nullable = false, length = 200)
    private List<String> tags = new ArrayList<>();

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ProjectCategory projectCategory;

    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL)
    private ProjectMetrics metrics;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private Set<ProjectFeature> features = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private Set<ProjectChallenge> challenges = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private Set<ProjectMedia> media = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private Set<ProjectSkill> skills = new HashSet<>();


    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private Set<ProjectAchievement> achievements = new HashSet<>();

    // COLLECTIONS METHODS

    public void addFeature(ProjectFeature feature){
        this.features.add(feature);
    }

    public void removeFeature(ProjectFeature feature){
        this.features.remove(feature);
    }

    public void addChallenge(ProjectChallenge challenge){
        this.challenges.add(challenge);
    }

    public void removeChallenge(ProjectChallenge challenge){
        this.challenges.remove(challenge);
    }

    public void addMedia(ProjectMedia item) {
        this.media.add(item);
    }

    public void removeMedia(ProjectMedia item) {
        this.media.remove(item);
    }

    public void addAchievement(ProjectAchievement achievement){
        this.achievements.add(achievement);
    }

    public void removeAchievement(ProjectAchievement achievement){
        this.achievements.remove(achievement);
    }

    public void addSkill(ProjectSkill skill){
        this.skills.add(skill);
    }

    public void removeSkill(ProjectSkill skill){
        this.skills.remove(skill);
    }
}