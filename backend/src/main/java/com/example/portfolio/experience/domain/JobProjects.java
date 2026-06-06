package com.example.portfolio.experience.domain;

import com.example.portfolio.projects.domain.Project;
import com.example.portfolio.shared.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "job_projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class JobProjects extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "job_experience_id", nullable = false)
    private JobExperience jobExperience;

    @OneToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
}
