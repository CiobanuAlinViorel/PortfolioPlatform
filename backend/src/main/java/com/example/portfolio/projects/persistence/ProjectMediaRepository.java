package com.example.portfolio.projects.persistence;

import com.example.portfolio.projects.domain.Project;
import com.example.portfolio.projects.domain.ProjectMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectMediaRepository extends JpaRepository<ProjectMedia, Long> {
    List<ProjectMedia> findAllByProjectOrderBySortOrderAsc(Project project);
}
