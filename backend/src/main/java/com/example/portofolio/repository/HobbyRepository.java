package com.example.portofolio.repository;

import com.example.portofolio.entity.Hobby;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HobbyRepository extends JpaRepository<Hobby, Long> {

    // Basic queries
    List<Hobby> findByPersonalId(Long personalId);

    // Activity level queries
    @Query("SELECT h FROM Hobby h " +
            "WHERE h.personal.id = :personalId " +
            "AND h.activityLevel IN ('FREQUENT', 'DAILY') " +
            "ORDER BY h.activityLevel DESC, h.yearsActive DESC")
    List<Hobby> findActiveHobbiesByPersonalId(@Param("personalId") Long personalId);

    // Statistics
    @Query("SELECT COUNT(h) FROM Hobby h WHERE h.personal.id = :personalId")
    Long countByPersonalId(@Param("personalId") Long personalId);

    // Search
    @Query("SELECT h FROM Hobby h " +
            "WHERE h.personal.id = :personalId " +
            "AND (LOWER(h.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(h.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Hobby> findByPersonalIdAndSearchTerm(@Param("personalId") Long personalId,
                                              @Param("search") String search);
}