package com.example.portofolio.entity;

import com.example.portofolio.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "job_experience")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class JobExperience extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id",  nullable = false)
    private Personal personal;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name ="company_name", nullable = false)
    private String companyName;

    @Column(name="role", nullable = false)
    private String role;

    @OneToMany(mappedBy = "jobExperience", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private Set<JobProjects> projects =  new HashSet<>();

    // COLLECTION METHODS
    public void addProject(JobProjects project) {
        projects.add(project);
    }
    public void removeProject(JobProjects project) {
        projects.remove(project);
    }
}
