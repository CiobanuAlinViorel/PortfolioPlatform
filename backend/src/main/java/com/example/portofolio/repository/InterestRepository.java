package com.example.portofolio.repository;

import com.example.portofolio.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterestRepository extends JpaRepository<Interest, Long> {

    // Basic queries
    List<Interest> findByPersonalId(Long personalId);
    

    // Statistics
    @Query("SELECT COUNT(i) FROM Interest i WHERE i.personal.id = :personalId")
    Long countByPersonalId(@Param("personalId") Long personalId);

    // Search
    @Query("SELECT i FROM Interest i " +
            "WHERE i.personal.id = :personalId " +
            "AND (LOWER(i.description) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(i.whyInterested) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Interest> findByPersonalIdAndSearchTerm(@Param("personalId") Long personalId,
                                                 @Param("search") String search);
}
