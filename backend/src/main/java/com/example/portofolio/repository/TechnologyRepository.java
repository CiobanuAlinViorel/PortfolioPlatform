package com.example.portofolio.repository;

import com.example.portofolio.entity.Technology;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnologyRepository extends JpaRepository<Technology, Long> {

    // Trending technologies
    @Query("SELECT t FROM Technology t " +
            "WHERE t.trending = true " +
            "ORDER BY t.popularityScore DESC, t.name ASC")
    List<Technology> findTrendingTechnologies();

    @Query("SELECT COUNT(t) FROM Technology t WHERE t.trending = true")
    Long countTrendingTechnologies();

//    @Query("SELECT t.category.name, COUNT(t) FROM Technology t " +
//            "GROUP BY t.category.name " +
//            "ORDER BY COUNT(t) DESC")
//    List<Object[]> countTechnologiesByCategory();

    @Query("SELECT AVG(t.popularityScore) FROM Technology t")
    Double findAveragePopularityScore();

    // Search
    @Query("SELECT t FROM Technology t " +
            "WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Technology> findByNameOrDescriptionContaining(@Param("search") String search);

//    @Query("SELECT t FROM Technology t " +
//            "WHERE t.category.id = :categoryId " +
//            "AND (LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
//            "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))")
//    List<Technology> findByCategoryIdAndSearchTerm(@Param("categoryId") Long categoryId,
//                                                   @Param("search") String search);

    // Recently released
    @Query("SELECT t FROM Technology t " +
            "WHERE t.releaseDate >= :since " +
            "ORDER BY t.releaseDate DESC")
    List<Technology> findRecentlyReleased(@Param("since") java.time.LocalDate since);


}