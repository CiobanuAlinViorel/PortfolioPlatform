package com.example.portofolio.repository;

import com.example.portofolio.entity.Certificate;
import com.example.portofolio.entity.CertificationCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    // Basic queries
    List<Certificate> findByPersonalId(Long personalId);

    // Optimized queries
//    @Query("SELECT c FROM Certificate c " +
//            "LEFT JOIN FETCH c.category " +
//            "WHERE c.personal.id = :personalId")
//    List<Certificate> findByPersonalIdWithCategory(@Param("personalId") Long personalId);

    // Verification status
    List<Certificate> findByPersonalIdAndVerifiedTrue(Long personalId);



    // Expiry management
    @Query("SELECT c FROM Certificate c " +
            "WHERE c.personal.id = :personalId " +
            "AND c.hasExpiry = true " +
            "AND c.expiryDate <= :date " +
            "ORDER BY c.expiryDate ASC")
    List<Certificate> findExpiringByPersonalId(@Param("personalId") Long personalId,
                                               @Param("date") LocalDate date);

    @Query("SELECT c.provider, COUNT(c) FROM Certificate c " +
            "WHERE c.personal.id = :personalId " +
            "GROUP BY c.provider " +
            "ORDER BY COUNT(c) DESC")
    List<Object[]> countCertificatesByProvider(@Param("personalId") Long personalId);

    // Relevance score
    @Query("SELECT c FROM Certificate c " +
            "WHERE c.personal.id = :personalId " +
            "AND c.relevanceScore >= :minScore " +
            "ORDER BY c.relevanceScore DESC, c.issueDate DESC")
    List<Certificate> findByPersonalIdAndMinRelevanceScore(@Param("personalId") Long personalId,
                                                           @Param("minScore") Integer minScore);

    // Statistics
    @Query("SELECT COUNT(c) FROM Certificate c WHERE c.personal.id = :personalId")
    Long countByPersonalId(@Param("personalId") Long personalId);

    @Query("SELECT COUNT(c) FROM Certificate c " +
            "WHERE c.personal.id = :personalId AND c.verified = true")
    Long countVerifiedByPersonalId(@Param("personalId") Long personalId);

    @Query("SELECT AVG(c.relevanceScore) FROM Certificate c " +
            "WHERE c.personal.id = :personalId")
    Double findAverageRelevanceScoreByPersonalId(@Param("personalId") Long personalId);

    // Search
    @Query("SELECT c FROM Certificate c " +
            "WHERE c.personal.id = :personalId " +
            "AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(c.provider) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Certificate> findByPersonalIdAndSearchTerm(@Param("personalId") Long personalId,
                                                    @Param("search") String search);

    /**
     * Get all categories with their icons
     */
    @Query("SELECT cc FROM CertificationCategory cc " +
            "LEFT JOIN FETCH cc.icon " +
            "ORDER BY cc.name ASC")
    List<CertificationCategory> findAllWithIcon();

}