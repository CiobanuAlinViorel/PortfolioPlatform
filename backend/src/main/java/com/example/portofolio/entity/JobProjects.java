package com.example.portofolio.entity;

import com.example.portofolio.entity.base.BaseEntity;
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
