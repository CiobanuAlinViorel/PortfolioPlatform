package com.example.portofolio.entity;

import com.example.portofolio.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * Courses from an education level
 */
@Entity
@Table(name = "course", indexes = {
        @Index(name = "idx_course_education_relevant", columnList = "education_id, is_relevant"),
        @Index(name = "idx_course_timeline", columnList = "year, semester")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = true,onlyExplicitlyIncluded = true)
public class Course extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "education_id", nullable = false)
    private Education education;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 10)
    private String grade;

    @Column(precision = 4, scale = 1)
    private BigDecimal credits;

    @Column(length = 50)
    private String semester;

    private Integer year;

    @Builder.Default
    @Column(name = "is_relevant")
    private Boolean relevant = false;

    // Relationships
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private Set<CourseProject> courseProjects = new HashSet<>();

    // COLLECTION METHODS
    public void addCourseProject(CourseProject courseProject) {
        this.courseProjects.add(courseProject);
    }

    public void removeCourseProject(CourseProject courseProject) {
        this.courseProjects.remove(courseProject);
    }
}