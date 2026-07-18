package com.example.portfolio.projects.persistence;

import com.example.portfolio.projects.domain.ProjectCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectCategoryRepository extends JpaRepository<ProjectCategory, Long> {
    Optional<ProjectCategory> findByName(String name);
}
