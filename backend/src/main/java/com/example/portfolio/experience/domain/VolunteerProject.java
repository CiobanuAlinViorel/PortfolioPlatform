package com.example.portfolio.experience.domain;

import com.example.portfolio.projects.domain.Project;
import com.example.portfolio.shared.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "volunteer_project")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class VolunteerProject extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "volunteer_experience_id", nullable = false)
    private VolunteerExperience volunteerExperience;

    @OneToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "contribution_percentage", nullable = false)
    private BigDecimal contributionPercentage;
}
