package com.example.portofolio.entity;

import com.example.portofolio.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Skill tags
 */
@Entity
@Table(name = "skill_tag", indexes = {
        @Index(name = "idx_skill_tag_unique", columnList = "skill_id, tag_name", unique = true)
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = true,onlyExplicitlyIncluded = true)
public class SkillTag extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(name = "tag_name", nullable = false, length = 50)
    private String tagName;
}