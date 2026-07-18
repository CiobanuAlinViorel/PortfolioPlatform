package com.example.portfolio.projects.persistence;

import com.example.portfolio.projects.domain.Project;
import com.example.portfolio.projects.domain.ProjectChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectChallengeRepository extends JpaRepository<ProjectChallenge, Long> {
    List<ProjectChallenge> findAllByProject(Project project);
}
