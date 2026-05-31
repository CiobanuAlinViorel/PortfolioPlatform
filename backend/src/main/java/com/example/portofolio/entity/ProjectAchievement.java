package com.example.portofolio.entity;

import com.example.portofolio.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_achievement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = true,onlyExplicitlyIncluded = true)
public class ProjectAchievement extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;

    @ManyToOne
    @JoinColumn(name="project_id", nullable = false)
    private Project project;

    @OneToOne
    @JoinColumn(name="project_challenge_id")
    private ProjectChallenge projectChallenge;

    @OneToOne
    @JoinColumn(name="project_feature_id")
    private ProjectFeature projectFeature;


    @Column(name = "impact_summary", nullable = false)
    private String impactSummary;

    @Column(name="demo_url", nullable = false)
    private String demoUrl;

    @Column(name="metric_before")
    private String metricBefore;

    @Column(name="metric_after")
    private String metricAfter;

}
