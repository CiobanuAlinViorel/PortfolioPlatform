package com.example.portfolio.experience.application;

import com.example.portfolio.education.persistence.CourseProjectRepository;
import com.example.portfolio.experience.domain.JobExperience;
import com.example.portfolio.experience.domain.JobProjects;
import com.example.portfolio.experience.dto.*;
import com.example.portfolio.experience.mapper.JobExperienceMapper;
import com.example.portfolio.experience.persistence.JobExperienceRepository;
import com.example.portfolio.experience.persistence.JobProjectsRepository;
import com.example.portfolio.experience.persistence.VolunteerProjectRepository;
import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.profile.persistence.ProfileRepository;
import com.example.portfolio.projects.domain.Project;
import com.example.portfolio.projects.persistence.ProjectRepository;
import com.example.portfolio.shared.base.BaseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobExperienceService {

    private final ProfileRepository profileRepository;
    private final JobExperienceRepository jobExperienceRepository;
    private final JobProjectsRepository jobProjectsRepository;
    private final VolunteerProjectRepository volunteerProjectRepository;
    private final CourseProjectRepository courseProjectRepository;
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
        job = jobExperienceRepository.save(job);

        if (request.getProjects() != null) {
            for (JobProjectRequest pr : request.getProjects()) {
                job.addProject(jobProjectsRepository.save(buildJobProject(pr, job, null)));
            }
        }

        return toDetailDto(job);
    }

    @Transactional
    public JobExperienceDetailDto updateJobExperience(Long id, UpdateJobExperienceRequest request) {
        JobExperience job = requireJobExperience(id);
        jobExperienceMapper.updateJobExperience(request, job);
        jobExperienceRepository.save(job);

        if (request.getProjects() != null) syncProjects(job, request.getProjects());

        return toDetailDto(job);
    }

    @Transactional
    public void deleteJobExperience(Long id) {
        JobExperience job = requireJobExperience(id);
        jobExperienceRepository.delete(job);
    }

    @Transactional
    public JobExperienceDetailDto addProject(Long jobId, Long projectId) {
        JobExperience job = requireJobExperience(jobId);
        Project project = resolveProject(projectId, null);

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
        Project project = requireExistingProject(projectId);

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

    // ── Sync helpers ─────────────────────────────────────────────────────────

    private void syncProjects(JobExperience job, List<JobProjectRequest> requests) {
        List<JobProjects> existing = jobProjectsRepository.findAllByJobExperience(job);
        Map<Long, JobProjects> existingById = existing.stream()
                .collect(Collectors.toMap(BaseEntity::getId, p -> p));
        Set<Long> keepIds = new HashSet<>();

        for (JobProjectRequest pr : requests) {
            if (pr.getId() != null && existingById.containsKey(pr.getId())) {
                JobProjects link = existingById.get(pr.getId());
                if (pr.getProjectId() != null && !pr.getProjectId().equals(link.getProject().getId())) {
                    link.setProject(resolveProject(pr.getProjectId(), link.getId()));
                    jobProjectsRepository.save(link);
                }
                keepIds.add(link.getId());
            } else {
                JobProjects link = jobProjectsRepository.save(buildJobProject(pr, job, null));
                job.addProject(link);
                keepIds.add(link.getId());
            }
        }

        existing.stream()
                .filter(p -> !keepIds.contains(p.getId()))
                .forEach(p -> { job.removeProject(p); jobProjectsRepository.delete(p); });
    }

    // ── Builder / resolve helpers ────────────────────────────────────────────

    private JobProjects buildJobProject(JobProjectRequest pr, JobExperience job, Long excludeLinkId) {
        Project project = resolveProject(pr.getProjectId(), excludeLinkId);
        return JobProjects.builder()
                .jobExperience(job)
                .project(project)
                .build();
    }

    /**
     * Resolves an existing Project by id and enforces that it isn't already linked to another
     * job experience, to any volunteer experience, or to any course — a project can only be
     * linked to one job at a time, and jobs/volunteering/courses don't mix except that a
     * volunteer project is exempt from course exclusivity (only job vs. course is enforced here).
     * excludeLinkId lets an update keep its own link.
     */
    private Project resolveProject(Long projectId, Long excludeLinkId) {
        Project project = requireExistingProject(projectId);

        jobProjectsRepository.findByProject(project).ifPresent(existingLink -> {
            if (excludeLinkId == null || !existingLink.getId().equals(excludeLinkId)) {
                throw new IllegalArgumentException("Project is already linked to a job experience");
            }
        });

        volunteerProjectRepository.findByProject(project).ifPresent(existingLink -> {
            throw new IllegalArgumentException("Project is already linked to a volunteer experience");
        });

        courseProjectRepository.findByProject(project).ifPresent(existingLink -> {
            throw new IllegalArgumentException("Project is already linked to a course");
        });

        return project;
    }

    private Profile requireProfile() {
        return profileRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new NoSuchElementException("Profile not found"));
    }

    private JobExperience requireJobExperience(Long id) {
        return jobExperienceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Job experience not found"));
    }

    private Project requireExistingProject(Long id) {
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
