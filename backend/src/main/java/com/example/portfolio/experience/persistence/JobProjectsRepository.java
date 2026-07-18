package com.example.portfolio.experience.persistence;

import com.example.portfolio.experience.domain.JobExperience;
import com.example.portfolio.experience.domain.JobProjects;
import com.example.portfolio.projects.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobProjectsRepository extends JpaRepository<JobProjects, Long> {

    Optional<JobProjects> findByProject(Project project);

    Optional<JobProjects> findByJobExperienceAndProject(JobExperience jobExperience, Project project);

    List<JobProjects> findAllByJobExperience(JobExperience jobExperience);
}
