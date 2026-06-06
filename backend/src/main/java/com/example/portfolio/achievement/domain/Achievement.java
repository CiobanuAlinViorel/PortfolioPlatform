package com.example.portfolio.achievement.domain;

import com.example.portfolio.profile.domain.Personal;
import com.example.portfolio.shared.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Achievements class
 */
@Entity
@Table(name = "achievement")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = true,onlyExplicitlyIncluded = true)
public class Achievement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    private Personal personal;


    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "achievement_type", nullable = false, length = 20)
    private AchievementType achievementType;

    @Column(name = "achievement_date", nullable = false)
    private LocalDate achievementDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "recognition_level", length = 20)
    private RecognitionLevel recognitionLevel = RecognitionLevel.LOCAL;


    @Column(name="recognized_by", length = 50)
    private String recognizedBy;

    @Column(name="proof_url")
    private String proofUrl;

    @Column(name="is_featured")
    private Boolean isFeatured;

    @Column(name="sort_order")
    private Integer sortOrder;
}
