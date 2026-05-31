package com.example.portofolio.entity;

import com.example.portofolio.entity.base.BaseEntity;
import com.example.portofolio.entity.enums.IconType;
import jakarta.persistence.*;
import lombok.*;

/**
 * Centralized system of icons
 */
@Entity
@Table(name = "icon", indexes = {
        @Index(name = "idx_icon_type_category", columnList = "type, category")
})
@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = true,onlyExplicitlyIncluded = true)
public class Icon extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IconType type;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private IconCategory iconCategory;
}
