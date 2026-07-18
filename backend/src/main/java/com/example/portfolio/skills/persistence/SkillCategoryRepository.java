package com.example.portfolio.skills.persistence;

import com.example.portfolio.skills.domain.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SkillCategoryRepository extends JpaRepository<SkillCategory, Long> {
    Optional<SkillCategory> findByName(String name);
}
