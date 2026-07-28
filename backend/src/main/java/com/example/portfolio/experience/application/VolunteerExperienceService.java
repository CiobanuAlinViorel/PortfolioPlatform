package com.example.portfolio.experience.application;

import com.example.portfolio.experience.domain.ImpactLevel;
import com.example.portfolio.experience.domain.VolunteerExperience;
import com.example.portfolio.experience.domain.VolunteerProject;
import com.example.portfolio.experience.domain.VolunteerResponsibility;
import com.example.portfolio.experience.dto.CreateVolunteerExperienceRequest;
import com.example.portfolio.experience.dto.UpdateVolunteerExperienceRequest;
import com.example.portfolio.experience.dto.VolunteerExperienceDetailDto;
import com.example.portfolio.experience.dto.VolunteerExperienceListItemDto;
import com.example.portfolio.experience.dto.ProjectInVolunteerDto;
import com.example.portfolio.experience.dto.VolunteerProjectDto;
import com.example.portfolio.experience.dto.VolunteerProjectRequest;
import com.example.portfolio.experience.dto.VolunteerResponsibilityRequest;
import com.example.portfolio.experience.mapper.VolunteerExperienceMapper;
import com.example.portfolio.experience.persistence.JobProjectsRepository;
import com.example.portfolio.experience.persistence.VolunteerExperienceRepository;
import com.example.portfolio.experience.persistence.VolunteerProjectRepository;
import com.example.portfolio.experience.persistence.VolunteerResponsibilityRepository;
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
public class VolunteerExperienceService {

    private final ProfileRepository profileRepository;
    private final VolunteerExperienceRepository volunteerExperienceRepository;
    private final VolunteerProjectRepository volunteerProjectRepository;
    private final VolunteerResponsibilityRepository volunteerResponsibilityRepository;
    private final JobProjectsRepository jobProjectsRepository;
    private final ProjectRepository projectRepository;
    private final VolunteerExperienceMapper volunteerExperienceMapper;

    // ── Public ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<VolunteerExperienceListItemDto> getVolunteerExperienceList() {
        Profile profile = requireProfile();
        return volunteerExperienceRepository.findAllByProfileOrderByStartDateDesc(profile)
                .stream()
                .map(volunteerExperienceMapper::toListItemDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public VolunteerExperienceDetailDto getVolunteerExperienceById(Long id) {
        return toDetailDto(requireVolunteerExperience(id));
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    @Transactional
    public VolunteerExperienceDetailDto createVolunteerExperience(CreateVolunteerExperienceRequest request) {
        Profile profile = requireProfile();

        VolunteerExperience volunteerExperience = volunteerExperienceMapper.toVolunteerExperience(request);
        volunteerExperience.setProfile(profile);
        volunteerExperience = volunteerExperienceRepository.save(volunteerExperience);

        if (request.getResponsibilities() != null) {
            for (VolunteerResponsibilityRequest rr : request.getResponsibilities()) {
                volunteerExperience.addResponsibility(
                        volunteerResponsibilityRepository.save(buildResponsibility(rr, volunteerExperience)));
            }
        }

        if (request.getProjects() != null) {
            for (VolunteerProjectRequest pr : request.getProjects()) {
                volunteerExperience.addProject(
                        volunteerProjectRepository.save(buildVolunteerProject(pr, volunteerExperience, null)));
            }
        }

        return toDetailDto(volunteerExperience);
    }

    @Transactional
    public VolunteerExperienceDetailDto updateVolunteerExperience(Long id, UpdateVolunteerExperienceRequest request) {
        VolunteerExperience volunteerExperience = requireVolunteerExperience(id);

        volunteerExperienceMapper.updateVolunteerExperience(request, volunteerExperience);
        volunteerExperienceRepository.save(volunteerExperience);

        if (request.getResponsibilities() != null) syncResponsibilities(volunteerExperience, request.getResponsibilities());
        if (request.getProjects() != null) syncProjects(volunteerExperience, request.getProjects());

        return toDetailDto(volunteerExperience);
    }

    @Transactional
    public void deleteVolunteerExperience(Long id) {
        // cascade = ALL on VolunteerExperience.responsibilities/volunteerProjects removes them along with the parent
        volunteerExperienceRepository.delete(requireVolunteerExperience(id));
    }

    @Transactional(readOnly = true)
    public List<ProjectInVolunteerDto> getAvailableProjects() {
        Profile profile = requireProfile();
        return projectRepository.findAvailableForJobExperience(profile)
                .stream()
                .map(volunteerExperienceMapper::toProjectInVolunteerDto)
                .toList();
    }

    // ── Sync helpers ─────────────────────────────────────────────────────────

    private void syncResponsibilities(VolunteerExperience volunteerExperience, List<VolunteerResponsibilityRequest> requests) {
        List<VolunteerResponsibility> existing =
                volunteerResponsibilityRepository.findAllByVolunteerExperienceOrderBySortOrderAsc(volunteerExperience);
        Map<Long, VolunteerResponsibility> existingById = existing.stream()
                .collect(Collectors.toMap(BaseEntity::getId, r -> r));
        Set<Long> keepIds = new HashSet<>();

        for (VolunteerResponsibilityRequest rr : requests) {
            if (rr.getId() != null && existingById.containsKey(rr.getId())) {
                VolunteerResponsibility r = existingById.get(rr.getId());
                if (rr.getDescription() != null) r.setDescription(rr.getDescription());
                if (rr.getImpactLevel() != null) r.setImpactLevel(rr.getImpactLevel());
                if (rr.getSortOrder() != null) r.setSortOrder(rr.getSortOrder());
                volunteerResponsibilityRepository.save(r);
                keepIds.add(r.getId());
            } else {
                VolunteerResponsibility r = volunteerResponsibilityRepository.save(buildResponsibility(rr, volunteerExperience));
                volunteerExperience.addResponsibility(r);
                keepIds.add(r.getId());
            }
        }

        existing.stream()
                .filter(r -> !keepIds.contains(r.getId()))
                .forEach(r -> { volunteerExperience.removeResponsibility(r); volunteerResponsibilityRepository.delete(r); });
    }

    private void syncProjects(VolunteerExperience volunteerExperience, List<VolunteerProjectRequest> requests) {
        List<VolunteerProject> existing = volunteerProjectRepository.findAllByVolunteerExperience(volunteerExperience);
        Map<Long, VolunteerProject> existingById = existing.stream()
                .collect(Collectors.toMap(BaseEntity::getId, p -> p));
        Set<Long> keepIds = new HashSet<>();

        for (VolunteerProjectRequest pr : requests) {
            if (pr.getId() != null && existingById.containsKey(pr.getId())) {
                VolunteerProject vp = existingById.get(pr.getId());
                if (pr.getProjectId() != null && !pr.getProjectId().equals(vp.getProject().getId())) {
                    vp.setProject(resolveProject(pr.getProjectId(), vp.getId()));
                }
                if (pr.getContributionPercentage() != null) vp.setContributionPercentage(pr.getContributionPercentage());
                volunteerProjectRepository.save(vp);
                keepIds.add(vp.getId());
            } else {
                VolunteerProject vp = volunteerProjectRepository.save(buildVolunteerProject(pr, volunteerExperience, null));
                volunteerExperience.addProject(vp);
                keepIds.add(vp.getId());
            }
        }

        existing.stream()
                .filter(p -> !keepIds.contains(p.getId()))
                .forEach(p -> { volunteerExperience.removeProject(p); volunteerProjectRepository.delete(p); });
    }

    // ── Builder / resolve helpers ────────────────────────────────────────────

    private VolunteerResponsibility buildResponsibility(VolunteerResponsibilityRequest rr, VolunteerExperience volunteerExperience) {
        return VolunteerResponsibility.builder()
                .volunteerExperience(volunteerExperience)
                .description(rr.getDescription())
                .impactLevel(rr.getImpactLevel() != null ? rr.getImpactLevel() : ImpactLevel.MEDIUM)
                .sortOrder(rr.getSortOrder() != null ? rr.getSortOrder() : 0)
                .build();
    }

    private VolunteerProject buildVolunteerProject(VolunteerProjectRequest pr, VolunteerExperience volunteerExperience, Long excludeLinkId) {
        Project project = resolveProject(pr.getProjectId(), excludeLinkId);
        return VolunteerProject.builder()
                .volunteerExperience(volunteerExperience)
                .project(project)
                .contributionPercentage(pr.getContributionPercentage())
                .build();
    }

    /**
     * Resolves an existing Project by id and enforces that it isn't already linked to another
     * volunteer experience, nor to any job experience — a project can only be linked to one
     * experience of either kind at a time. excludeLinkId lets an update keep its own link.
     */
    private Project resolveProject(Long projectId, Long excludeLinkId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NoSuchElementException("Project not found"));

        volunteerProjectRepository.findByProject(project).ifPresent(existingLink -> {
            if (excludeLinkId == null || !existingLink.getId().equals(excludeLinkId)) {
                throw new IllegalArgumentException("Project is already linked to a volunteer experience");
            }
        });

        jobProjectsRepository.findByProject(project).ifPresent(existingLink -> {
            throw new IllegalArgumentException("Project is already linked to a job experience");
        });

        return project;
    }

    private Profile requireProfile() {
        return profileRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new NoSuchElementException("Profile not found"));
    }

    private VolunteerExperience requireVolunteerExperience(Long id) {
        return volunteerExperienceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Volunteer experience not found"));
    }

    private VolunteerExperienceDetailDto toDetailDto(VolunteerExperience volunteerExperience) {
        VolunteerExperienceDetailDto dto = volunteerExperienceMapper.toDetailDto(volunteerExperience);

        dto.setResponsibilities(volunteerExperience.getResponsibilities().stream()
                .sorted((a, b) -> {
                    Integer sa = a.getSortOrder() != null ? a.getSortOrder() : Integer.MAX_VALUE;
                    Integer sb = b.getSortOrder() != null ? b.getSortOrder() : Integer.MAX_VALUE;
                    return sa.compareTo(sb);
                })
                .map(volunteerExperienceMapper::toResponsibilityDto)
                .toList());

        dto.setProjects(volunteerExperience.getVolunteerProjects().stream()
                .map(vp -> VolunteerProjectDto.builder()
                        .id(vp.getId())
                        .contributionPercentage(vp.getContributionPercentage())
                        .project(volunteerExperienceMapper.toProjectInVolunteerDto(vp.getProject()))
                        .build())
                .toList());

        return dto;
    }
}
