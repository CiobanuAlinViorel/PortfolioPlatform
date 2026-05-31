package com.example.portofolio.service.core;

import com.example.portofolio.entity.Education;
import com.example.portofolio.entity.SkillCategory;
import com.example.portofolio.entity.enums.EducationStatus;
import com.example.portofolio.repository.*;
import com.example.portofolio.service.base.ServiceUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


import com.example.portofolio.dto.*;
import com.example.portofolio.entity.EntityMetadata;
import com.example.portofolio.entity.Skill;
import com.example.portofolio.entity.enums.EntityType;
import com.example.portofolio.service.base.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@Slf4j
public class SkillService extends BaseService<Skill, Long, SkillRepository> {

    private final EntityMetadataRepository entityMetadataRepository;
    private final SkillCategoryRepository skillCategoryRepository;
    private final ProjectRepository projectRepository;
    private final CertificateRepository certificateRepository;
    private final EducationRepository educationRepository;

    @Autowired
    public SkillService(SkillRepository skillRepository,
                        EntityMetadataRepository entityMetadataRepository,
                        SkillCategoryRepository skillCategoryRepository,
                        ProjectRepository projectRepository,
                        CertificateRepository certificateRepository,
                        EducationRepository educationRepository) {
        super(skillRepository);
        this.entityMetadataRepository = entityMetadataRepository;
        this.skillCategoryRepository = skillCategoryRepository;
        this.projectRepository = projectRepository;
        this.certificateRepository = certificateRepository;
        this.educationRepository = educationRepository;
    }


    @Cacheable(value = "skills", key = "#personalId")
    public List<SkillDto> findByPersonalId(@Valid @NotNull @Positive Long personalId) {
        log.debug("Finding skills for personal ID: {}", personalId);


        if (personalId == null || personalId <= 0) {
            throw new IllegalArgumentException("Personal ID must be positive");
        }

        List<Skill> skills = repository.findByPersonalIdWithCategoryAndTags(personalId);
        return skills.stream().map(this::toSkillDto).toList();
    }

    // ===== DTO CONVERSION =====

    @Override
    protected SkillDto toDto(Skill skill) {
        return toSkillDto(skill);
    }

    /**
     * Taking over all categories of skills (without skills)
     */
    @Cacheable(value = "allSkillCategories")
    public List<FeaturedSkillCategoryDto> getAllSkillCategories() {
        log.debug("Getting all skill categories");


        List<SkillCategory> allCategories = skillCategoryRepository.findAllWithParentAndIcon();


        return allCategories.stream()
                .map(category -> FeaturedSkillCategoryDto.builder()
                        .name(category.getName())
                        .description(category.getDescription())
                        .icon(category.getIcon() != null ?
                                category.getIcon().getName() :
                                getDefaultCategoryIcon(category.getName()))
                        .skills(null)
                        .build())
                .toList();
    }

    private String getDefaultCategoryIcon(String categoryName) {
        if (categoryName == null) return "star";

        return switch (categoryName.toLowerCase()) {
            case "technical skills" -> "code";
            case "communication" -> "users";
            case "management" -> "briefcase";
            case "problem solving" -> "puzzle-piece";
            case "personal development" -> "trending-up";
            case "leadership" -> "crown";
            default -> "star";
        };
    }

    private SkillDto toSkillDto(Skill skill) {
        Optional<EntityMetadata> metadata = entityMetadataRepository
                .findByEntityTypeAndEntityId(EntityType.SKILL, skill.getId());

        Integer projectCount = 10;

        return SkillDto.builder()
                .id(skill.getId().toString())
                .name(skill.getName())
                .level(skill.getLevel())
                .proficiency(skill.getProficiency() != null ?
                        skill.getProficiency().toString().toLowerCase() : null)
                .description(skill.getDescription())
                .yearsOfExperience(skill.getYearsOfExperience() != null ?
                        skill.getYearsOfExperience().doubleValue() : null)
                .projects(projectCount)
                .icon(metadata.map(em -> em.getIcon() != null ? em.getIcon().getName() : null).orElse(null))
                .color(metadata.map(EntityMetadata::getPrimaryColor).orElse(null))
                .category(skill.getCategory() != null ? skill.getCategory().getName() : null)
                .build();
    }

    // ===== FEATURED SKILLS =====

    @Cacheable(value = "featuredSkills", key = "#personalId")
    public List<FeaturedSkillDto> findFeaturedSkills(@Valid @NotNull @Positive Long personalId) {
        log.debug("Finding featured skills for personal ID: {}", personalId);

        if (personalId == null || personalId <= 0) {
            throw new IllegalArgumentException("Personal ID must be positive");
        }

        List<Skill> skills = new ArrayList<>();
        return skills.stream().map(this::toFeaturedSkillDto).toList();
    }

    private FeaturedSkillDto toFeaturedSkillDto(Skill skill) {
        Optional<EntityMetadata> metadata = entityMetadataRepository
                .findByEntityTypeAndEntityId(EntityType.SKILL, skill.getId());

        List<String> projectTitles = new ArrayList<>();

        return FeaturedSkillDto.builder()
                .id(skill.getId().toString())
                .name(skill.getName())
                .level(skill.getLevel())
                .categoryName(skill.getCategory() != null ? skill.getCategory().getName() : null)
                .icon(metadata.map(em -> em.getIcon() != null ? em.getIcon().getName() : null).orElse(null))
                .description(skill.getDescription())
                .yearsOfExperience(skill.getYearsOfExperience() != null ?
                        skill.getYearsOfExperience().doubleValue() : null)
                .color(metadata.map(EntityMetadata::getPrimaryColor).orElse(null))
                .projects(projectTitles)
                .proficiency(skill.getProficiency() != null ?
                        skill.getProficiency().toString().toLowerCase() : null)
                .trending(skill.getTrending())
                .learning(skill.getLearning())
                .build();
    }



    // ===== STATISTICS =====

    // Replace the getSkillStatistics method in SkillService with this:

    @Cacheable(value = "heroStats", key = "#personalId")
    public SkillsHeroStatsDto getHeroStats(@Valid @NotNull @Positive Long personalId) {
        log.debug("Getting hero statistics for personal ID: {}", personalId);

        if (personalId == null || personalId <= 0) {
            throw new IllegalArgumentException("Personal ID must be positive");
        }


        Integer yearsCoding = calculateYearsCoding(personalId);


        Long totalProjects = projectRepository.countByPersonalId(personalId);


        Long totalCertifications = certificateRepository.countByPersonalId(personalId);


        Double avgLevel = repository.findAverageSkillLevelByPersonalId(personalId);
        double avgProficiencyPercent = avgLevel != null ? avgLevel : 0.0;


        Integer totalTechnologies = 10;


        String yearsCodingText = yearsCoding + "+";
        String certificationsText = totalCertifications.toString();
        String avgProficiencyText = Math.round(avgProficiencyPercent) + "%";


        String description = buildHeroDescription(personalId);
        String projectsBuiltText = totalProjects + "+ projects built";
        String technologiesMasteredText = totalTechnologies + " technologies mastered";

        return SkillsHeroStatsDto.builder()
                .description(description)
                .projectsText(projectsBuiltText)
                .technologiesText(technologiesMasteredText)
                .yearsCoding(yearsCodingText)
                .projects(totalProjects.toString())
                .certifications(certificationsText)
                .avgProficiency(avgProficiencyText)
                .yearsCodingLabel("YEARS CODING")
                .projectsLabel("PROJECTS")
                .certificationsLabel("CERTIFICATIONS")
                .avgProficiencyLabel("AVG. PROFICIENCY")
                .build();
    }

    /**
     * Calculate your programming years based on your first project or skill
     */
    private Integer calculateYearsCoding(Long personalId) {

        Integer oldestProjectYear = projectRepository.findOldestProjectYear(personalId);


        LocalDate oldestSkillDate = repository.findOldestSkillDate(personalId);


        int currentYear = LocalDate.now().getYear();
        int yearsFromProjects = oldestProjectYear != null ? currentYear - oldestProjectYear : 0;
        int yearsFromSkills = oldestSkillDate != null ? currentYear - oldestSkillDate.getYear() : 0;


        int yearsCoding = Math.max(yearsFromProjects, yearsFromSkills);


        return Math.max(yearsCoding, 1);
    }

    /**
     * Build the main description for the hero section
     */
    private String buildHeroDescription(Long personalId) {

        List<Education> ongoingEducations = educationRepository.findByPersonalIdAndStatus(personalId, EducationStatus.ONGOING);
        Optional<Education> currentEducation = ongoingEducations.stream().findFirst();

        String specialization = "Computer Science Economics"; // Default
        if (currentEducation.isPresent() && currentEducation.get().getFieldOfStudy() != null) {
            specialization = currentEducation.get().getFieldOfStudy();
        }

        return specialization + " student with expertise in modern web technologies";
    }

// ===== KEEP THE ORIGINAL METHOD FOR COMPATIBILITY =====

    /**
     * Returns top 5 level-based skills
     * Respect TopSkill interface: {name, level, color}
     */
    @Cacheable(value = "topSkills", key = "#personalId + '_' + #limit")
    public List<TopSkillDto> getTopSkills(@Valid @NotNull @Positive Long personalId,
                                          @Valid @Positive Integer limit) {
        ServiceUtils.logMethodEntry("getTopSkills", personalId, limit);
        ServiceUtils.validatePersonalId(personalId);


        int skillLimit = limit != null ? limit : 5;
        Pageable pageable = PageRequest.of(0, skillLimit);


        List<Object[]> results = new ArrayList<>();

        List<TopSkillDto> topSkills = results.stream()
                .map(row -> {
                    Skill skill = (Skill) row[0];
                    EntityMetadata metadata = (EntityMetadata) row[1];


                    Integer projectCount = 10;


                    String color = getSkillColor(skill, metadata);

                    return TopSkillDto.builder()
                            .name(skill.getName())
                            .level(skill.getLevel())
                            .color(color)
                            // Optional fields
                            .category(skill.getCategory() != null ? skill.getCategory().getName() : null)
                            .proficiency(skill.getProficiency() != null ?
                                    skill.getProficiency().toString().toLowerCase() : null)
                            .projects(projectCount)
                            .build();
                })
                .toList();

        log.debug("Found {} top skills for personalId: {}", topSkills.size(), personalId);
        ServiceUtils.logMethodExit("getTopSkills", topSkills.size());
        return topSkills;
    }

    /**
     * Helper method to determine the color of the skill
     */
    private String getSkillColor(Skill skill, EntityMetadata metadata) {

        if (metadata != null && metadata.getPrimaryColor() != null) {
            return metadata.getPrimaryColor();
        }


        if (skill.getLevel() != null) {
            return getColorByLevel(skill.getLevel());
        }


        if (skill.getCategory() != null) {
            return getColorByCategory(skill.getCategory().getName());
        }

        // 4. Default
        return "#3B82F6"; // Blue default
    }

    /**
     * Helper method for level based colors
     */
    private String getColorByLevel(Integer level) {
        if (level >= 90) return "#10B981"; // Green - Expert
        if (level >= 70) return "#F59E0B"; // Yellow - Advanced
        if (level >= 50) return "#3B82F6"; // Blue - Intermediate
        return "#6B7280"; // Gray - Beginner
    }

    /**
     * Helper method for category-based colors
     */
    private String getColorByCategory(String categoryName) {
        if (categoryName == null) return "#3B82F6";

        return switch (categoryName.toLowerCase()) {
            case "technical skills", "programming" -> "#10B981"; // Green
            case "frameworks", "libraries" -> "#3B82F6"; // Blue
            case "databases" -> "#F59E0B"; // Yellow
            case "tools", "devops" -> "#8B5CF6"; // Purple
            case "soft skills", "communication" -> "#EC4899"; // Pink
            default -> "#6B7280"; // Gray
        };
    }
}


