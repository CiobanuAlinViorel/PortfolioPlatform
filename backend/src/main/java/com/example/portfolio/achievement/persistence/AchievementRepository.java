package com.example.portfolio.achievement.persistence;

import com.example.portfolio.achievement.domain.Achievement;
import com.example.portfolio.profile.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface AchievementRepository extends JpaRepository<Achievement, Long>, JpaSpecificationExecutor<Achievement> {
    List<Achievement> findByProfileOrderBySortOrderAscAchievementDateDesc(Profile profile);
}
