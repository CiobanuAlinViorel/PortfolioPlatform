package com.example.portofolio.entity;

import com.example.portofolio.entity.base.BaseEntity;
import com.example.portofolio.entity.enums.GoalType;
import com.example.portofolio.entity.enums.PriorityLevel;
import com.example.portofolio.entity.enums.GoalStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDate;

/**
 * Future goals for somebody
 */
@Entity
@Table(name = "future_goal", indexes = {
        @Index(name = "idx_future_goal_personal_type", columnList = "personal_id, goal_type"),
        @Index(name = "idx_future_goal_target_priority", columnList = "target_date, priority"),
        @Index(name = "idx_future_goal_progress", columnList = "status, progress_percentage")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = true,onlyExplicitlyIncluded = true)
public class FutureGoal extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    private Personal personal;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false, length = 20)
    private GoalType goalType;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PriorityLevel priority = PriorityLevel.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private GoalStatus status = GoalStatus.PLANNING;

    @Min(0) @Max(100)
    @Builder.Default
    @Column(name = "progress_percentage")
    private Integer progressPercentage = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id")
    private Skill skill; // optional, if the objective is linked with a skill

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_id")
    private Icon icon;
}