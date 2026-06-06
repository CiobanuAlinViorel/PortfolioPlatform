package com.example.portfolio.education.domain;

import com.example.portfolio.profile.domain.Personal;
import com.example.portfolio.shared.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Education level for a person
 */
@Entity
@Table(name = "education")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor(access =  AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = true,onlyExplicitlyIncluded = true)
public class Education extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    private Personal personal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EducationLevel level;

    @Column(nullable = false, length = 200)
    private String institution;

    @Column(length = 200)
    private String degree;

    @Column(name = "field_of_study", nullable = false, length = 200)
    private String fieldOfStudy;

    @Column(length = 150)
    private String location;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EducationStatus status;

    @Column(length = 10)
    private String gpa;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Relationships
    @OneToMany(mappedBy = "education", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private Set<Course> courses = new HashSet<>();

    @OneToMany(mappedBy = "education", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private Set<EducationAchievement> achievements = new HashSet<>();


    // COLLECTIONS METHODS

    public void addCourse(Course course) {
        courses.add(course);
    }

    public void removeCourse(Course course) {
        courses.remove(course);
    }

    public void addAchievement(EducationAchievement achievement) {
        achievements.add(achievement);
    }

    public void removeAchievement(EducationAchievement achievement) {
        achievements.remove(achievement);
    }
}
