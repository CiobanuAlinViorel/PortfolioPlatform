package com.example.portfolio.skills.persistence;

import com.example.portfolio.skills.domain.Skill;
import com.example.portfolio.skills.domain.SkillTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkillTagRepository extends JpaRepository<SkillTag, Long> {
    List<SkillTag> findAllBySkill(Skill skill);
}
