package com.example.portofolio.repository;

import com.example.portofolio.entity.Education;
import com.example.portofolio.entity.enums.EducationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EducationRepository extends JpaRepository<Education, Long> {

    // Basic queries
    List<Education> findByPersonalId(Long personalId);

    List<Education> findByPersonalIdAndStatus(Long personalId, EducationStatus status);

    List<Education> findByPersonalIdOrderByStartDateDesc(Long personalId);

    // Optimized queries
    @Query("SELECT e FROM Education e " +
            "LEFT JOIN FETCH e.courses c " +
            "LEFT JOIN FETCH e.achievements " +
            "WHERE e.personal.id = :personalId")
    List<Education> findByPersonalIdWithCoursesAndAchievements(@Param("personalId") Long personalId);

    @Query("SELECT e FROM Education e WHERE e.personal.id = :personalId AND e.status = :status")
    Education findCurrentByPersonalId(@Param("personalId") Long personalId);


    // Statistics
    @Query("SELECT COUNT(e) FROM Education e WHERE e.personal.id = :personalId")
    Long countByPersonalId(@Param("personalId") Long personalId);

    // Search
    @Query("SELECT e FROM Education e " +
            "WHERE e.personal.id = :personalId " +
            "AND (LOWER(e.institution) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(e.degree) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(e.fieldOfStudy) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Education> findByPersonalIdAndSearchTerm(@Param("personalId") Long personalId,
                                                  @Param("search") String search);

    @Query("SELECT COUNT(DISTINCT YEAR(e.startDate)) FROM Education e WHERE e.personal.id = :personalId")
    Integer countAcademicYearsByPersonalId(@Param("personalId") Long personalId);

}