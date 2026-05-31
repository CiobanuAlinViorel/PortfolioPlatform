package com.example.portofolio.entity;

import com.example.portofolio.entity.base.BaseEntity;
import com.example.portofolio.entity.enums.ProjectStatus;
import com.example.portofolio.entity.enums.ComplexityLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.mapping.Array;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Projects
 */
@Entity
@Table(name = "project", indexes = {
        @Index(name = "idx_project_personal_status", columnList = "personal_id, status"),
        @Index(name = "idx_project_category_complexity", columnList = "category, complexity"),
        @Index(name = "idx_project_timeline", columnList = "year, completion_date")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = true)
public class Project extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    private Personal personal;

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
    private Set<ProjectImage> images = new HashSet<>();

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

    public void addImage(ProjectImage image){
        this.images.add(image);
    }

    public void removeImage(ProjectImage image){
        this.images.remove(image);
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