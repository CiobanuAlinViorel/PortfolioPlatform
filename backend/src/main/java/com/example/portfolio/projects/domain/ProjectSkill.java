package com.example.portfolio.projects.domain;

import com.example.portfolio.skills.domain.Skill;
import com.example.portfolio.shared.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "project_skill", uniqueConstraints = {
        @UniqueConstraint(name = "uk_project_skill", columnNames = {"project_id", "skill_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProjectSkill extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(name = "usage", nullable = false)
    private BigDecimal usage;
}
