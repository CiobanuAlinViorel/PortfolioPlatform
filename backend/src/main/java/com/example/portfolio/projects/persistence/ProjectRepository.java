package com.example.portfolio.projects.persistence;

import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.projects.domain.Project;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT p FROM Project p WHERE p.profile = :profile AND p.sortOrder IS NOT NULL ORDER BY p.sortOrder ASC")
    List<Project> findTopByProfile(@Param("profile") Profile profile, Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.profile = :profile ORDER BY p.sortOrder ASC, p.title ASC")
    List<Project> findAllByProfile(@Param("profile") Profile profile);

    @Query("SELECT p FROM Project p WHERE p.profile = :profile " +
           "AND p.id NOT IN (SELECT jp.project.id FROM JobProjects jp) " +
           "AND p.id NOT IN (SELECT vp.project.id FROM VolunteerProject vp) " +
           "AND p.id NOT IN (SELECT cp.project.id FROM CourseProject cp) " +
           "AND (p.projectCategory IS NULL OR LOWER(p.projectCategory.name) <> 'course') " +
           "ORDER BY p.title ASC")
    List<Project> findAvailableForJobExperience(@Param("profile") Profile profile);
}
