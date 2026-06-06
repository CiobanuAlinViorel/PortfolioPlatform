package com.example.portfolio.education.domain;

import com.example.portfolio.achievement.domain.Achievement;
import com.example.portfolio.shared.base.BaseEntity;
import jakarta.persistence.*;
//import jakarta.persistence.Index;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "education_achievement")
@EqualsAndHashCode(callSuper = true,onlyExplicitlyIncluded = true)
public class EducationAchievement extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "education_id", nullable = false)
    private Education education;

    @OneToOne
    @JoinColumn(name="achievement_id", nullable = false)
    private Achievement achievement;

    @ManyToOne
    @JoinColumn(name = "course_id")

    private Course course ;

    @Column(name="topic", nullable = false)
    private String topic;

    @Column(name="grade", nullable = false)
    private BigDecimal grade;

    @Column(name="max_grade", nullable = false)
    private BigDecimal maxGrade;

    @Column(name = "credits", nullable = false)
    private Integer credits;

    @Column(name="teacher_or_supervisor", nullable = false)
    private String teacherOrSupervisor;

    @Column(name = "institution_name", nullable = false)
    private String institutionName;

    @Column(name = "semester", nullable = false)
    private Integer semester;

    @Column(name="academic_year", nullable = false)
    private Integer academicYear;


}
