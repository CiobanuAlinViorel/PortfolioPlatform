package com.example.portfolio.education.persistence;

import com.example.portfolio.education.domain.Education;
import com.example.portfolio.profile.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface EducationRepository extends JpaRepository<Education, Long>, JpaSpecificationExecutor<Education> {
    List<Education> findByProfileOrderByStartDateDesc(Profile profile);
}
