package com.example.portfolio.hobby.persistence;

import com.example.portfolio.hobby.domain.Hobby;
import com.example.portfolio.hobby.domain.HobbySkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HobbySkillRepository extends JpaRepository<HobbySkill, Long> {
    List<HobbySkill> findAllByHobby(Hobby hobby);
}
