package com.example.portofolio.entity;

import com.example.portofolio.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Personality examples
 */
@Entity
@Table(name = "personality_example", indexes = {
        @Index(name = "idx_personality_example_order", columnList = "trait_id, sort_order")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = true,onlyExplicitlyIncluded = true)
public class PersonalityExample extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trait_id", nullable = false)
    private PersonalityTrait trait;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(length = 150)
    private String context;

    @Builder.Default
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}