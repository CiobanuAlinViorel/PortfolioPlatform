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

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Technology Service with ServiceUtils
 */
@Service
@Slf4j
public class TechnologyService extends BaseService<Technology, Long, TechnologyRepository> {

    private final EntityMetadataRepository entityMetadataRepository;
    private final TechnologyCategoryRepository technologyCategoryRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final SkillRepository skillRepository;
    private final CertificateRepository certificateRepository;

    @Autowired
    public TechnologyService(TechnologyRepository technologyRepository,
                             EntityMetadataRepository entityMetadataRepository,
                             TechnologyCategoryRepository technologyCategoryRepository,
                             ProjectRepository projectRepository,
                             CertificateRepository certificateRepository,
                             LearningProgressRepository learningProgressRepository,
                             SkillRepository skillRepository) {
        super(technologyRepository);
        this.entityMetadataRepository = entityMetadataRepository;
        this.technologyCategoryRepository = technologyCategoryRepository;

        this.learningProgressRepository = learningProgressRepository;
        this.skillRepository = skillRepository;
        this.certificateRepository = certificateRepository;
    }


    @Override
    protected TechnologyDto toDto(Technology technology) {
        return toTechnologyDto(technology);
    }

    // ===== CORE TECHNOLOGY QUERIES =====

    @Cacheable(value = "allTechnologies")
    public List<TechnologyDto> findAllTechnologies() {
        ServiceUtils.logMethodEntry("findAllTechnologies");

        List<Technology> technologies = repository.findAll();
        List<TechnologyDto> result = technologies.stream()
                .map(this::toTechnologyDto)
                .toList();

        ServiceUtils.logMethodExit("findAllTechnologies", result.size());
        return result;
    }

    // ===== TRENDING & POPULAR TECHNOLOGIES =====

    @Cacheable(value = "trendingTechnologies")
    public List<TechnologyDto> findTrendingTechnologies() {
        ServiceUtils.logMethodEntry("findTrendingTechnologies");

        List<Technology> technologies = repository.findTrendingTechnologies();
        List<TechnologyDto> result = technologies.stream()
                .map(this::toTechnologyDto)
                .toList();

        ServiceUtils.logMethodExit("findTrendingTechnologies", result.size());
        return result;
    }


    // ===== VERSION MANAGEMENT =====

    public List<TechnologyDto> findRecentlyReleased(@Valid @NotNull Integer days) {
        ServiceUtils.logMethodEntry("findRecentlyReleased", days);

        if (days <= 0) {
            throw new IllegalArgumentException("Days must be positive");
        }

        LocalDate since = LocalDate.now().minusDays(days);
        List<Technology> technologies = repository.findRecentlyReleased(since);
        List<TechnologyDto> result = technologies.stream()
                .map(this::toTechnologyDto)
                .toList();

        ServiceUtils.logMethodExit("findRecentlyReleased", result.size());
        return result;
    }


    // ===== STATISTICS =====

    @Cacheable(value = "technologyStatsByCategory")
    public Map<String, Long> getTechnologyStatsByCategory() {
        ServiceUtils.logMethodEntry("getTechnologyStatsByCategory");

        List<Object[]> results = new ArrayList<>();
        Map<String, Long> stats = results.stream().collect(Collectors.toMap(
                row -> (String) row[0],
                row -> ((Number) row[1]).longValue()
        ));

        ServiceUtils.logMethodExit("getTechnologyStatsByCategory", stats.size());
        return stats;
    }

    @Cacheable(value = "technologyStats")
    public TechnologyStatisticsDto getTechnologyStatistics() {
        ServiceUtils.logMethodEntry("getTechnologyStatistics");

        Long totalTechnologies = count();
        Long trendingCount = repository.countTrendingTechnologies();
        Double avgPopularityScore = repository.findAveragePopularityScore();
        Map<String, Long> categoryStats = getTechnologyStatsByCategory();

        TechnologyStatisticsDto result = TechnologyStatisticsDto.builder()
                .totalTechnologies(totalTechnologies)
                .trendingCount(trendingCount)
                .averagePopularityScore(avgPopularityScore != null ? avgPopularityScore : 0.0)
                .categoryDistribution(categoryStats)
                .recentlyReleasedCount((long) findRecentlyReleased(30).size())
                .build();

        ServiceUtils.logMethodExit("getTechnologyStatistics", result);
        return result;
    }

    // ===== CATEGORY MANAGEMENT =====

    @Cacheable(value = "technologyCategoriesWithCount")
    public List<TechCategoryInfoDto> getCategoriesWithCount() {
        ServiceUtils.logMethodEntry("getCategoriesWithCount");

        List<Object[]> results = new ArrayList<>();
        List<TechCategoryInfoDto> categories = results.stream()
                .map(row -> {
                    TechnologyCategory category = (TechnologyCategory) row[0];


                    return TechCategoryInfoDto.builder()
                            .id(category.getId().toString())
                            .name(category.getName())
                            .description(ServiceUtils.truncateText(category.getDescription(), 200))
                            .icon(category.getIcon() != null ? category.getIcon().getName() : getDefaultIconForCategory(category.getName()))
                            .color(getColorForCategory(category.getName()))
                            .bgColor(getBgColorForCategory(category.getName()))
                            .build();
                })
                .toList();

        ServiceUtils.logMethodExit("getCategoriesWithCount", categories.size());
        return categories;
    }




    // ===== DTO CONVERSION =====

    private TechnologyDto toTechnologyDto(Technology technology) {
        return toTechnologyDtoForPersonal(technology);
    }

    private TechnologyDto toTechnologyDtoForPersonal(Technology technology) {
        Optional<EntityMetadata> metadata = entityMetadataRepository
                .findByEntityTypeAndEntityId(EntityType.TECHNOLOGY, technology.getId());

        // Get usage information
        Integer projectCount = getProjectCountForPersonal(technology.getId());

        // Get proficiency and experience for personal usage
        String proficiency = calculateProficiencyForPersonal(technology.getId());

        Double yearsOfExperience = calculateExperienceForPersonal(technology.getId());

        // Get features
        List<String> features = getTechnologyFeatures(technology);

        return TechnologyDto.builder()
                .id(technology.getId().toString())
                .name(technology.getName())
                .category(technology.getTechnologyCategory() != null ? technology.getTechnologyCategory().getName() : "Other")
                .proficiency(proficiency)
                .level(ServiceUtils.proficiencyToLevel(proficiency))
                .yearsOfExperience(yearsOfExperience)
                .projects(projectCount)
                .description(ServiceUtils.truncateText(technology.getDescription(), 300))
                .icon(ServiceUtils.getIconFromMetadata(metadata, getDefaultIconForTechnology(technology.getName())))
                .color(ServiceUtils.getColorFromMetadata(metadata, getColorForTechnology(technology)))
                .backgroundColor(ServiceUtils.getColorFromMetadata(metadata, getBgColorForTechnology(technology)))
                .features(features)
                .trending(technology.getTrending())
                .certification(hasCertification(technology.getId()))
                .learning(isCurrentlyLearning(technology.getId()))
                .build();
    }

    // ===== HELPER METHODS =====

    private List<String> getTechnologyFeatures(Technology technology) {
        if (technology.getFeatures() == null || technology.getFeatures().isEmpty()) {
            return List.of();
        }
        return technology.getFeatures().stream()
                .filter(feature -> !feature.getDeprecated())
                .map(TechnologyFeature::getTitle)
                .filter(Objects::nonNull)
                .toList();
    }

    private Integer getProjectCountForPersonal(Long technologyId) {


       return null;
    }

    private String calculateProficiencyForPersonal(Long technologyId) {

        Integer projectCount = getProjectCountForPersonal(technologyId);

        if (projectCount >= 5) return "expert";
        if (projectCount >= 3) return "advanced";
        if (projectCount >= 1) return "intermediate";
        return "beginner";
    }

    private Double calculateExperienceForPersonal(Long technologyId) {

        // Simplified calculation - could be enhanced with actual project dates
        Integer projectCount = getProjectCountForPersonal(technologyId);
        return Math.min(projectCount * 0.5, 5.0); // Max 5 years experience
    }


    private Boolean hasCertification(Long technologyId) {
        if (technologyId == null) {
            return false;
        }

        try {
            // Get the technology to check its name
            Optional<Technology> technologyOpt = repository.findById(technologyId);
            if (technologyOpt.isEmpty()) {
                return false;
            }

            Technology technology = technologyOpt.get();
            String techName = technology.getName().toLowerCase();

            // Find certificates for this personal that might be related to the technology
            List<Certificate> certificates = certificateRepository.findByPersonalIdAndVerifiedTrue(1L);

            // Check if any certificate is related to this technology
            return certificates.stream().anyMatch(cert -> {
                String certName = cert.getName().toLowerCase();
                String certDescription = cert.getDescription() != null ? cert.getDescription().toLowerCase() : "";
                String provider = cert.getProvider().toLowerCase();

                // Direct name match
                if (certName.contains(techName)) {
                    return true;
                }

                // Description contains technology name
                if (certDescription.contains(techName)) {
                    return true;
                }

                // Check for common technology-provider combinations
                return isProviderKnownForTechnology(provider, techName);
            });

        } catch (Exception e) {
            log.error("Error checking certification for technology {} and personal {}: {}",
                    technologyId, 1L, e.getMessage());
            return false;
        }
    }

    private Boolean isCurrentlyLearning(Long technologyId) {
        if (technologyId == null) {
            return false;
        }

        try {
            // Get the technology to check its name
            Optional<Technology> technologyOpt = repository.findById(technologyId);
            if (technologyOpt.isEmpty()) {
                return false;
            }

            Technology technology = technologyOpt.get();
            String techName = technology.getName().toLowerCase();

            // Find skills related to this technology for the personal
            List<Skill> relatedSkills = skillRepository.findByPersonalId(1L).stream()
                    .filter(skill -> {
                        String skillName = skill.getName().toLowerCase();
                        return skillName.contains(techName) ||
                                (skill.getDescription() != null &&
                                        skill.getDescription().toLowerCase().contains(techName));
                    })
                    .toList();

            if (relatedSkills.isEmpty()) {
                return false;
            }

            // Check if any of these skills have active learning progress
            return relatedSkills.stream().anyMatch(skill -> {
                List<LearningProgress> activeProgress = learningProgressRepository
                        .findBySkillIdAndStatus(skill.getId(), LearningStatus.IN_PROGRESS);

                return !activeProgress.isEmpty() &&
                        activeProgress.stream().anyMatch(progress ->
                                progress.getProgressPercentage() < 100 &&
                                        progress.getCompletionDate() == null);
            });

        } catch (Exception e) {
            log.error("Error checking learning status for technology {} and personal {}: {}",
                    technologyId, 1L, e.getMessage());
            return false;
        }
    }

    private boolean isProviderKnownForTechnology(String provider, String techName) {
        if (provider == null || techName == null) {
            return false;
        }

        // Create a map of known technology-provider relationships
        // Using Map.of() has a limit of 10 entries, so we'll use a builder pattern
        Map<String, List<String>> techProviders = new HashMap<>();

        // Programming Languages & Frameworks
        techProviders.put("java", List.of("oracle", "sun", "redhat", "ibm"));
        techProviders.put("python", List.of("python", "psf", "jetbrains"));
        techProviders.put("javascript", List.of("mozilla", "ecma"));
        techProviders.put("typescript", List.of("microsoft"));
        techProviders.put("node", List.of("nodejs", "node"));
        techProviders.put("react", List.of("meta", "facebook"));
        techProviders.put("angular", List.of("google"));
        techProviders.put("vue", List.of("vue"));
        techProviders.put("spring", List.of("pivotal", "vmware", "spring"));

        // Cloud & Infrastructure
        techProviders.put("aws", List.of("amazon", "aws"));
        techProviders.put("microsoft", List.of("microsoft", "azure", "office"));
        techProviders.put("google", List.of("google", "gcp"));
        techProviders.put("docker", List.of("docker", "mirantis"));
        techProviders.put("kubernetes", List.of("cncf", "kubernetes", "redhat"));
        techProviders.put("terraform", List.of("hashicorp"));
        techProviders.put("ansible", List.of("redhat"));
        techProviders.put("puppet", List.of("puppet"));
        techProviders.put("chef", List.of("chef"));

        // Databases
        techProviders.put("mongodb", List.of("mongodb", "mongo"));
        techProviders.put("postgresql", List.of("postgresql", "postgres"));
        techProviders.put("mysql", List.of("mysql", "oracle"));
        techProviders.put("redis", List.of("redis", "redislabs"));
        techProviders.put("elasticsearch", List.of("elastic"));

        // DevOps & Tools
        techProviders.put("jenkins", List.of("cloudbees", "jenkins"));
        techProviders.put("git", List.of("git", "github", "gitlab", "atlassian"));
        techProviders.put("jira", List.of("atlassian"));
        techProviders.put("confluence", List.of("atlassian"));

        // Business Applications
        techProviders.put("salesforce", List.of("salesforce"));
        techProviders.put("tableau", List.of("tableau"));
        techProviders.put("powerbi", List.of("microsoft"));

        // Methodologies & Certifications
        techProviders.put("scrum", List.of("scrum", "scrumalliance", "scrum.org"));
        techProviders.put("agile", List.of("scaled", "safe", "agile"));
        techProviders.put("pmp", List.of("pmi", "project management institute"));
        techProviders.put("itil", List.of("axelos", "itil"));

        // Hardware & Networking
        techProviders.put("cisco", List.of("cisco"));
        techProviders.put("comptia", List.of("comptia"));

        // Operating Systems
        techProviders.put("linux", List.of("redhat", "suse", "canonical", "lpi"));
        techProviders.put("ubuntu", List.of("canonical"));
        techProviders.put("centos", List.of("redhat"));

        // Check if the provider is known for this technology
        return techProviders.entrySet().stream()
                .anyMatch(entry -> {
                    String techKey = entry.getKey();
                    List<String> providers = entry.getValue();

                    // Check if technology name contains the key (case-insensitive)
                    boolean techMatches = techName.toLowerCase().contains(techKey);

                    // Check if any provider matches (case-insensitive)
                    boolean providerMatches = providers.stream()
                            .anyMatch(knownProvider -> provider.toLowerCase().contains(knownProvider.toLowerCase()));

                    return techMatches && providerMatches;
                });
    }
    // ===== COLOR & ICON HELPERS =====

    private String getColorForTechnology(Technology technology) {
        if (technology.getTrending()) return "#8B5CF6"; // Purple for trending
        if (technology.getPopularityScore() != null && technology.getPopularityScore() >= 80) {
            return "#10B981"; // Green for popular
        }
        return "#6B7280"; // Gray default
    }

    private String getBgColorForTechnology(Technology technology) {
        if (technology.getTrending()) return "#F3E8FF"; // Light Purple
        if (technology.getPopularityScore() != null && technology.getPopularityScore() >= 80) {
            return "#D1FAE5"; // Light Green
        }
        return "#F9FAFB"; // Light Gray
    }

    private String getColorForCategory(String categoryName) {
        return switch (categoryName.toLowerCase()) {
            case "frontend", "ui/ux" -> "#3B82F6"; // Blue
            case "backend", "server" -> "#10B981"; // Green
            case "database" -> "#F59E0B"; // Amber
            case "cloud", "devops" -> "#8B5CF6"; // Purple
            case "mobile" -> "#EF4444"; // Red
            case "ai/ml", "data science" -> "#06B6D4"; // Cyan
            default -> "#6B7280"; // Gray
        };
    }

    private String getBgColorForCategory(String categoryName) {
        return switch (categoryName.toLowerCase()) {
            case "frontend", "ui/ux" -> "#DBEAFE"; // Light Blue
            case "backend", "server" -> "#D1FAE5"; // Light Green
            case "database" -> "#FEF3C7"; // Light Amber
            case "cloud", "devops" -> "#F3E8FF"; // Light Purple
            case "mobile" -> "#FEE2E2"; // Light Red
            case "ai/ml", "data science" -> "#CFFAFE"; // Light Cyan
            default -> "#F9FAFB"; // Light Gray
        };
    }

    private String getDefaultIconForTechnology(String technologyName) {
        if (technologyName == null) return "code";

        String lowerName = technologyName.toLowerCase();
        if (lowerName.contains("react") || lowerName.contains("vue") || lowerName.contains("angular")) {
            return "component";
        }
        if (lowerName.contains("java") || lowerName.contains("python") || lowerName.contains("javascript")) {
            return "code";
        }
        if (lowerName.contains("database") || lowerName.contains("sql") || lowerName.contains("mongo")) {
            return "database";
        }
        if (lowerName.contains("cloud") || lowerName.contains("aws") || lowerName.contains("azure")) {
            return "cloud";
        }
        return "code";
    }

    private String getDefaultIconForCategory(String categoryName) {
        return switch (categoryName.toLowerCase()) {
            case "frontend" -> "layout";
            case "backend" -> "server";
            case "database" -> "database";
            case "cloud" -> "cloud";
            case "mobile" -> "smartphone";
            case "ai/ml" -> "brain";
            default -> "code";
        };
    }

    // ===== METADATA SUPPORT =====

    @Override
    public List<Technology> findFeatured() {
        return repository.findTrendingTechnologies(); // Use trending as featured
    }

}


