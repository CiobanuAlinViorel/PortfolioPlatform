package com.example.portofolio.entity;

import com.example.portofolio.entity.base.BaseEntity;
import com.example.portofolio.entity.enums.AchievementContext;
import com.example.portofolio.entity.enums.AchievementType;
import com.example.portofolio.entity.enums.RecognitionLevel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Achievements class
 */
@Entity
@Table(name = "achievement", indexes = {
        @Index(name = "idx_achievement_personal_type", columnList = "personal_id, achievement_type"),
        @Index(name = "idx_achievement_date_level", columnList = "achievement_date, recognition_level")
})
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

    @Enumerated(EnumType.STRING)
    private AchievementContext achievementContext;

    @Column(name="recognized_by", length = 50)
    private String recognizedBy;

    @Column(name="proof_url")
    private String proofUrl;

    @Column(name="is_featured")
    private Boolean isFeatured;

    @Column(name="sort_order")
    private Integer sortOrder;
}
