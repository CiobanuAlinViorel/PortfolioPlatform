package com.example.portofolio.entity;

import com.example.portofolio.entity.base.BaseEntity;
import com.example.portofolio.entity.enums.ImportanceLevel;
import jakarta.persistence.*;
import lombok.*;

/**
 * Personal values
 */
@Entity
@Table(name = "personal_value", indexes = {
        @Index(name = "idx_personal_value_importance", columnList = "personal_id, importance_level"),
        @Index(name = "idx_personal_value_order", columnList = "personal_id, sort_order")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = true,onlyExplicitlyIncluded = true)
public class PersonalValue extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    private Personal personal;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "importance_level", length = 20)
    private ImportanceLevel importanceLevel = ImportanceLevel.MEDIUM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_id")
    private Icon icon;

    @Builder.Default
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}