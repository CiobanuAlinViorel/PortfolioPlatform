package com.example.portfolio.projects.application;

import com.example.portfolio.achievement.domain.Achievement;
import com.example.portfolio.achievement.domain.RecognitionLevel;
import com.example.portfolio.achievement.persistence.AchievementRepository;
import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.profile.persistence.ProfileRepository;
import com.example.portfolio.projects.domain.*;
import com.example.portfolio.projects.dto.*;
import com.example.portfolio.projects.mapper.ProjectMapper;
import com.example.portfolio.projects.persistence.*;
import com.example.portfolio.shared.base.BaseEntity;
import com.example.portfolio.shared.service.CloudinaryService;
import com.example.portfolio.skills.domain.*;
import com.example.portfolio.skills.persistence.SkillCategoryRepository;
import com.example.portfolio.skills.persistence.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProfileRepository profileRepository;
    private final ProjectRepository projectRepository;
    private final ProjectCategoryRepository projectCategoryRepository;
    private final ProjectFeatureRepository projectFeatureRepository;
    private final ProjectChallengeRepository projectChallengeRepository;
    private final ProjectMediaRepository projectMediaRepository;
    private final ProjectMetricsRepository projectMetricsRepository;
    private final ProjectAchievementRepository projectAchievementRepository;
    private final ProjectSkillRepository projectSkillRepository;
    private final SkillRepository skillRepository;
    private final SkillCategoryRepository skillCategoryRepository;
    private final AchievementRepository achievementRepository;
    private final CloudinaryService cloudinaryService;
    private final ProjectMapper projectMapper;

    // ─────────────────────────── READ ────────────────────────────

    @Transactional(readOnly = true)
    public List<ProjectListItemDto> getProjectsList() {
        Profile profile = getProfile();
        return projectRepository.findAllByProfile(profile)
                .stream()
                .map(projectMapper::toProjectListItemDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectDetailDto getProjectById(Long id) {
        return buildDetailDto(findProject(id));
    }

    // ─────────────────────────── CREATE ──────────────────────────

    @Transactional
    public ProjectDetailDto createProject(CreateProjectRequest request, List<MultipartFile> files) {
        Profile profile = getProfile();

        resolveMediaUrls(request.getMedia(), files);
        validateMedia(request.getMedia());

        Project project = Project.builder()
                .profile(profile)
                .title(request.getTitle())
                .description(request.getDescription())
                .longDescription(request.getLongDescription())
                .status(request.getStatus())
                .complexity(request.getComplexity())
                .demoUrl(request.getDemoUrl())
                .githubUrl(request.getGithubUrl())
                .year(request.getYear())
                .completionDate(request.getCompletionDate())
                .developmentTime(request.getDevelopmentTime())
                .sortOrder(request.getSortOrder())
                .tags(request.getTags() != null ? request.getTags() : new ArrayList<>())
                .projectCategory(resolveProjectCategory(request.getCategoryId(), request.getCategoryName()))
                .build();

        project = projectRepository.save(project);

        if (request.getFeatures() != null) {
            for (ProjectFeatureRequest fr : request.getFeatures()) {
                project.addFeature(projectFeatureRepository.save(buildFeature(fr, project)));
            }
        }

        if (request.getChallenges() != null) {
            for (ProjectChallengeRequest cr : request.getChallenges()) {
                project.addChallenge(projectChallengeRepository.save(buildChallenge(cr, project)));
            }
        }

        if (request.getMedia() != null) {
            enforceSinglePrimary(request.getMedia());
            for (ProjectMediaRequest mr : request.getMedia()) {
                project.addMedia(projectMediaRepository.save(buildMedia(mr, project)));
            }
        }

        project.setMetrics(projectMetricsRepository.save(buildMetrics(request.getMetrics(), project)));

        if (request.getSkills() != null) {
            for (ProjectSkillRequest sr : request.getSkills()) {
                Skill skill = resolveOrCreateSkill(sr, profile);
                project.addSkill(projectSkillRepository.save(
                        ProjectSkill.builder().project(project).skill(skill).usage(sr.getUsage()).build()
                ));
            }
        }

        if (request.getAchievements() != null) {
            for (ProjectAchievementRequest ar : request.getAchievements()) {
                Achievement achievement = resolveOrCreateAchievement(ar, profile);
                project.addAchievement(projectAchievementRepository.save(
                        buildProjectAchievement(ar, project, achievement)
                ));
            }
        }

        return buildDetailDto(project);
    }

    // ─────────────────────────── UPDATE ──────────────────────────

    @Transactional
    public ProjectDetailDto updateProject(Long id, UpdateProjectRequest request, List<MultipartFile> files) {
        Project project = findProject(id);
        Profile profile = project.getProfile();

        resolveMediaUrls(request.getMedia(), files);
        validateMedia(request.getMedia());

        if (request.getTitle() != null)           project.setTitle(request.getTitle());
        if (request.getDescription() != null)     project.setDescription(request.getDescription());
        if (request.getLongDescription() != null) project.setLongDescription(request.getLongDescription());
        if (request.getStatus() != null)          project.setStatus(request.getStatus());
        if (request.getComplexity() != null)      project.setComplexity(request.getComplexity());
        if (request.getDemoUrl() != null)         project.setDemoUrl(request.getDemoUrl());
        if (request.getGithubUrl() != null)       project.setGithubUrl(request.getGithubUrl());
        if (request.getYear() != null)            project.setYear(request.getYear());
        if (request.getCompletionDate() != null)  project.setCompletionDate(request.getCompletionDate());
        if (request.getDevelopmentTime() != null) project.setDevelopmentTime(request.getDevelopmentTime());
        if (request.getSortOrder() != null)       project.setSortOrder(request.getSortOrder());
        if (request.getTags() != null)            project.setTags(request.getTags());
        if (request.getCategoryId() != null || request.getCategoryName() != null) {
            project.setProjectCategory(resolveProjectCategory(request.getCategoryId(), request.getCategoryName()));
        }

        projectRepository.save(project);

        if (request.getFeatures()     != null) syncFeatures(project, request.getFeatures());
        if (request.getChallenges()   != null) syncChallenges(project, request.getChallenges());
        if (request.getMedia()        != null) syncMedia(project, request.getMedia());
        if (request.getMetrics()      != null) syncMetrics(project, request.getMetrics());
        if (request.getSkills()       != null) syncSkills(project, request.getSkills(), profile);
        if (request.getAchievements() != null) syncAchievements(project, request.getAchievements(), profile);

        return buildDetailDto(project);
    }

    // ─────────────────────────── DELETE ──────────────────────────

    @Transactional
    public void deleteProject(Long id) {
        Project project = findProject(id);
        projectMediaRepository.findAllByProjectOrderBySortOrderAsc(project)
                .forEach(m -> cloudinaryService.deleteMedia(m.getImageUrl()));
        projectRepository.delete(project);
    }

    // ─────────────────── CATEGORY CRUD ───────────────────────────

    @Transactional
    public ProjectCategoryDto createCategory(CreateProjectCategoryRequest request) {
        ProjectCategory category = ProjectCategory.builder().name(request.getName()).build();
        return projectMapper.toProjectCategoryDto(projectCategoryRepository.save(category));
    }

    @Transactional
    public ProjectCategoryDto updateCategory(Long id, UpdateProjectCategoryRequest request) {
        ProjectCategory category = projectCategoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Project category not found"));
        if (request.getName() != null) category.setName(request.getName());
        return projectMapper.toProjectCategoryDto(projectCategoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!projectCategoryRepository.existsById(id))
            throw new NoSuchElementException("Project category not found");
        projectCategoryRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ProjectCategoryDto getCategory(Long id) {
        return projectMapper.toProjectCategoryDto(
                projectCategoryRepository.findById(id)
                        .orElseThrow(() -> new NoSuchElementException("Project category not found"))
        );
    }

    @Transactional(readOnly = true)
    public List<ProjectCategoryDto> getAllCategories() {
        return projectCategoryRepository.findAll()
                .stream()
                .map(projectMapper::toProjectCategoryDto)
                .toList();
    }

    // ─────────────────── SYNC HELPERS ────────────────────────────

    private void syncFeatures(Project project, List<ProjectFeatureRequest> requests) {
        List<ProjectFeature> existing = projectFeatureRepository.findAllByProjectOrderBySortOrderAsc(project);
        Map<Long, ProjectFeature> existingById = existing.stream()
                .collect(Collectors.toMap(BaseEntity::getId, f -> f));
        Set<Long> keepIds = new HashSet<>();

        for (ProjectFeatureRequest fr : requests) {
            if (fr.getId() != null && existingById.containsKey(fr.getId())) {
                ProjectFeature f = existingById.get(fr.getId());
                if (fr.getTitle() != null)               f.setTitle(fr.getTitle());
                if (fr.getDescription() != null)         f.setDescription(fr.getDescription());
                if (fr.getImplementationDate() != null)  f.setImplementationDate(fr.getImplementationDate());
                if (fr.getDevelopmentTimeHours() != null) f.setDevelopmentTimeHours(fr.getDevelopmentTimeHours());
                if (fr.getSortOrder() != null)           f.setSortOrder(fr.getSortOrder());
                projectFeatureRepository.save(f);
                keepIds.add(f.getId());
            } else {
                ProjectFeature f = projectFeatureRepository.save(buildFeature(fr, project));
                project.addFeature(f);
                keepIds.add(f.getId());
            }
        }

        existing.stream()
                .filter(f -> !keepIds.contains(f.getId()))
                .forEach(f -> { project.removeFeature(f); projectFeatureRepository.delete(f); });
    }

    private void syncChallenges(Project project, List<ProjectChallengeRequest> requests) {
        List<ProjectChallenge> existing = projectChallengeRepository.findAllByProject(project);
        Map<Long, ProjectChallenge> existingById = existing.stream()
                .collect(Collectors.toMap(BaseEntity::getId, c -> c));
        Set<Long> keepIds = new HashSet<>();

        for (ProjectChallengeRequest cr : requests) {
            if (cr.getId() != null && existingById.containsKey(cr.getId())) {
                ProjectChallenge c = existingById.get(cr.getId());
                if (cr.getTitle() != null)       c.setTitle(cr.getTitle());
                if (cr.getDescription() != null) c.setDescription(cr.getDescription());
                if (cr.getSolution() != null)    c.setSolution(cr.getSolution());
                if (cr.getDifficulty() != null)  c.setDifficulty(cr.getDifficulty());
                projectChallengeRepository.save(c);
                keepIds.add(c.getId());
            } else {
                ProjectChallenge c = projectChallengeRepository.save(buildChallenge(cr, project));
                project.addChallenge(c);
                keepIds.add(c.getId());
            }
        }

        existing.stream()
                .filter(c -> !keepIds.contains(c.getId()))
                .forEach(c -> { project.removeChallenge(c); projectChallengeRepository.delete(c); });
    }

    private void syncMedia(Project project, List<ProjectMediaRequest> requests) {
        List<ProjectMedia> existing = projectMediaRepository.findAllByProjectOrderBySortOrderAsc(project);
        Map<Long, ProjectMedia> existingById = existing.stream()
                .collect(Collectors.toMap(BaseEntity::getId, m -> m));
        Set<Long> keepIds = new HashSet<>();
        enforceSinglePrimary(requests);

        for (ProjectMediaRequest mr : requests) {
            if (mr.getId() != null && existingById.containsKey(mr.getId())) {
                ProjectMedia m = existingById.get(mr.getId());
                if (mr.getTitle() != null)     m.setTitle(mr.getTitle());
                if (mr.getDescription() != null) m.setDescription(mr.getDescription());
                if (mr.getImageUrl() != null)  m.setImageUrl(mr.getImageUrl());
                if (mr.getAltText() != null)   m.setAltText(mr.getAltText());
                if (mr.getSortOrder() != null) m.setSortOrder(mr.getSortOrder());
                if (mr.getPrimary() != null)   m.setPrimary(mr.getPrimary());
                projectMediaRepository.save(m);
                keepIds.add(m.getId());
            } else {
                ProjectMedia m = projectMediaRepository.save(buildMedia(mr, project));
                project.addMedia(m);
                keepIds.add(m.getId());
            }
        }

        existing.stream()
                .filter(m -> !keepIds.contains(m.getId()))
                .forEach(m -> {
                    cloudinaryService.deleteMedia(m.getImageUrl());
                    project.removeMedia(m);
                    projectMediaRepository.delete(m);
                });
    }

    private void syncMetrics(Project project, ProjectMetricsRequest request) {
        ProjectMetrics metrics = projectMetricsRepository.findByProject(project)
                .orElse(ProjectMetrics.builder().project(project).build());

        if (request.getUsersCount() != null)            metrics.setUsersCount(request.getUsersCount());
        if (request.getPerformanceScore() != null)      metrics.setPerformanceScore(request.getPerformanceScore());
        if (request.getCodeQualityScore() != null)      metrics.setCodeQualityScore(request.getCodeQualityScore());
        if (request.getLinesOfCode() != null)           metrics.setLinesOfCode(request.getLinesOfCode());
        if (request.getCommitsCount() != null)          metrics.setCommitsCount(request.getCommitsCount());
        if (request.getTestCoveragePercentage() != null) metrics.setTestCoveragePercentage(request.getTestCoveragePercentage());
        metrics.setLastUpdated(LocalDateTime.now());

        project.setMetrics(projectMetricsRepository.save(metrics));
    }

    private void syncSkills(Project project, List<ProjectSkillRequest> requests, Profile profile) {
        List<ProjectSkill> existing = projectSkillRepository.findAllByProject(project);
        Map<Long, ProjectSkill> existingBySkillId = existing.stream()
                .collect(Collectors.toMap(ps -> ps.getSkill().getId(), ps -> ps));
        Set<Long> keepSkillIds = new HashSet<>();

        for (ProjectSkillRequest sr : requests) {
            Skill skill = resolveOrCreateSkill(sr, profile);
            keepSkillIds.add(skill.getId());

            if (existingBySkillId.containsKey(skill.getId())) {
                ProjectSkill ps = existingBySkillId.get(skill.getId());
                if (sr.getUsage() != null) ps.setUsage(sr.getUsage());
                projectSkillRepository.save(ps);
            } else {
                project.addSkill(projectSkillRepository.save(
                        ProjectSkill.builder().project(project).skill(skill).usage(sr.getUsage()).build()
                ));
            }
        }

        existing.stream()
                .filter(ps -> !keepSkillIds.contains(ps.getSkill().getId()))
                .forEach(ps -> { project.removeSkill(ps); projectSkillRepository.delete(ps); });
    }

    private void syncAchievements(Project project, List<ProjectAchievementRequest> requests, Profile profile) {
        List<ProjectAchievement> existing = projectAchievementRepository.findAllByProject(project);
        Map<Long, ProjectAchievement> existingById = existing.stream()
                .collect(Collectors.toMap(pa -> pa.getId(), pa -> pa));
        Set<Long> keepIds = new HashSet<>();

        for (ProjectAchievementRequest ar : requests) {
            if (ar.getId() != null && existingById.containsKey(ar.getId())) {
                ProjectAchievement pa = existingById.get(ar.getId());
                if (ar.getImpactSummary() != null) pa.setImpactSummary(ar.getImpactSummary());
                if (ar.getDemoUrl() != null)       pa.setDemoUrl(ar.getDemoUrl());
                if (ar.getMetricBefore() != null)  pa.setMetricBefore(ar.getMetricBefore());
                if (ar.getMetricAfter() != null)   pa.setMetricAfter(ar.getMetricAfter());
                Achievement achievement = pa.getAchievement();
                if (ar.getTitle() != null)           achievement.setTitle(ar.getTitle());
                if (ar.getDescription() != null)     achievement.setDescription(ar.getDescription());
                if (ar.getAchievementType() != null) achievement.setAchievementType(ar.getAchievementType());
                if (ar.getAchievementDate() != null) achievement.setAchievementDate(ar.getAchievementDate());
                if (ar.getRecognitionLevel() != null) achievement.setRecognitionLevel(ar.getRecognitionLevel());
                if (ar.getRecognizedBy() != null)    achievement.setRecognizedBy(ar.getRecognizedBy());
                if (ar.getProofUrl() != null)        achievement.setProofUrl(ar.getProofUrl());
                if (ar.getIsFeatured() != null)      achievement.setIsFeatured(ar.getIsFeatured());
                achievementRepository.save(achievement);
                projectAchievementRepository.save(pa);
                keepIds.add(pa.getId());
            } else {
                Achievement achievement = resolveOrCreateAchievement(ar, profile);
                ProjectAchievement pa = projectAchievementRepository.save(
                        buildProjectAchievement(ar, project, achievement)
                );
                project.addAchievement(pa);
                keepIds.add(pa.getId());
            }
        }

        existing.stream()
                .filter(pa -> !keepIds.contains(pa.getId()))
                .forEach(pa -> { project.removeAchievement(pa); projectAchievementRepository.delete(pa); });
    }

    // ─────────────────── BUILDER HELPERS ─────────────────────────

    private ProjectFeature buildFeature(ProjectFeatureRequest fr, Project project) {
        return ProjectFeature.builder()
                .project(project)
                .title(fr.getTitle())
                .description(fr.getDescription())
                .implementationDate(fr.getImplementationDate())
                .developmentTimeHours(fr.getDevelopmentTimeHours())
                .sortOrder(fr.getSortOrder() != null ? fr.getSortOrder() : 0)
                .build();
    }

    private ProjectChallenge buildChallenge(ProjectChallengeRequest cr, Project project) {
        return ProjectChallenge.builder()
                .project(project)
                .title(cr.getTitle())
                .description(cr.getDescription())
                .solution(cr.getSolution())
                .difficulty(cr.getDifficulty() != null ? cr.getDifficulty() : DifficultyLevel.MEDIUM)
                .build();
    }

    private ProjectMedia buildMedia(ProjectMediaRequest mr, Project project) {
        return ProjectMedia.builder()
                .project(project)
                .title(mr.getTitle())
                .description(mr.getDescription())
                .imageUrl(mr.getImageUrl())
                .altText(mr.getAltText())
                .sortOrder(mr.getSortOrder() != null ? mr.getSortOrder() : 0)
                .primary(Boolean.TRUE.equals(mr.getPrimary()))
                .build();
    }

    private ProjectMetrics buildMetrics(ProjectMetricsRequest request, Project project) {
        if (request == null) {
            return ProjectMetrics.builder().project(project).build();
        }
        return ProjectMetrics.builder()
                .project(project)
                .usersCount(request.getUsersCount() != null ? request.getUsersCount() : 0L)
                .performanceScore(request.getPerformanceScore())
                .codeQualityScore(request.getCodeQualityScore())
                .linesOfCode(request.getLinesOfCode() != null ? request.getLinesOfCode() : 0L)
                .commitsCount(request.getCommitsCount() != null ? request.getCommitsCount() : 0)
                .testCoveragePercentage(request.getTestCoveragePercentage())
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    private ProjectAchievement buildProjectAchievement(ProjectAchievementRequest ar, Project project, Achievement achievement) {
        return ProjectAchievement.builder()
                .project(project)
                .achievement(achievement)
                .impactSummary(ar.getImpactSummary())
                .demoUrl(ar.getDemoUrl())
                .metricBefore(ar.getMetricBefore())
                .metricAfter(ar.getMetricAfter())
                .build();
    }

    // ─────────────────── RESOLVE / FIND-OR-CREATE ─────────────────

    private Profile getProfile() {
        return profileRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new NoSuchElementException("Profile not found"));
    }

    private Project findProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Project not found"));
    }

    private ProjectCategory resolveProjectCategory(Long categoryId, String categoryName) {
        if (categoryId != null) {
            return projectCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NoSuchElementException("Project category not found"));
        }
        if (categoryName != null && !categoryName.isBlank()) {
            return projectCategoryRepository.findByName(categoryName)
                    .orElseGet(() -> projectCategoryRepository.save(
                            ProjectCategory.builder().name(categoryName).build()
                    ));
        }
        return null;
    }

    private SkillCategory resolveOrCreateSkillCategory(Long categoryId, String categoryName) {
        if (categoryId != null) {
            return skillCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NoSuchElementException("Skill category not found"));
        }
        if (categoryName != null && !categoryName.isBlank()) {
            return skillCategoryRepository.findByName(categoryName)
                    .orElseGet(() -> skillCategoryRepository.save(
                            SkillCategory.builder().name(categoryName).build()
                    ));
        }
        return null;
    }

    private Skill resolveOrCreateSkill(ProjectSkillRequest sr, Profile profile) {
        if (sr.getSkillId() != null) {
            return skillRepository.findById(sr.getSkillId())
                    .orElseThrow(() -> new NoSuchElementException("Skill not found: " + sr.getSkillId()));
        }

        Optional<Skill> existing = skillRepository.findByProfileAndName(profile, sr.getSkillName());
        if (existing.isPresent()) return existing.get();

        SkillCategory skillCategory = resolveOrCreateSkillCategory(sr.getSkillCategoryId(), sr.getSkillCategoryName());

        Skill skill = Skill.builder()
                .profile(profile)
                .name(sr.getSkillName())
                .category(skillCategory)
                .proficiency(sr.getProficiency() != null ? sr.getProficiency() : ProficiencyLevel.BEGINNER)
                .level(sr.getLevel() != null ? sr.getLevel() : 1)
                .yearsOfExperience(sr.getYearsOfExperience() != null ? sr.getYearsOfExperience() : BigDecimal.ZERO)
                .description(sr.getSkillDescription())
                .lastUsedDate(sr.getLastUsedDate())
                .hasCertification(Boolean.TRUE.equals(sr.getHasCertification()))
                .learning(Boolean.TRUE.equals(sr.getLearning()))
                .sortOrder(sr.getSortOrder())
                .build();

        skill = skillRepository.save(skill);

        if (sr.getLearningProgresses() != null) {
            for (LearningProgressRequest lpr : sr.getLearningProgresses()) {
                skill.addLearningProgress(LearningProgress.builder()
                        .skill(skill)
                        .name(lpr.getName())
                        .status(lpr.getStatus() != null ? lpr.getStatus() : LearningStatus.NOT_STARTED)
                        .startDate(lpr.getStartDate())
                        .completionDate(lpr.getCompletionDate())
                        .progressPercentage(lpr.getProgressPercentage() != null ? lpr.getProgressPercentage() : 0)
                        .timeSpentHours(lpr.getTimeSpentHours() != null ? lpr.getTimeSpentHours() : BigDecimal.ZERO)
                        .estimatedCompletion(lpr.getEstimatedCompletion())
                        .description(lpr.getDescription())
                        .build());
            }
            skill = skillRepository.save(skill);
        }

        return skill;
    }

    private Achievement resolveOrCreateAchievement(ProjectAchievementRequest ar, Profile profile) {
        if (ar.getAchievementId() != null) {
            return achievementRepository.findById(ar.getAchievementId())
                    .orElseThrow(() -> new NoSuchElementException("Achievement not found: " + ar.getAchievementId()));
        }
        return achievementRepository.save(Achievement.builder()
                .profile(profile)
                .title(ar.getTitle())
                .description(ar.getDescription())
                .achievementType(ar.getAchievementType())
                .achievementDate(ar.getAchievementDate())
                .recognitionLevel(ar.getRecognitionLevel() != null ? ar.getRecognitionLevel() : RecognitionLevel.LOCAL)
                .recognizedBy(ar.getRecognizedBy())
                .proofUrl(ar.getProofUrl())
                .isFeatured(Boolean.TRUE.equals(ar.getIsFeatured()))
                .sortOrder(ar.getSortOrder())
                .build());
    }

    /**
     * After URL resolution: every new slot (no id) must have a URL, and no primary item may be a video.
     */
    private void validateMedia(List<ProjectMediaRequest> media) {
        if (media == null) return;
        for (ProjectMediaRequest mr : media) {
            if (mr.getId() == null && (mr.getImageUrl() == null || mr.getImageUrl().isBlank())) {
                throw new IllegalArgumentException(
                        "Each new media item requires an imageUrl or a file in the 'files' multipart part");
            }
            if (Boolean.TRUE.equals(mr.getPrimary()) && mr.getImageUrl() != null && isVideo(mr.getImageUrl())) {
                throw new IllegalArgumentException("Primary media must be an image, not a video");
            }
        }
    }

    private boolean isVideo(String url) {
        if (url.contains("/video/upload/")) return true;
        String lower = url.toLowerCase();
        return lower.endsWith(".mp4") || lower.endsWith(".mov") || lower.endsWith(".avi")
                || lower.endsWith(".webm") || lower.endsWith(".mkv") || lower.endsWith(".ogg");
    }

    /**
     * Pairs incoming files with media slots that have no URL yet.
     * Files are consumed in order — index 0 fills the first blank slot, index 1 the second, etc.
     * Slots that already carry a URL (existing media on update) are left untouched.
     */
    private void resolveMediaUrls(List<ProjectMediaRequest> media, List<MultipartFile> files) {
        if (media == null || files == null || files.isEmpty()) return;
        int fileIdx = 0;
        for (ProjectMediaRequest mr : media) {
            if ((mr.getImageUrl() == null || mr.getImageUrl().isBlank()) && fileIdx < files.size()) {
                mr.setImageUrl(cloudinaryService.uploadMedia(files.get(fileIdx++)));
            }
        }
    }

    private void enforceSinglePrimary(List<ProjectMediaRequest> media) {
        boolean found = false;
        for (ProjectMediaRequest mr : media) {
            if (Boolean.TRUE.equals(mr.getPrimary())) {
                if (found) mr.setPrimary(false);
                else found = true;
            }
        }
    }

    private ProjectDetailDto buildDetailDto(Project project) {
        ProjectDetailDto dto = projectMapper.toProjectDetailDto(project);

        dto.setFeatures(project.getFeatures().stream()
                .sorted(Comparator.comparingInt(f -> f.getSortOrder() != null ? f.getSortOrder() : Integer.MAX_VALUE))
                .map(projectMapper::toProjectFeatureDto)
                .toList());

        dto.setChallenges(project.getChallenges().stream()
                .map(projectMapper::toProjectChallengeDto)
                .toList());

        dto.setMedia(project.getMedia().stream()
                .sorted(Comparator.comparingInt(m -> m.getSortOrder() != null ? m.getSortOrder() : Integer.MAX_VALUE))
                .map(projectMapper::toProjectMediaDto)
                .toList());

        dto.setSkills(project.getSkills().stream()
                .map(projectMapper::toSkillInProjectDto)
                .toList());

        dto.setMetrics(project.getMetrics() != null
                ? projectMapper.toProjectMetricsDto(project.getMetrics())
                : null);

        dto.setAchievements(project.getAchievements().stream()
                .map(projectMapper::toProjectAchievementDto)
                .toList());

        return dto;
    }
}
