package com.example.portfolio.hobby.domain;

import com.example.portfolio.shared.base.BaseEntity;
import com.example.portfolio.skills.domain.Skill;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "hobby_skill")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = true,onlyExplicitlyIncluded = true)
public class HobbySkill extends BaseEntity {
    @OneToOne
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @ManyToOne
    @JoinColumn(name = "hobby_id", nullable = false)
    private Hobby hobby;

    @Column(name = "usage_percentage",  nullable = false)
    private BigDecimal usagePercentage;

}
