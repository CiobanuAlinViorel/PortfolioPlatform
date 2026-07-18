package com.example.portfolio.projects.persistence;

import com.example.portfolio.projects.domain.Project;
import com.example.portfolio.projects.domain.ProjectFeature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectFeatureRepository extends JpaRepository<ProjectFeature, Long> {
    List<ProjectFeature> findAllByProjectOrderBySortOrderAsc(Project project);
}
