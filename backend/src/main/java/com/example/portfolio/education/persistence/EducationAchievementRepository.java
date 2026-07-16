package com.example.portfolio.education.persistence;

import com.example.portfolio.education.domain.Education;
import com.example.portfolio.education.domain.EducationAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EducationAchievementRepository extends JpaRepository<EducationAchievement, Long> {
    List<EducationAchievement> findAllByEducation(Education education);
}
