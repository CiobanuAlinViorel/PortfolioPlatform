package com.example.portofolio.entity;

import com.example.portofolio.entity.base.BaseEntity;
import com.example.portofolio.entity.enums.HighlightType;
import com.example.portofolio.entity.enums.EntityType;
import com.example.portofolio.entity.enums.PriorityLevel;
import jakarta.persistence.*;
import lombok.*;

/**
 * Highlights
 */
@Entity
@Table(name = "highlight", indexes = {
        @Index(name = "idx_highlight_personal_type", columnList = "personal_id, highlight_type"),
        @Index(name = "idx_highlight_priority_type", columnList = "priority_level, highlight_type")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = true,onlyExplicitlyIncluded = true)
public class Highlight extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    private Personal personal;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "highlight_type", nullable = false, length = 20)
    private HighlightType highlightType;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "priority_level", length = 20)
    private PriorityLevel priorityLevel = PriorityLevel.MEDIUM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_id")
    private Icon icon;
}