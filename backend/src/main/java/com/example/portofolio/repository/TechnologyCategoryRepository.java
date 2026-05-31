package com.example.portofolio.repository;

import com.example.portofolio.entity.TechnologyCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnologyCategoryRepository extends JpaRepository<TechnologyCategory, Long> {

//    @Query("SELECT tc, COUNT(t) FROM TechnologyCategory tc " +
//            "LEFT JOIN Technology t ON t.category.id = tc.id " +
//            "GROUP BY tc " +
//            "ORDER BY tc.sortOrder, tc.name")
//    List<Object[]> findAllWithTechnologyCount();
}