package com.example.portfolio.projects.persistence;

import com.example.portfolio.projects.domain.ProjectSkill;
import com.example.portfolio.skills.domain.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectSkillRepository extends JpaRepository<ProjectSkill, Long> {
    List<ProjectSkill> findAllBySkill(Skill skill);
}
