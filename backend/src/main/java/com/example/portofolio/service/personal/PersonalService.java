package com.example.portofolio.service.personal;

import com.example.portofolio.dto.*;
import com.example.portofolio.entity.*;
import com.example.portofolio.entity.enums.EntityType;
import com.example.portofolio.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PersonalService {

    // Repository dependencies bazate pe entitățile existente
    private final PersonalRepository personalRepository;
    private final ProjectRepository projectRepository;
    private final SkillRepository skillRepository;
    private final CertificateRepository certificateRepository;
    private final AchievementRepository achievementRepository;
    private final EducationRepository educationRepository;
    private final ContactInfoRepository contactInfoRepository;
    private final EntityMetadataRepository entityMetadataRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final FutureGoalRepository futureGoalRepository;
    private final HighlightRepository highlightRepository;
    private final PersonalValueRepository personalValueRepository;
    private final PersonalityTraitRepository personalityTraitRepository;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM yyyy");

    // ===== BASIC PERSONAL OPERATIONS =====

    public Optional<Personal> findById(Long id) {
        return personalRepository.findById(id);
    }

    public List<Personal> findAll() {
        return personalRepository.findAll();
    }

    @Transactional
    public Personal save(Personal personal) {
        log.info("Saving personal profile: {} {}", personal.getFirstName(), personal.getLastName());
        return personalRepository.save(personal);
    }

    @Transactional
    public void deleteById(Long id) {
        log.info("Deleting personal profile with id: {}", id);
        personalRepository.deleteById(id);
    }

    // ===== HERO SECTION DATA =====

    public HeroTotalFeaturesDto getHeroTotalFeatures(Long personalId) {
        log.debug("Getting hero total features for personal: {}", personalId);

        // Count projects
        Integer nrOfProjects = Math.toIntExact(projectRepository.countByPersonalId(personalId));

        // Count unique technologies used in projects
        Integer nrOfTechnologies = 10;
        // Count academic years from education
        Integer nrAcademicYears = educationRepository.countAcademicYearsByPersonalId(personalId);

        // Count certificates
        Integer nrOfCertifications = Math.toIntExact(certificateRepository.countByPersonalId(personalId));

        return HeroTotalFeaturesDto.builder()
                .nrOfProjects(nrOfProjects)
                .nrOfTechnologies(nrOfTechnologies != null ? nrOfTechnologies : 0)
                .nrAcademicYears(nrAcademicYears != null ? nrAcademicYears : 0)
                .nrOfCertifications(nrOfCertifications)
                .build();
    }

    public HomeStoryHighLightsDto getHomeStoryHighlights(Long personalId) {
        log.debug("Getting home story highlights for personal: {}", personalId);

        // Get current education for role
        Education currentEducation = educationRepository.findCurrentByPersonalId(personalId);
        String currentRole = currentEducation != null ? currentEducation.getDegree() : "Student";

        // Get location from contact info
        Optional<ContactInfo> contactInfo = contactInfoRepository.findByPersonalId(personalId);
        String location = "Unknown";
        if (contactInfo.isPresent() && contactInfo.get().getContactLocation() != null) {
            ContactLocation contactLocation = contactInfo.get().getContactLocation();
            location = contactLocation.getCity() + ", " + contactLocation.getCountry();
        }

        // Get education institution
        String education = currentEducation != null ? currentEducation.getInstitution() : "";

        return HomeStoryHighLightsDto.builder()
                .currentRole(currentRole)
                .location(location)
                .education(education)
                .passion("Technology & Innovation") // Poate fi făcut dinamic din personal description
                .goal("Building impactful software solutions") // Similar
                .build();
    }

    // ===== FEATURED CONTENT =====

    public List<FeaturedStatDto> getFeaturedStats(Long personalId) {
        log.debug("Getting featured stats for personal: {}", personalId);

        return List.of(
                FeaturedStatDto.builder()
                        .value(String.valueOf(projectRepository.countByPersonalId(personalId)))
                        .label("Projects")
                        .icon("Code")
                        .build(),
                FeaturedStatDto.builder()
                        .value(String.valueOf(skillRepository.countByPersonalId(personalId)))
                        .label("Skills")
                        .icon("Star")
                        .build(),
                FeaturedStatDto.builder()
                        .value(String.valueOf(certificateRepository.countByPersonalId(personalId)))
                        .label("Certificates")
                        .icon("Award")
                        .build(),
                FeaturedStatDto.builder()
                        .value(String.valueOf(achievementRepository.countByPersonalId(personalId)))
                        .label("Achievements")
                        .icon("Trophy")
                        .build()
        );
    }

    // ===== TIMELINE AND LEARNING =====

    public List<TimelineMilestoneDto> getTimelineMilestones(Long personalId) {
        log.debug("Getting timeline milestones for personal: {}", personalId);

        List<TimelineMilestoneDto> milestones = new java.util.ArrayList<>();

        // Add achievements to timeline
        achievementRepository.findByPersonalIdOrderByAchievementDateDesc(personalId)
                .forEach(achievement -> milestones.add(mapAchievementToTimeline(achievement)));

        // Add education milestones
        educationRepository.findByPersonalIdOrderByStartDateDesc(personalId)
                .forEach(education -> milestones.add(mapEducationToTimeline(education)));

        // Add major projects
        projectRepository.findMajorProjectsByPersonalId(personalId)
                .forEach(project -> milestones.add(mapProjectToTimeline(project)));

        // Sort by year descending
        return milestones.stream()
                .sorted((a, b) -> b.getYear().compareTo(a.getYear()))
                .collect(Collectors.toList());
    }

    public List<CurrentLearningDto> getCurrentLearning(Long personalId) {
        log.debug("Getting current learning for personal: {}", personalId);

        return learningProgressRepository.findActiveByPersonalId(personalId)
                .stream()
                .map(this::mapToCurrentLearningDto)
                .collect(Collectors.toList());
    }

    public List<FutureGoalDto> getFutureGoals(Long personalId) {
        log.debug("Getting future goals for personal: {}", personalId);

        return futureGoalRepository.findByPersonalIdOrderByPriorityDesc(personalId)
                .stream()
                .map(this::mapToFutureGoalDto)
                .collect(Collectors.toList());
    }
    /**
     * Returnează toate highlight-urile pentru o persoană
     */
    public List<HighlightDto> getPersonalHighlights(Long personalId) {
        log.debug("Getting personal highlights for personal: {}", personalId);

        List<Highlight> highlights = highlightRepository.findByPersonalIdWithIcon(personalId);

        return highlights.stream()
                .map(this::mapToHighlightDto)
                .collect(Collectors.toList());
    }

    /**
     * Returnează toate valorile personale pentru o persoană
     */
    public List<PersonalValueDto> getPersonalValues(Long personalId) {
        log.debug("Getting personal values for personal: {}", personalId);

        List<PersonalValue> personalValues = personalValueRepository.findByPersonalIdWithIcon(personalId);

        return personalValues.stream()
                .map(this::mapToPersonalValueDto)
                .collect(Collectors.toList());
    }



    // ===== PRIVATE MAPPING METHODS =====

    private TimelineMilestoneDto mapAchievementToTimeline(Achievement achievement) {
        EntityMetadata metadata = getEntityMetadata(EntityType.ACHIEVEMENT, achievement.getId());

        return TimelineMilestoneDto.builder()
                .id(achievement.getId().toString())
                .year(String.valueOf(achievement.getAchievementDate().getYear()))
                .title(achievement.getTitle())
                .category("achievement")
                .description(achievement.getDescription())
                .icon(metadata != null && metadata.getIcon() != null ? metadata.getIcon().getName() : "Award")
                .isActive(false)
                .primaryColor(metadata != null ? metadata.getPrimaryColor() : "#3B82F6")
                .secondaryColor(metadata != null ? metadata.getSecondaryColor() : "#1E40AF")
                .importance(metadata != null ? metadata.getImportance().toString().toLowerCase() : "medium")
                .build();
    }

    private TimelineMilestoneDto mapEducationToTimeline(Education education) {
        EntityMetadata metadata = getEntityMetadata(EntityType.EDUCATION, education.getId());

        return TimelineMilestoneDto.builder()
                .id(education.getId().toString())
                .year(String.valueOf(education.getStartDate().getYear()))
                .title(education.getDegree())
                .category("education")
                .description(education.getInstitution() + " - " + education.getFieldOfStudy())
                .duration(calculateEducationDuration(education))
                .isActive(education.getEndDate() == null)
                .icon(metadata != null && metadata.getIcon() != null ? metadata.getIcon().getName() : "GraduationCap")
                .primaryColor(metadata != null ? metadata.getPrimaryColor() : "#3B82F6")
                .secondaryColor(metadata != null ? metadata.getSecondaryColor() : "#1E40AF")
                .build();
    }



    private TimelineMilestoneDto mapProjectToTimeline(Project project) {
        EntityMetadata metadata = getEntityMetadata(EntityType.PROJECT, project.getId());

        return TimelineMilestoneDto.builder()
                .id(project.getId().toString())
                .year(String.valueOf(project.getYear()))
                .title(project.getTitle())
                .category("project")
                .description(project.getDescription())
                .technologies(getTechnologiesForProject(project.getId()))
                .duration(project.getDevelopmentTime() != null ? project.getDevelopmentTime() + " months" : null)
                .icon(metadata != null && metadata.getIcon() != null ? metadata.getIcon().getName() : "Code")
                .primaryColor(metadata != null ? metadata.getPrimaryColor() : "#3B82F6")
                .secondaryColor(metadata != null ? metadata.getSecondaryColor() : "#1E40AF")
                .build();
    }

    private CurrentLearningDto mapToCurrentLearningDto(LearningProgress learning) {
        EntityMetadata metadata = getEntityMetadata(EntityType.SKILL, learning.getSkill().getId());

        return CurrentLearningDto.builder()
                .id(learning.getId().toString())
                .title(learning.getName())
                .status(learning.getStatus().toString().toLowerCase())
                .progress(learning.getProgressPercentage())
                .description(learning.getDescription())
                .startDate(learning.getStartDate() != null ? learning.getStartDate().format(dateFormatter) : null)
                .expectedCompletion(learning.getEstimatedCompletion())
                .color(metadata != null ? metadata.getPrimaryColor() : "#3B82F6")
                .icon(metadata != null && metadata.getIcon() != null ? metadata.getIcon().getName() : "BookOpen")
                .gradient(metadata != null ? metadata.getGradient() : null)
                .build();
    }

    private FutureGoalDto mapToFutureGoalDto(FutureGoal goal) {
        EntityMetadata metadata = getEntityMetadata(EntityType.SKILL, goal.getId()); // Assuming goals can have metadata

        return FutureGoalDto.builder()
                .id(goal.getId().toString())
                .title(goal.getTitle())
                .description(goal.getDescription())
                .targetDate(goal.getTargetDate() != null ? goal.getTargetDate().format(dateFormatter) : null)
                .priority(goal.getPriority().toString().toLowerCase())
                .color(metadata != null ? metadata.getPrimaryColor() : "#3B82F6")
                .icon(goal.getIcon() != null ? goal.getIcon().getName() : "Target")
                .gradient(metadata != null ? metadata.getGradient() : null)
                .build();
    }

    private PersonalValueDto mapToPersonalValueDto(PersonalValue value) {
        return PersonalValueDto.builder()
                .id(value.getId().toString())
                .title(value.getTitle())
                .description(value.getDescription())
                .icon(value.getIcon() != null ? value.getIcon().getName() : "Heart")
                .importanceLevel(value.getImportanceLevel().toString().toLowerCase())
                .build();
    }

    private PersonalityTraitDto mapToPersonalityTraitDto(PersonalityTrait trait) {
        List<String> examples = trait.getExamples().stream()
                .map(PersonalityExample::getTitle)
                .collect(Collectors.toList());

        return PersonalityTraitDto.builder()
                .id(trait.getId().toString())
                .trait(trait.getTrait())
                .description(trait.getDescription())
                .icon(trait.getIcon() != null ? trait.getIcon().getName() : "User")
                .examples(examples)
                .build();
    }

    // Adaugă metoda de mapping pentru HighlightDto
    private HighlightDto mapToHighlightDto(Highlight highlight) {

        return HighlightDto.builder()
                .id(highlight.getId().toString())
                .title(highlight.getTitle())
                .description(highlight.getDescription())
                .highlightType(highlight.getHighlightType() != null ?
                        highlight.getHighlightType().toString().toLowerCase() : null)
                .priorityLevel(highlight.getPriorityLevel() != null ?
                        highlight.getPriorityLevel().toString().toLowerCase() : null)
                .icon(highlight.getIcon() != null ?
                        highlight.getIcon().getName() :
                        getDefaultHighlightIcon(highlight.getHighlightType()))
                .build();
    }

    // Helper methods pentru highlight-uri
    private String getDefaultHighlightIcon(Object highlightType) {
        if (highlightType == null) return "Star";

        String type = highlightType.toString().toLowerCase();
        return switch (type) {
            case "achievement" -> "Trophy";
            case "skill" -> "Code";
            case "project" -> "Folder";
            case "education" -> "GraduationCap";
            case "experience" -> "Briefcase";
            case "certification" -> "Award";
            default -> "Star";
        };
    }


    /**
     * Returns all personality traits for a person
     */
    public List<PersonalityTraitDto> getPersonalityTraits(Long personalId) {
        log.debug("Getting personality traits for personal: {}", personalId);

        List<PersonalityTrait> personalityTraits = personalityTraitRepository.findByPersonalIdWithIconAndExamples(personalId);

        return personalityTraits.stream()
                .map(this::mapToPersonalityTraitDto)
                .collect(Collectors.toList());
    }


    // ===== UTILITY METHODS =====

    private EntityMetadata getEntityMetadata(EntityType entityType, Long entityId) {
        return entityMetadataRepository.findByEntityTypeAndEntityId(entityType, entityId)
                .orElse(null);
    }

    private List<String> getTechnologiesForProject(Long projectId) {
        return new ArrayList<>();
    }

    private String calculateEducationDuration(Education education) {
        if (education.getEndDate() == null) {
            return "Ongoing";
        }
        long years = education.getStartDate().until(education.getEndDate()).getYears();
        return years + (years == 1 ? " year" : " years");
    }
}