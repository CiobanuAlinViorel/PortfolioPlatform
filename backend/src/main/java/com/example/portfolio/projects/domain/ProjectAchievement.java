package com.example.portfolio.projects.domain;

import com.example.portfolio.achievement.domain.Achievement;
import com.example.portfolio.shared.base.BaseEntity;
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


    @Column(name = "impact_summary")
    private String impactSummary;

    @Column(name="demo_url")
    private String demoUrl;

    @Column(name="metric_before")
    private String metricBefore;

    @Column(name="metric_after")
    private String metricAfter;

}
