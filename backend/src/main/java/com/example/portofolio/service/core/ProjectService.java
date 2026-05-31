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

/**
 * Optimized Project Service with ServiceUtils
 */
@Service
@Slf4j
public class ProjectService extends BaseService<Project, Long, ProjectRepository> {

    private final EntityMetadataRepository entityMetadataRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository,
                          EntityMetadataRepository entityMetadataRepository) {
        super(projectRepository);
        this.entityMetadataRepository = entityMetadataRepository;

    }

    @Override
    protected ProjectExportDto toDto(Project project) {
        return toProjectExportDto(project);
    }


    // ===== LIVE PROJECTS =====

    @Cacheable(value = "liveProjects", key = "#personalId")
    public List<FeaturedProjectDto> findLiveProjects(@Valid @NotNull @Positive Long personalId) {
        ServiceUtils.logMethodEntry("findLiveProjects", personalId);
        ServiceUtils.validatePersonalId(personalId);

        List<Project> projects = repository.findByPersonalId(personalId);


        List<Project> liveProjects = projects.stream()
                .filter(project -> project.getDemoUrl() != null &&
                        !project.getDemoUrl().trim().isEmpty())
                .toList();

        List<FeaturedProjectDto> result = ServiceUtils.safeMap(liveProjects, this::toFeaturedProjectDto);

        log.debug("Found {} live projects out of {} total projects for personalId: {}",
                result.size(), projects.size(), personalId);

        ServiceUtils.logMethodExit("findLiveProjects", result.size());
        return result;
    }
    // ===== CORE PROJECT QUERIES =====

    @Cacheable(value = "projectsByPersonal", key = "#personalId")
    public List<ProjectExportDto> findByPersonalId(@Valid @NotNull @Positive Long personalId) {
        ServiceUtils.logMethodEntry("findByPersonalId", personalId);
        ServiceUtils.validatePersonalId(personalId);

        List<Project> projects = repository.findByPersonalId(personalId);
        List<ProjectExportDto> result = ServiceUtils.safeMap(projects, this::toProjectExportDto);

        ServiceUtils.logMethodExit("findByPersonalId", result.size());
        return result;
    }

    @Cacheable(value = "featuredProjects", key = "#personalId")
    public List<FeaturedProjectDto> findFeaturedProjects(@Valid @NotNull @Positive Long personalId) {
        ServiceUtils.logMethodEntry("findFeaturedProjects", personalId);
        ServiceUtils.validatePersonalId(personalId);

        List<Project> allProjects = repository.findByPersonalId(personalId);
        List<Project> featuredProjects = ServiceUtils.filterAndMap(
                allProjects,
                project -> {
                    Optional<EntityMetadata> metadata = entityMetadataRepository
                            .findByEntityTypeAndEntityId(EntityType.PROJECT, project.getId());
                    return ServiceUtils.isFeatured(metadata);
                },
                java.util.function.Function.identity()
        );

        List<FeaturedProjectDto> result = ServiceUtils.safeMap(featuredProjects, this::toFeaturedProjectDto);
        ServiceUtils.logMethodExit("findFeaturedProjects", result.size());
        return result;
    }


    // ===== DTO CONVERSION WITH SERVICEUTILS =====

    private ProjectExportDto toProjectExportDto(Project project) {
        Optional<EntityMetadata> metadata = entityMetadataRepository
                .findByEntityTypeAndEntityId(EntityType.PROJECT, project.getId());

        List<String> technologies = new ArrayList<String>();

        return ProjectExportDto.builder()
                .id(project.getId().toString())
                .title(project.getTitle())
                .description(project.getDescription())
                .longDescription(project.getDescription()) // Use same for now
                .technologies(technologies)
                .category("")
                .status(ServiceUtils.enumToLowerString(project.getStatus()))
                .featured(ServiceUtils.isFeatured(metadata))
                .images(getProjectImages(project))
                .demoUrl(project.getDemoUrl())
                .githubUrl(project.getGithubUrl())
                .features(getProjectFeatures(project))
                .challenges(getProjectChallenges(project))
                .developmentTime(project.getDevelopmentTime())
                .complexity(ServiceUtils.enumToLowerString(project.getComplexity()))
                .metrics(project.getMetrics() != null ? toProjectMetricsDto(project.getMetrics()) : null)
                .tags(ServiceUtils.processTags(project.getTags().toString()))
                .year(project.getYear())
                .primaryColor(ServiceUtils.getColorFromMetadata(metadata, "#3B82F6"))
                .secondaryColor(ServiceUtils.getColorFromMetadata(metadata, "#93C5FD"))
                .build();
    }

    private FeaturedProjectDto toFeaturedProjectDto(Project project) {
        Optional<EntityMetadata> metadata = entityMetadataRepository
                .findByEntityTypeAndEntityId(EntityType.PROJECT, project.getId());

        List<String> technologies = new ArrayList<>();

        return FeaturedProjectDto.builder()
                .id(project.getId().toString())
                .title(project.getTitle())
                .description(project.getDescription())
                .shortDescription(ServiceUtils.generateShortDescription(project.getDescription(), 150))
                .technologies(technologies)
                .image(getPrimaryImage(project))
                .githubUrl(project.getGithubUrl())
                .liveUrl(project.getDemoUrl())
                .featured(ServiceUtils.isFeatured(metadata))
                .category("")
                .primaryColor(ServiceUtils.getColorFromMetadata(metadata, "#3B82F6"))
                .secondaryColor(ServiceUtils.getColorFromMetadata(metadata, "#93C5FD"))
                .build();
    }

    private ProjectMetricsDto toProjectMetricsDto(ProjectMetrics metrics) {
        return ProjectMetricsDto.builder()
                .users(metrics.getUsersCount() != null ? metrics.getUsersCount() : 0L)
                .performance(metrics.getPerformanceScore())
                .codeQuality(metrics.getCodeQualityScore())
                .lines(metrics.getLinesOfCode() != null ? metrics.getLinesOfCode() : 0L)
                .commits(metrics.getCommitsCount())
                .testCoverage(metrics.getTestCoveragePercentage())
                .lastUpdated(ServiceUtils.formatDateAsIso(LocalDate.from(metrics.getLastUpdated())))
                .build();
    }

    // ===== HELPER METHODS =====

    private List<String> getProjectImages(Project project) {
        if (project.getImages() == null || project.getImages().isEmpty()) {
            return List.of();
        }
        return project.getImages().stream()
                .map(ProjectImage::getImageUrl)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private String getPrimaryImage(Project project) {
        if (project.getImages() == null || project.getImages().isEmpty()) {
            return null;
        }
        return project.getImages().stream()
                .filter(img -> img.getPrimary() != null && img.getPrimary())
                .findFirst()
                .map(ProjectImage::getImageUrl)
                .orElse(project.getImages().iterator().next().getImageUrl());
    }

    private List<String> getProjectFeatures(Project project) {
        if (project.getFeatures() == null || project.getFeatures().isEmpty()) {
            return List.of();
        }
        return project.getFeatures().stream()
                .map(ProjectFeature::getTitle)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private List<String> getProjectChallenges(Project project) {
        if (project.getChallenges() == null || project.getChallenges().isEmpty()) {
            return List.of();
        }
        return project.getChallenges().stream()
                .map(ProjectChallenge::getDescription)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    // ===== METADATA SUPPORT =====

    @Override
    public List<Project> findFeatured() {
        return repository.findAll().stream()
                .filter(project -> {
                    Optional<EntityMetadata> metadata = entityMetadataRepository
                            .findByEntityTypeAndEntityId(EntityType.PROJECT, project.getId());
                    return ServiceUtils.isFeatured(metadata);
                })
                .toList();
    }


    /**
     * Returns the distribution of project categories with percentages
     * Exactly as in your image - each category with number and percentage
     */
    @Cacheable(value = "projectCategoryDistribution", key = "#personalId")
    public List<ProjectCategoryDistributionDto> getProjectCategoryDistribution(@Valid @NotNull @Positive Long personalId) {
        ServiceUtils.logMethodEntry("getProjectCategoryDistribution", personalId);
        ServiceUtils.validatePersonalId(personalId);


        List<Object[]> distribution = new ArrayList<>();
        Long totalProjects = repository.getTotalProjectCount(personalId);

        log.debug("Found {} categories with {} total projects for personalId: {}",
                distribution.size(), totalProjects, personalId);


        List<ProjectCategoryDistributionDto> result = distribution.stream()
                .map(row -> {
                    String category = (String) row[0];
                    Long count = ((Number) row[1]).longValue();

                    // Calculează procentajul
                    Double percentage = totalProjects > 0 ?
                            (count.doubleValue() / totalProjects.doubleValue()) * 100.0 : 0.0;

                    return ProjectCategoryDistributionDto.builder()
                            .category(category)
                            .projectCount(count)
                            .percentage(percentage)
                            .build();
                })
                .toList();

        ServiceUtils.logMethodExit("getProjectCategoryDistribution", result.size());
        return result;
    }


    /**
     * Calculate project-based development experience
     * Exactly as in your image - Years Active, Avg. Complexity, Success Rate
     */
    @Cacheable(value = "developmentExperience", key = "#personalId")
    public DevelopmentExperienceDto getDevelopmentExperience(@Valid @NotNull @Positive Long personalId) {
        ServiceUtils.logMethodEntry("getDevelopmentExperience", personalId);
        ServiceUtils.validatePersonalId(personalId);


        Integer firstYear = repository.findOldestProjectYear(personalId);
        Integer latestYear = repository.findNewestProjectYear(personalId);
        int currentYear = LocalDate.now().getYear();

        int yearsActive = 0;
        if (firstYear != null) {

            Integer endYear = latestYear != null ? Math.max(latestYear, currentYear) : currentYear;
            yearsActive = endYear - firstYear + 1;
        }

        log.debug("Years calculation: first={}, latest={}, active={}", firstYear, latestYear, yearsActive);


        List<Object[]> complexityDistribution = repository.findComplexityDistribution(personalId);
        String avgComplexity = "INTERMEDIATE"; // default

        if (!complexityDistribution.isEmpty()) {
            Object[] mostFrequent = complexityDistribution.get(0);
            ComplexityLevel complexityEnum = (ComplexityLevel) mostFrequent[0];
            avgComplexity = complexityEnum.name();

            log.debug("Complexity distribution: {}, most frequent: {}",
                    complexityDistribution.size(), avgComplexity);
        }


        Long totalProjects = repository.countByPersonalId(personalId);
        Long deployedProjects = repository.countDeployedProjects(personalId);
        Long liveProjects = repository.countLiveProjects(personalId);




        double successRate = 0.0;
        if (totalProjects > 0) {
            successRate = (deployedProjects.doubleValue() / totalProjects.doubleValue()) * 100.0;
        }

        log.debug("Success rate calculation: deployed={}, live={}, total={}, rate={}%",
                deployedProjects, liveProjects, totalProjects, successRate);

        DevelopmentExperienceDto result = DevelopmentExperienceDto.builder()
                .yearsActive(yearsActive)
                .firstProjectYear(firstYear)
                .latestProjectYear(latestYear)
                .avgComplexity(avgComplexity)
                .successRate(successRate)
                .deployedProjects(deployedProjects)
                .totalProjects(totalProjects)
                .liveProjects(liveProjects)
                .build();

        ServiceUtils.logMethodExit("getDevelopmentExperience", result);
        return result;
    }

}

