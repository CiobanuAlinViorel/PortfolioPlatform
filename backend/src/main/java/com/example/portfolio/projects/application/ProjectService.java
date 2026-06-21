package com.example.portfolio.projects.application;

import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.profile.persistence.ProfileRepository;
import com.example.portfolio.projects.domain.Project;
import com.example.portfolio.projects.dto.*;
import com.example.portfolio.projects.mapper.ProjectMapper;
import com.example.portfolio.projects.persistence.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProfileRepository profileRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    @Transactional(readOnly = true)
    public List<ProjectListItemDto> getProjectsList() {
        Profile profile = profileRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new NoSuchElementException("Profile not found"));
        return projectRepository.findAllByProfile(profile)
                .stream()
                .map(projectMapper::toProjectListItemDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectDetailDto getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Project not found"));

        ProjectDetailDto dto = projectMapper.toProjectDetailDto(project);

        dto.setFeatures(project.getFeatures().stream()
                .sorted(Comparator.comparingInt(f -> f.getSortOrder() != null ? f.getSortOrder() : Integer.MAX_VALUE))
                .map(projectMapper::toProjectFeatureDto)
                .toList());

        dto.setChallenges(project.getChallenges().stream()
                .map(projectMapper::toProjectChallengeDto)
                .toList());

        dto.setImages(project.getImages().stream()
                .sorted(Comparator.comparingInt(i -> i.getSortOrder() != null ? i.getSortOrder() : Integer.MAX_VALUE))
                .map(projectMapper::toProjectImageDto)
                .toList());

        dto.setSkills(project.getSkills().stream()
                .map(projectMapper::toSkillInProjectDto)
                .toList());

        dto.setMetrics(project.getMetrics() != null
                ? projectMapper.toProjectMetricsDto(project.getMetrics())
                : null);

        return dto;
    }
}
