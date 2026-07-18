package com.example.portfolio.projects.persistence;

import com.example.portfolio.projects.domain.Project;
import com.example.portfolio.projects.domain.ProjectAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectAchievementRepository extends JpaRepository<ProjectAchievement, Long> {
    List<ProjectAchievement> findAllByProject(Project project);
}
