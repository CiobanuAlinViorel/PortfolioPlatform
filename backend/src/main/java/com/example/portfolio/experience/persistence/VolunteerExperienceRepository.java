package com.example.portfolio.experience.persistence;

import com.example.portfolio.experience.domain.VolunteerExperience;
import com.example.portfolio.profile.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VolunteerExperienceRepository extends JpaRepository<VolunteerExperience, Long> {

    Optional<VolunteerExperience> findFirstByProfileOrderByStartDateDesc(Profile profile);

    List<VolunteerExperience> findAllByProfileOrderByStartDateDesc(Profile profile);
}
