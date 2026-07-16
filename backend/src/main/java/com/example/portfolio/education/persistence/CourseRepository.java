package com.example.portfolio.education.persistence;

import com.example.portfolio.education.domain.Course;
import com.example.portfolio.education.domain.Education;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findAllByEducationOrderByYearAscSemesterAsc(Education education);
}
