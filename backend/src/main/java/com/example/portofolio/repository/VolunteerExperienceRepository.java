package com.example.portofolio.repository;

import com.example.portofolio.entity.VolunteerExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VolunteerExperienceRepository extends JpaRepository<VolunteerExperience, Long> {

    // Basic queries
    List<VolunteerExperience> findByPersonalId(Long personalId);

    // Optimized queries
    @Query("SELECT ve FROM VolunteerExperience ve " +
            "LEFT JOIN FETCH ve.responsibilities " +
            "WHERE ve.personal.id = :personalId")
    List<VolunteerExperience> findByPersonalIdWithResponsibilities(@Param("personalId") Long personalId);


    // Statistics
    @Query("SELECT COUNT(ve) FROM VolunteerExperience ve WHERE ve.personal.id = :personalId")
    Long countByPersonalId(@Param("personalId") Long personalId);

    // Search
    @Query("SELECT ve FROM VolunteerExperience ve " +
            "WHERE ve.personal.id = :personalId " +
            "AND (LOWER(ve.organization) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(ve.role) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(ve.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<VolunteerExperience> findByPersonalIdAndSearchTerm(@Param("personalId") Long personalId,
                                                            @Param("search") String search);

}