package com.example.portfolio.education.domain;

import com.example.portfolio.projects.domain.Project;
import com.example.portfolio.shared.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

/**
 * Link between a course and a project
 */
@Entity
@Table(name = "course_project")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = true,onlyExplicitlyIncluded = true)
public class CourseProject extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @OneToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(length = 10)
    private String grade;

    @Min(0) @Max(100)
    @Builder.Default
    @Column(name = "contribution_percentage")
    private Integer contributionPercentage = 100;
}
