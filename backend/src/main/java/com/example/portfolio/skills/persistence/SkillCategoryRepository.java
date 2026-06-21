package com.example.portfolio.skills.persistence;

import com.example.portfolio.skills.domain.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillCategoryRepository extends JpaRepository<SkillCategory, Long> {
}
