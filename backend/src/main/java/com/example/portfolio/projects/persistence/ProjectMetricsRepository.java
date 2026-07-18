package com.example.portfolio.projects.persistence;

import com.example.portfolio.projects.domain.Project;
import com.example.portfolio.projects.domain.ProjectMetrics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectMetricsRepository extends JpaRepository<ProjectMetrics, Long> {
    Optional<ProjectMetrics> findByProject(Project project);
}
