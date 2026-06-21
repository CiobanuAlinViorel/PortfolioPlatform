package com.example.portfolio.experience.persistence;

import com.example.portfolio.experience.domain.JobExperience;
import com.example.portfolio.profile.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobExperienceRepository extends JpaRepository<JobExperience, Long> {

    Optional<JobExperience> findFirstByProfileOrderByStartDateDesc(Profile profile);

    List<JobExperience> findAllByProfileOrderByStartDateDesc(Profile profile);
}
