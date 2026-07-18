package com.example.portfolio.experience.persistence;

import com.example.portfolio.experience.domain.VolunteerExperience;
import com.example.portfolio.experience.domain.VolunteerProject;
import com.example.portfolio.projects.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VolunteerProjectRepository extends JpaRepository<VolunteerProject, Long> {

    Optional<VolunteerProject> findByProject(Project project);

    List<VolunteerProject> findAllByVolunteerExperience(VolunteerExperience volunteerExperience);
}
