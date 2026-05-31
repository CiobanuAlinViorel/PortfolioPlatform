package com.example.portofolio.entity;

import com.example.portofolio.entity.base.BaseEntity;
import com.example.portofolio.entity.enums.DifficultyLevel;
import jakarta.persistence.*;
import lombok.*;

/**
 * Project challenges
 */
@Entity
@Table(name = "project_challenge", indexes = {
        @Index(name = "idx_project_challenge_difficulty", columnList = "project_id, difficulty")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = true,onlyExplicitlyIncluded = true)
public class ProjectChallenge extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String solution;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DifficultyLevel difficulty = DifficultyLevel.MEDIUM;
}