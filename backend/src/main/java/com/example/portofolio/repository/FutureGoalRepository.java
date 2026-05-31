package com.example.portofolio.repository;

import com.example.portofolio.entity.FutureGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FutureGoalRepository extends JpaRepository<FutureGoal, Long> {

    // Basic queries
    List<FutureGoal> findByPersonalId(Long personalId);

    // Statistics
    @Query("SELECT COUNT(fg) FROM FutureGoal fg WHERE fg.personal.id = :personalId")
    Long countByPersonalId(@Param("personalId") Long personalId);

    // Search
    @Query("SELECT fg FROM FutureGoal fg " +
            "WHERE fg.personal.id = :personalId " +
            "AND (LOWER(fg.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(fg.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<FutureGoal> findByPersonalIdAndSearchTerm(@Param("personalId") Long personalId,
                                                   @Param("search") String search);

    @Query("SELECT fg FROM FutureGoal fg WHERE fg.personal.id = :personalId " +
            "ORDER BY fg.priority DESC, fg.targetDate ASC")
    List<FutureGoal> findByPersonalIdOrderByPriorityDesc(@Param("personalId") Long personalId);

}