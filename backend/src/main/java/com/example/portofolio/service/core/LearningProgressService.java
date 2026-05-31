package com.example.portofolio.service.core;

import com.example.portofolio.dto.LearningMilestoneDto;
import com.example.portofolio.dto.LearningProgressDto;
import com.example.portofolio.entity.*;
import com.example.portofolio.entity.enums.*;
import com.example.portofolio.repository.*;
import com.example.portofolio.service.base.BaseService;
import com.example.portofolio.service.base.ServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing learning progress
 */
@Service
@Slf4j
public class LearningProgressService extends BaseService<LearningProgress, Long, LearningProgressRepository> {

    private final EntityMetadataRepository entityMetadataRepository;
    private final SkillRepository skillRepository;
    private final ProjectRepository projectRepository;


    @Autowired
    public LearningProgressService(LearningProgressRepository learningProgressRepository,
                                   EntityMetadataRepository entityMetadataRepository,
                                   SkillRepository skillRepository,
                                   ProjectRepository projectRepository) {
        super(learningProgressRepository);
        this.entityMetadataRepository = entityMetadataRepository;
        this.skillRepository = skillRepository;
        this.projectRepository = projectRepository;

    }

    @Override
    protected LearningProgressDto toDto(LearningProgress learningProgress) {
        return toLearningProgressDto(learningProgress);
    }

    // ===== CORE QUERIES =====

    @Cacheable(value = "learningProgressByPersonal", key = "#personalId")
    public List<LearningProgressDto> findByPersonalId(@Valid @NotNull @Positive Long personalId) {
        ServiceUtils.logMethodEntry("findByPersonalId", personalId);
        ServiceUtils.validatePersonalId(personalId);

        List<LearningProgress> learningProgresses = repository.findByPersonalIdWithSkillAndCategory(personalId);
        List<LearningProgressDto> result = ServiceUtils.safeMap(learningProgresses, this::toLearningProgressDto);

        ServiceUtils.logMethodExit("findByPersonalId", result.size());
        return result;
    }


    /**
     * Returns all learning milestones for one person
     * Combines achievements and important projects in a unified list
     */
    @Cacheable(value = "learningMilestones", key = "#personalId")
    public List<LearningMilestoneDto> getLearningMilestones(@Valid @NotNull @Positive Long personalId) {
        ServiceUtils.logMethodEntry("getLearningMilestones", personalId);
        ServiceUtils.validatePersonalId(personalId);


        List<Project> completedProjects = projectRepository.findCompletedProjectsAsAchievements(personalId, Pageable.unpaged());

        List<LearningMilestoneDto> projectMilestones = completedProjects.stream()
                .map(project -> {

                    List<String> technologies = null;

                    return LearningMilestoneDto.builder()
                            .id(project.getId().toString())
                            .title(project.getTitle())
                            .year(project.getYear() != null ? project.getYear().toString() :
                                    project.getCompletionDate() != null ?
                                            String.valueOf(project.getCompletionDate().getYear()) :
                                            String.valueOf(LocalDate.now().getYear()))
                            .description(project.getDescription())
                            .technologies(technologies)
                            .build();
                })
                .toList();

        List<LearningMilestoneDto> allMilestones = new ArrayList<>(projectMilestones);


        List<Skill> expertSkills = skillRepository.findByPersonalId(personalId).stream()
                .filter(skill -> skill.getLevel() != null && skill.getLevel() >= 90)
                .toList();

        List<LearningMilestoneDto> skillMilestones = expertSkills.stream()
                .map(skill -> {

                    List<String> technologies = new ArrayList<>();



                        technologies = List.of(skill.getName());
                    

                    String year = skill.getUpdatedAt() != null ?
                            String.valueOf(skill.getUpdatedAt().getYear()) :
                            String.valueOf(LocalDate.now().getYear());

                    return LearningMilestoneDto.builder()
                            .id("skill_" + skill.getId().toString())
                            .title("Expert Level: " + skill.getName())
                            .year(year)
                            .description("Achieved expert level (90%+) in " + skill.getName())
                            .technologies(technologies)
                            .build();
                })
                .toList();

        allMilestones.addAll(skillMilestones);


        List<LearningProgress> completedLearning = repository.findCompletedLearningByPersonalId(personalId);

        List<LearningMilestoneDto> learningMilestones = completedLearning.stream()
                .map(learning -> {

                    List<String> technologies = new ArrayList<>();


                    technologies = List.of(learning.getSkill().getName());

                    String year = learning.getCompletionDate() != null ?
                            String.valueOf(learning.getCompletionDate().getYear()) :
                            String.valueOf(LocalDate.now().getYear());

                    return LearningMilestoneDto.builder()
                            .id("learning_" + learning.getId().toString())
                            .title("Completed: " + learning.getName())
                            .year(year)
                            .description(learning.getDescription())
                            .technologies(technologies)
                            .build();
                })
                .toList();

        allMilestones.addAll(learningMilestones);

        List<LearningMilestoneDto> result = allMilestones.stream()
                .sorted((m1, m2) -> {

                    try {
                        int year1 = Integer.parseInt(m1.getYear());
                        int year2 = Integer.parseInt(m2.getYear());
                        return Integer.compare(year2, year1);
                    } catch (NumberFormatException e) {
                        return m2.getYear().compareTo(m1.getYear());
                    }
                })
                .toList();

        log.debug("Found {} learning milestones for personalId: {}", result.size(), personalId);
        ServiceUtils.logMethodExit("getLearningMilestones", result.size());
        return result;
    }

    // ===== DTO CONVERSION =====

    private LearningProgressDto toLearningProgressDto(LearningProgress learningProgress) {
        Optional<EntityMetadata> skillMetadata = entityMetadataRepository
                .findByEntityTypeAndEntityId(EntityType.SKILL, learningProgress.getSkill().getId());

        String color = skillMetadata
                .map(EntityMetadata::getPrimaryColor)
                .orElse(getDefaultColorByProgress(learningProgress.getProgressPercentage()));

        String eta = null;
        if (learningProgress.getEstimatedCompletion() != null &&
                !learningProgress.getEstimatedCompletion().trim().isEmpty()) {
            try {
                eta = formatEta(LocalDateTime.parse(learningProgress.getEstimatedCompletion()));
            } catch (Exception e) {
                log.warn("Could not parse estimated completion date: {}",
                        learningProgress.getEstimatedCompletion(), e);
            }
        }

        return LearningProgressDto.builder()
                .id(learningProgress.getId().toString())
                .name(learningProgress.getName())
                .progress(learningProgress.getProgressPercentage())
                .color(color)
                .timeSpent(learningProgress.getTimeSpentHours() != null ?
                        learningProgress.getTimeSpentHours().intValue() : 0)
                .eta(eta)
                .description(learningProgress.getDescription())
                .build();
    }

    // ===== HELPER METHODS =====

    private String getDefaultColorByProgress(Integer progress) {
        if (progress == null) return "#6B7280"; // Gray

        if (progress >= 80) return "#10B981"; // Green - Almost done
        if (progress >= 60) return "#F59E0B"; // Yellow - Good progress
        if (progress >= 30) return "#3B82F6"; // Blue - Getting there
        return "#EF4444"; // Red - Just started
    }

    private String formatEta(LocalDateTime estimatedCompletion) {
        if (estimatedCompletion == null) return null;

        return estimatedCompletion.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    // ===== METADATA SUPPORT =====

    @Override
    public List<LearningProgress> findFeatured() {

        return repository.findAll().stream()
                .filter(lp -> lp.getProgressPercentage() != null && lp.getProgressPercentage() >= 80)
                .toList();
    }


}

