package com.example.portofolio.entity;

import com.example.portofolio.entity.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

/**
 * Link between a course and a project
 */
@Entity
@Table(name = "course_project", indexes = {
        @Index(name = "idx_course_project_unique", columnList = "course_id, project_id", unique = true)
})
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
