package com.example.portfolio.education.persistence;

import com.example.portfolio.education.domain.Course;
import com.example.portfolio.education.domain.CourseProject;
import com.example.portfolio.projects.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseProjectRepository extends JpaRepository<CourseProject, Long> {

    Optional<CourseProject> findByProject(Project project);

    List<CourseProject> findAllByCourse(Course course);
}
