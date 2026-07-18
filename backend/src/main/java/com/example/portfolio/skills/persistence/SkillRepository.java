package com.example.portfolio.skills.persistence;

import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.skills.domain.Skill;
import com.example.portfolio.skills.domain.SkillCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    @Query("SELECT s FROM Skill s WHERE s.profile = :profile AND s.sortOrder IS NOT NULL ORDER BY s.sortOrder ASC")
    List<Skill> findTopByProfile(@Param("profile") Profile profile, Pageable pageable);

    List<Skill> findAllByProfileOrderBySortOrderAscNameAsc(Profile profile);

    Optional<Skill> findByProfileAndName(Profile profile, String name);

    List<Skill> findAllByCategory(SkillCategory category);
}
