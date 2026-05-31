package com.example.portofolio.repository;

import com.example.portofolio.entity.Skill;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    // Basic queries
    List<Skill> findByPersonalId(Long personalId);

    // Optimized queries cu JOIN FETCH
    @Query("SELECT s FROM Skill s " +
            "LEFT JOIN FETCH s.category " +
            "LEFT JOIN FETCH s.tags " +
            "WHERE s.personal.id = :personalId")
    List<Skill> findByPersonalIdWithCategoryAndTags(@Param("personalId") Long personalId);


    // Statistics
    @Query("SELECT COUNT(s) FROM Skill s WHERE s.personal.id = :personalId")
    Long countByPersonalId(@Param("personalId") Long personalId);

    @Query("SELECT AVG(s.level) FROM Skill s WHERE s.personal.id = :personalId")
    Double findAverageSkillLevelByPersonalId(@Param("personalId") Long personalId);

    // Search and filtering
    @Query("SELECT s FROM Skill s " +
            "WHERE s.personal.id = :personalId " +
            "AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(s.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Skill> findByPersonalIdAndNameOrDescriptionContaining(@Param("personalId") Long personalId,
                                                               @Param("search") String search);

    /**
     * Find the skills by category name
     */
    @Query("SELECT s FROM Skill s " +
            "LEFT JOIN FETCH s.category sc " +
            "WHERE s.personal.id = :personalId " +
            "AND LOWER(sc.name) LIKE LOWER(CONCAT('%', :categoryName, '%')) " +
            "ORDER BY s.level DESC")
    List<Skill> findByPersonalIdAndCategoryName(@Param("personalId") Long personalId,
                                                @Param("categoryName") String categoryName);


    /**
     * Find the date of the oldest skill for calculating the programming years
     */
    @Query("SELECT MIN(s.createdAt) FROM Skill s WHERE s.personal.id = :personalId")
    LocalDate findOldestSkillDate(@Param("personalId") Long personalId);

}