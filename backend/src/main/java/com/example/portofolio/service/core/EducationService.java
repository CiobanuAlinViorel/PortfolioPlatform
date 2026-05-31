package com.example.portofolio.service.core;

import com.example.portofolio.dto.*;
import com.example.portofolio.entity.*;
import com.example.portofolio.entity.enums.*;
import com.example.portofolio.repository.*;
import com.example.portofolio.service.base.BaseService;
import com.example.portofolio.service.base.ServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Optimized Education Service with ServiceUtils
 */
@Service
@Slf4j
public class EducationService extends BaseService<Education, Long, EducationRepository> {

    private final EntityMetadataRepository entityMetadataRepository;
    private final AchievementRepository achievementRepository;
    private final CourseRepository courseRepository;
    private final CourseProjectRepository courseProjectRepository;
    private final ProjectRepository projectRepository;
    private final SkillRepository skillRepository;

    @Autowired
    public EducationService(EducationRepository educationRepository,
                            EntityMetadataRepository entityMetadataRepository,
                            AchievementRepository achievementRepository,
                            CourseRepository courseRepository,
                            CourseProjectRepository courseProjectRepository,
                            SkillRepository skillRepository,
                            ProjectRepository projectRepository) {
        super(educationRepository);
        this.entityMetadataRepository = entityMetadataRepository;
        this.achievementRepository = achievementRepository;
        this.courseRepository = courseRepository;
        this.courseProjectRepository = courseProjectRepository;

        this.projectRepository = projectRepository;
        this.skillRepository = skillRepository;
    }


    @Override
    protected EducationDto toDto(Education education) {
        return toEducationDto(education);
    }

    // ===== CORE EDUCATION QUERIES =====

    @Cacheable(value = "educationByPersonal", key = "#personalId")
    public List<EducationDto> findByPersonalId(@Valid @NotNull @Positive Long personalId) {
        ServiceUtils.logMethodEntry("findByPersonalId", personalId);
        ServiceUtils.validatePersonalId(personalId);

        List<Education> educations = repository.findByPersonalIdWithCoursesAndAchievements(personalId);
        List<EducationDto> result = educations.stream()
                .map(this::toEducationDto)
                .toList();

        ServiceUtils.logMethodExit("findByPersonalId", result.size());
        return result;
    }

    // ===== COURSE MANAGEMENT =====

    public List<CourseDto> findRelevantCourses(@Valid @NotNull @Positive Long educationId) {
        ServiceUtils.logMethodEntry("findRelevantCourses", educationId);
        ServiceUtils.validateEntityId(educationId);

        List<Course> courses = courseRepository.findByEducationIdAndRelevantTrue(educationId);
        List<CourseDto> result = courses.stream()
                .map(this::toCourseDto)
                .toList();

        ServiceUtils.logMethodExit("findRelevantCourses", result.size());
        return result;
    }


    // ===== ACHIEVEMENTS =====

    public List<AchievementDto> findEducationAchievements(@Valid @NotNull @Positive Long personalId,
                                                          @Valid @NotNull @Positive Long educationId) {
        ServiceUtils.logMethodEntry("findEducationAchievements", personalId, educationId);
        ServiceUtils.validatePersonalId(personalId);
        ServiceUtils.validateEntityId(educationId);

        List<Achievement> achievements = null;
        List<AchievementDto> result = null;

        ServiceUtils.logMethodExit("findEducationAchievements", result.size());
        return result;
    }

    // ===== DTO CONVERSION WITH SERVICEUTILS =====

    private EducationDto toEducationDto(Education education) {
        Optional<EntityMetadata> metadata = entityMetadataRepository
                .findByEntityTypeAndEntityId(EntityType.EDUCATION, education.getId());

        List<AchievementDto> achievements = findEducationAchievements(
                education.getPersonal().getId(), education.getId());

        List<CourseDto> relevantCourses = findRelevantCourses(education.getId());

        String period = ServiceUtils.formatPeriod(education.getStartDate(), education.getEndDate());

        return EducationDto.builder()
                .id(education.getId().toString())
                .level(ServiceUtils.enumToLowerString(education.getLevel()))
                .institution(education.getInstitution())
                .degree(education.getDegree())
                .field(education.getFieldOfStudy())
                .period(period)
                .location(education.getLocation())
                .description(education.getDescription())
                .achievements(achievements)
                .relevantCourses(relevantCourses)
                .status(ServiceUtils.enumToLowerString(education.getStatus()))
                .gpa(education.getGpa())
                .highlights(getEducationHighlights())
                .icon(ServiceUtils.getIconFromMetadata(metadata, "graduation-cap"))
                .primaryColor(ServiceUtils.getColorFromMetadata(metadata, "#10B981"))
                .secondaryColor(ServiceUtils.getColorFromMetadata(metadata, "#A7F3D0"))
                .build();
    }

    private CourseDto toCourseDto(Course course) {
        return CourseDto.builder()
                .id(course.getId().toString())
                .title(course.getTitle())
                .description(ServiceUtils.truncateText(course.getDescription(), 200))
                .grade(course.getGrade())
                .credits(course.getCredits())
                .semester(course.getSemester())
                .year(course.getYear())
                .relevant(course.getRelevant())
                .build();
    }

    private AchievementDto toAchievementDto(Achievement achievement) {
        Optional<EntityMetadata> metadata = entityMetadataRepository
                .findByEntityTypeAndEntityId(EntityType.ACHIEVEMENT, achievement.getId());

        String formattedDate = ServiceUtils.formatDateAsIso(achievement.getAchievementDate());

        return AchievementDto.builder()
                .id(achievement.getId().toString())
                .title(achievement.getTitle())
                .description(ServiceUtils.truncateText(achievement.getDescription(), 300))
                .date(formattedDate)
                .type(ServiceUtils.enumToLowerString(achievement.getAchievementType()))
                .icon(ServiceUtils.getIconFromMetadata(metadata, "award"))
                .primaryColor(ServiceUtils.getColorFromMetadata(metadata, "#F59E0B"))
                .secondaryColor(ServiceUtils.getColorFromMetadata(metadata, "#FDE68A"))
                .recognitionLevel(ServiceUtils.enumToLowerString(achievement.getRecognitionLevel()))
                .build();
    }

    /**
     * Returns all academic projects for a person from all stages of education
     */
    @Cacheable(value = "academicProjects", key = "#personalId")
    public List<AcademicProjectDto> getAcademicProjects(@Valid @NotNull @Positive Long personalId) {
        ServiceUtils.logMethodEntry("getAcademicProjects", personalId);
        ServiceUtils.validatePersonalId(personalId);

        // Obține toate proiectele academice prin relația CourseProject DIRECT
        List<CourseProject> courseProjects = courseProjectRepository.findByPersonalIdWithDetails(personalId);

        List<AcademicProjectDto> result = courseProjects.stream()
                .map(this::toAcademicProjectDto)
                .collect(Collectors.toList());

        log.debug("Found {} academic projects for personalId: {}", result.size(), personalId);
        ServiceUtils.logMethodExit("getAcademicProjects", result.size());
        return result;
    }

    /**
     * Helper method for DTO conversion
     */
    private AcademicProjectDto toAcademicProjectDto(CourseProject courseProject) {
        Project project = courseProject.getProject();
        Course course = courseProject.getCourse();

        // Obține tehnologiile pentru proiect
        List<String> technologies = null;

        // Obține metadata pentru culori și icon
        Optional<EntityMetadata> metadata = entityMetadataRepository
                .findByEntityTypeAndEntityId(EntityType.PROJECT, project.getId());

        return AcademicProjectDto.builder()
                .id(project.getId().toString())
                .title(project.getTitle())
                .courseName(course.getTitle())
                .description(project.getDescription())
                .technologies(technologies)
                .duration(project.getDevelopmentTime())
                .type( "Academic")
                .githubLink(project.getGithubUrl())
                .icon(getProjectIcon(project, metadata))
                .primaryColor(metadata.map(EntityMetadata::getPrimaryColor).orElse("#3B82F6"))
                .secondaryColor(metadata.map(EntityMetadata::getSecondaryColor).orElse("#93C5FD"))
                .build();
    }

    /**
     * Helper method pentru determinarea icon-ului proiectului
     */
    private String getProjectIcon(Project project, Optional<EntityMetadata> metadata) {
        // 1. Încearcă din metadata
        if (metadata.isPresent() && metadata.get().getIcon() != null) {
            return metadata.get().getIcon().getName();
        }

        // 2. Icon bazat pe categoria proiectului
        if (project.getProjectCategory().getName() != null) {
            return "something";
        }

        // 3. Icon bazat pe titlul proiectului
        return getIconByTitle(project.getTitle());
    }

    /**
     * Helper method pentru icon bazat pe categorie
     */
    private String getIconByCategory(String category) {
        if (category == null) return "folder";

        return switch (category.toLowerCase()) {
            case "web", "frontend" -> "globe";
            case "mobile", "app" -> "smartphone";
            case "desktop" -> "monitor";
            case "game", "gaming" -> "gamepad-2";
            case "ai", "machine learning" -> "brain";
            case "data", "database" -> "database";
            case "api", "backend" -> "server";
            case "security" -> "shield";
            default -> "folder";
        };
    }

    /**
     * Helper method pentru icon bazat pe titlu
     */
    private String getIconByTitle(String title) {
        if (title == null) return "folder";

        String lowerTitle = title.toLowerCase();
        if (lowerTitle.contains("web") || lowerTitle.contains("website")) return "globe";
        if (lowerTitle.contains("mobile") || lowerTitle.contains("app")) return "smartphone";
        if (lowerTitle.contains("game")) return "gamepad-2";
        if (lowerTitle.contains("ai") || lowerTitle.contains("machine")) return "brain";
        if (lowerTitle.contains("data") || lowerTitle.contains("analytics")) return "bar-chart";
        if (lowerTitle.contains("api")) return "server";
        if (lowerTitle.contains("security")) return "shield";
        if (lowerTitle.contains("chat") || lowerTitle.contains("message")) return "message-circle";
        if (lowerTitle.contains("shop") || lowerTitle.contains("store")) return "shopping-cart";

        return "folder"; // Default
    }

    // ===== HELPER METHODS =====

    private List<HighlightDto> getEducationHighlights() {
        // For now, return empty list - implement based on actual Highlight entity structure
        return List.of();
    }


    // ===== METADATA SUPPORT =====

    @Override
    public List<Education> findFeatured() {
        return repository.findAll().stream()
                .filter(education -> {
                    Optional<EntityMetadata> metadata = entityMetadataRepository
                            .findByEntityTypeAndEntityId(EntityType.EDUCATION, education.getId());
                    return ServiceUtils.isFeatured(metadata);
                })
                .toList();
    }


    /**
     * Returns the current year of study for ongoing education
     * For completed education returns null
     */
    public String getCurrentYearOfStudy(@Valid @NotNull @Positive Long personalId) {
        ServiceUtils.logMethodEntry("getCurrentYearOfStudy", personalId);
        ServiceUtils.validatePersonalId(personalId);

        List<Education> ongoingEducations = repository.findByPersonalIdAndStatus(personalId, EducationStatus.ONGOING);
        Optional<Education> currentEducation = ongoingEducations.stream().findFirst();

        if (currentEducation.isEmpty()) {
            log.debug("No ongoing education found for personalId: {}", personalId);
            return null;
        }

        Education education = currentEducation.get();

        log.debug("Found ongoing education: id={}, startDate={}, endDate={}, status={}",
                education.getId(), education.getStartDate(), education.getEndDate(), education.getStatus());

        // For ONGOING education, We ignore END_DATE
        // end_date for ONGOING is just an estimation/planning, doesn't mean that is completed

        LocalDate startDate = education.getStartDate();
        LocalDate currentDate = LocalDate.now();

        if (startDate == null) {
            log.warn("Start date is null for education id: {}", education.getId());
            return "1";
        }


        int yearsDifference = currentDate.getYear() - startDate.getYear();

        if (currentDate.getMonthValue() < startDate.getMonthValue() ||
                (currentDate.getMonthValue() == startDate.getMonthValue() &&
                        currentDate.getDayOfMonth() < startDate.getDayOfMonth())) {
            yearsDifference--;
        }

        int currentYear = yearsDifference + 1;

        if (currentYear < 1) {
            currentYear = 1;
        }

        if (currentYear > 6) {
            currentYear = 6;
        }

        String result = String.valueOf(currentYear);

        log.debug("Calculated current year: startDate={}, currentDate={}, yearsDifference={}, finalYear={}",
                startDate, currentDate, yearsDifference, result);

        ServiceUtils.logMethodExit("getCurrentYearOfStudy", result);
        return result;
    }

    @Cacheable(value = "academicStats", key = "#personalId")
    public AcademicStatsDto getAcademicStats(@Valid @NotNull @Positive Long personalId) {
        ServiceUtils.logMethodEntry("getAcademicStats", personalId);
        ServiceUtils.validatePersonalId(personalId);


        List<Education> ongoingEducations = repository.findByPersonalIdAndStatus(personalId, EducationStatus.ONGOING);
        Optional<Education> currentEducation = ongoingEducations.stream().findFirst();

        if (currentEducation.isEmpty()) {
            log.debug("No ongoing education (in the stats case) found for personalId: {}", personalId );
            return AcademicStatsDto.builder()
                    .totalCourses(0)
                    .currentYear("0")
                    .specialization("No current education")
                    .focusAreas(List.of())
                    .languages(List.of())
                    .build();
        }

        Education education = currentEducation.get();
        Integer totalCourses = getTotalCoursesForEducation(education.getId());
        String currentYear = getCurrentYearOfStudy(personalId);
        String specialization = education.getFieldOfStudy() != null ?
                education.getFieldOfStudy() :
                education.getDegree();
        List<String> focusAreas = getTopProjectCategories(personalId);
        List<AcademicLanguageDto> languages = getForeignLanguages(personalId);

        AcademicStatsDto result = AcademicStatsDto.builder()
                .totalCourses(totalCourses)
                .currentYear(currentYear != null ? currentYear : "0")
                .specialization(specialization)
                .focusAreas(focusAreas)
                .languages(languages)
                .build();

        log.debug("Academic stats for personalId {}: {} courses, year {}, specialization: {}",
                personalId, totalCourses, currentYear, specialization);

        ServiceUtils.logMethodExit("getAcademicStats", result);
        return result;
    }

    /**
     * Get the total number of courses for an education
     */
    private Integer getTotalCoursesForEducation(Long educationId) {
        log.debug("Getting total courses for educationId: {}", educationId);

        Long count = courseRepository.countByEducationId(educationId);
        log.debug("Found {} courses for educationId: {}", count, educationId);

        return Math.toIntExact(count);
    }

    /**
     * Get top N categories of projects as focus areas
     */
    private List<String> getTopProjectCategories(Long personalId) {
        List<Object[]> categoryDistribution = new ArrayList<>();

        return categoryDistribution.stream()
                .limit(4)
                .map(row -> (String) row[0])
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Get foreign languages from skills with the category "Foreign Languages"
     */
    private List<AcademicLanguageDto> getForeignLanguages(Long personalId) {
        List<Skill> languageSkills = skillRepository.findByPersonalIdAndCategoryName(personalId, "Foreign Languages");

        return languageSkills.stream()
                .map(this::mapSkillToLanguage)
                .collect(Collectors.toList());
    }

    /**
     * Map a language skill at AcademicLanguageDto
     */
    private AcademicLanguageDto mapSkillToLanguage(Skill skill) {
        String skillName = skill.getName().toLowerCase();

        String emoji = getLanguageEmoji(skillName);
        String level = extractLanguageLevel(skill);

        return AcademicLanguageDto.builder()
                .name(skill.getName())
                .level(level)
                .icon(emoji)
                .iconType("emoji")
                .build();
    }

    /**
     * Get the emoji for a language
     */
    private String getLanguageEmoji(String languageName) {
        return switch (languageName.toLowerCase()) {
            case "english" -> "🇬🇧";
            case "french", "français" -> "🇫🇷";
            case "german", "deutsch" -> "🇩🇪";
            case "spanish", "español" -> "🇪🇸";
            case "italian", "italiano" -> "🇮🇹";
            case "portuguese", "português" -> "🇵🇹";
            case "russian", "русский" -> "🇷🇺";
            case "chinese", "中文" -> "🇨🇳";
            case "japanese", "日本語" -> "🇯🇵";
            case "korean", "한국어" -> "🇰🇷";
            case "arabic", "العربية" -> "🇸🇦";
            case "romanian", "română" -> "🇷🇴";
            case "hungarian", "magyar" -> "🇭🇺";
            default -> "🌍"; // Default global icon
        };
    }

    /**
     * Extract the language level from the skill (description or proficiency)
     */
    private String extractLanguageLevel(Skill skill) {

        if (skill.getDescription() != null) {
            String desc = skill.getDescription().toUpperCase();
            if (desc.contains("A1")) return "A1";
            if (desc.contains("A2")) return "A2";
            if (desc.contains("B1")) return "B1";
            if (desc.contains("B2")) return "B2";
            if (desc.contains("C1")) return "C1";
            if (desc.contains("C2")) return "C2";
            if (desc.contains("NATIVE")) return "Native";
            if (desc.contains("FLUENT")) return "C2";
        }


        if (skill.getProficiency() != null) {
            return switch (skill.getProficiency()) {
                case BEGINNER -> "A1-A2";
                case INTERMEDIATE -> "B1-B2";
                case ADVANCED -> "C1";
                case EXPERT -> "C2";
            };
        }


        if (skill.getLevel() != null) {
            int level = skill.getLevel();
            if (level >= 90) return "C2";
            if (level >= 80) return "C1";
            if (level >= 70) return "B2";
            if (level >= 60) return "B1";
            if (level >= 40) return "A2";
            return "A1";
        }

        return "Unknown";
    }
}
