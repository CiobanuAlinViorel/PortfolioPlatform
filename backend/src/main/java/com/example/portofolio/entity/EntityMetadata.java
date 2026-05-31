package com.example.portofolio.entity;

import com.example.portofolio.entity.base.BaseEntity;
import com.example.portofolio.entity.enums.EntityType;
import com.example.portofolio.entity.enums.ImportanceLevel;
import com.example.portofolio.entity.Icon;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import io.hypersistence.utils.hibernate.type.json.JsonType;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized Metadata for all entities
 */
@Entity
@Table(name = "entity_metadata",
        uniqueConstraints = @UniqueConstraint(name = "uk_metadata_entity",
                columnNames = {"entity_type", "entity_id"}),
        indexes = {
                @Index(name = "idx_metadata_priority", columnList = "importance, featured")
        })
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = true,onlyExplicitlyIncluded = true)
public class EntityMetadata extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 20)
    private EntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Builder.Default
    @Column(name = "primary_color", length = 7)
    private String primaryColor = "#3B82F6";

    @Builder.Default
    @Column(name = "secondary_color", length = 7)
    private String secondaryColor = "#1E40AF";

    @Column(length = 100)
    private String gradient;

    @Column(name = "glow_color", length = 7)
    private String glowColor;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ImportanceLevel importance = ImportanceLevel.MEDIUM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_id")
    private Icon icon;

    @Builder.Default
    private Boolean featured = false;
}