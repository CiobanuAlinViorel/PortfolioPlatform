package com.example.portfolio.experience.persistence;

import com.example.portfolio.experience.domain.JobExperience;
import com.example.portfolio.experience.domain.JobProjects;
import com.example.portfolio.projects.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobProjectsRepository extends JpaRepository<JobProjects, Long> {

    boolean existsByProject(Project project);

    Optional<JobProjects> findByJobExperienceAndProject(JobExperience jobExperience, Project project);
}
