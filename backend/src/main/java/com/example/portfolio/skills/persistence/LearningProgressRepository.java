package com.example.portfolio.skills.persistence;

import com.example.portfolio.skills.domain.LearningProgress;
import com.example.portfolio.skills.domain.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearningProgressRepository extends JpaRepository<LearningProgress, Long> {
    List<LearningProgress> findAllBySkill(Skill skill);
}
