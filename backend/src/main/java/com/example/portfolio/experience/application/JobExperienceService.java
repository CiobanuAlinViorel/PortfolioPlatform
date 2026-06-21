package com.example.portfolio.experience.application;

import com.example.portfolio.experience.domain.JobExperience;
import com.example.portfolio.experience.domain.JobProjects;
import com.example.portfolio.experience.dto.*;
import com.example.portfolio.experience.mapper.JobExperienceMapper;
import com.example.portfolio.experience.persistence.JobExperienceRepository;
import com.example.portfolio.experience.persistence.JobProjectsRepository;
import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.profile.persistence.ProfileRepository;
import com.example.portfolio.projects.domain.Project;
import com.example.portfolio.projects.persistence.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class JobExperienceService {

    private final ProfileRepository profileRepository;
    private final JobExperienceRepository jobExperienceRepository;
    private final JobProjectsRepository jobProjectsRepository;
    private final ProjectRepository projectRepository;
    private final JobExperienceMapper jobExperienceMapper;

    // ── Public ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<JobExperienceListItemDto> getJobExperienceList() {
        Profile profile = requireProfile();
        return jobExperienceRepository.findAllByProfileOrderByStartDateDesc(profile)
                .stream()
                .map(jobExperienceMapper::toListItemDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public JobExperienceDetailDto getJobExperienceById(Long id) {
        JobExperience job = requireJobExperience(id);
        return toDetailDto(job);
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    @Transactional
    public JobExperienceDetailDto createJobExperience(CreateJobExperienceRequest request) {
        Profile profile = requireProfile();
        JobExperience job = jobExperienceMapper.toJobExperience(request);
        job.setProfile(profile);
        return toDetailDto(jobExperienceRepository.save(job));
    }

    @Transactional
    public JobExperienceDetailDto updateJobExperience(Long id, UpdateJobExperienceRequest request) {
        JobExperience job = requireJobExperience(id);
        jobExperienceMapper.updateJobExperience(request, job);
        return toDetailDto(jobExperienceRepository.save(job));
    }

    @Transactional
    public void deleteJobExperience(Long id) {
        JobExperience job = requireJobExperience(id);
        jobExperienceRepository.delete(job);
    }

    @Transactional
    public JobExperienceDetailDto addProject(Long jobId, Long projectId) {
        JobExperience job = requireJobExperience(jobId);
        Project project = requireProject(projectId);

        if (jobProjectsRepository.existsByProject(project)) {
            throw new IllegalArgumentException("Project is already linked to a job experience");
        }

        JobProjects link = JobProjects.builder()
                .jobExperience(job)
                .project(project)
                .build();
        job.addProject(jobProjectsRepository.save(link));
        return toDetailDto(job);
    }

    @Transactional
    public JobExperienceDetailDto removeProject(Long jobId, Long projectId) {
        JobExperience job = requireJobExperience(jobId);
        Project project = requireProject(projectId);

        JobProjects link = jobProjectsRepository.findByJobExperienceAndProject(job, project)
                .orElseThrow(() -> new NoSuchElementException("Project is not linked to this job experience"));

        job.removeProject(link);
        jobProjectsRepository.delete(link);
        return toDetailDto(job);
    }

    @Transactional(readOnly = true)
    public List<ProjectInJobDto> getAvailableProjects() {
        Profile profile = requireProfile();
        return projectRepository.findAvailableForJobExperience(profile)
                .stream()
                .map(jobExperienceMapper::toProjectInJobDto)
                .toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Profile requireProfile() {
        return profileRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new NoSuchElementException("Profile not found"));
    }

    private JobExperience requireJobExperience(Long id) {
        return jobExperienceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Job experience not found"));
    }

    private Project requireProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Project not found"));
    }

    private JobExperienceDetailDto toDetailDto(JobExperience job) {
        JobExperienceDetailDto dto = jobExperienceMapper.toDetailDto(job);
        dto.setProjects(job.getProjects().stream()
                .map(jp -> jobExperienceMapper.toProjectInJobDto(jp.getProject()))
                .toList());
        return dto;
    }
}
