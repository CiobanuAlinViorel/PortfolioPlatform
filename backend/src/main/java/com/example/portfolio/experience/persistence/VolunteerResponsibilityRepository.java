package com.example.portfolio.experience.persistence;

import com.example.portfolio.experience.domain.VolunteerExperience;
import com.example.portfolio.experience.domain.VolunteerResponsibility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VolunteerResponsibilityRepository extends JpaRepository<VolunteerResponsibility, Long> {

    List<VolunteerResponsibility> findAllByVolunteerExperienceOrderBySortOrderAsc(VolunteerExperience volunteerExperience);
}
