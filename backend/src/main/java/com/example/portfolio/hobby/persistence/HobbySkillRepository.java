package com.example.portfolio.hobby.persistence;

import com.example.portfolio.hobby.domain.Hobby;
import com.example.portfolio.hobby.domain.HobbySkill;
import com.example.portfolio.skills.domain.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HobbySkillRepository extends JpaRepository<HobbySkill, Long> {
    List<HobbySkill> findAllByHobby(Hobby hobby);

    Optional<HobbySkill> findBySkill(Skill skill);
}
