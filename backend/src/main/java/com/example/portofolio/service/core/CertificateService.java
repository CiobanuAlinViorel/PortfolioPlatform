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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Certificate Service with ServiceUtils
 */
@Service
@Slf4j
public class CertificateService extends BaseService<Certificate, Long, CertificateRepository> {

    private final EntityMetadataRepository entityMetadataRepository;


    @Autowired
    public CertificateService(CertificateRepository certificateRepository,
                              EntityMetadataRepository entityMetadataRepository) {
        super(certificateRepository);
        this.entityMetadataRepository = entityMetadataRepository;

    }



    @Override
    protected CertificateDto toDto(Certificate certificate) {
        return toCertificateDto(certificate);
    }

    // ===== CORE CERTIFICATE QUERIES =====

    @Cacheable(value = "certificatesByPersonal", key = "#personalId")
    public List<CertificateDto> findByPersonalId(@Valid @NotNull @Positive Long personalId) {
        ServiceUtils.logMethodEntry("findByPersonalId", personalId);
        ServiceUtils.validatePersonalId(personalId);

        List<Certificate> certificates = new ArrayList<>();
        List<CertificateDto> result = certificates.stream()
                .map(this::toCertificateDto)
                .toList();

        ServiceUtils.logMethodExit("findByPersonalId", result.size());
        return result;
    }


    @Cacheable(value = "featuredCertificates", key = "#personalId")
    public List<CertificateDto> findFeaturedCertificates(@Valid @NotNull @Positive Long personalId) {
        ServiceUtils.logMethodEntry("findFeaturedCertificates", personalId);
        ServiceUtils.validatePersonalId(personalId);

        List<Certificate> certificates = null;
        List<CertificateDto> result = null;

        ServiceUtils.logMethodExit("findFeaturedCertificates", result.size());
        return result;
    }

    // ===== EXPIRY MANAGEMENT =====

    public List<CertificateDto> findExpiringCertificates(@Valid @NotNull @Positive Long personalId,
                                                         @Valid @NotNull Integer daysAhead) {
        ServiceUtils.logMethodEntry("findExpiringCertificates", personalId, daysAhead);
        ServiceUtils.validatePersonalId(personalId);

        if (daysAhead <= 0) {
            throw new IllegalArgumentException("Days ahead must be positive");
        }

        LocalDate expiryDate = LocalDate.now().plusDays(daysAhead);
        List<Certificate> certificates = repository.findExpiringByPersonalId(personalId, expiryDate);
        List<CertificateDto> result = certificates.stream()
                .map(this::toCertificateDto)
                .toList();

        ServiceUtils.logMethodExit("findExpiringCertificates", result.size());
        return result;
    }


    // ===== PROVIDER QUERIES =====


    @Cacheable(value = "certificatesByProvider", key = "#personalId")
    public Map<String, Long> getCertificatesByProvider(@Valid @NotNull @Positive Long personalId) {
        ServiceUtils.logMethodEntry("getCertificatesByProvider", personalId);
        ServiceUtils.validatePersonalId(personalId);

        List<Object[]> results = repository.countCertificatesByProvider(personalId);
        Map<String, Long> stats = results.stream().collect(Collectors.toMap(
                row -> (String) row[0],
                row -> ((Number) row[1]).longValue()
        ));

        ServiceUtils.logMethodExit("getCertificatesByProvider", stats.size());
        return stats;
    }

    // ===== RELEVANCE SCORE QUERIES =====

    public List<CertificateDto> findByMinRelevanceScore(@Valid @NotNull @Positive Long personalId,
                                                        @Valid @NotNull Integer minScore) {
        ServiceUtils.logMethodEntry("findByMinRelevanceScore", personalId, minScore);
        ServiceUtils.validatePersonalId(personalId);

        if (minScore < 0 || minScore > 100) {
            throw new IllegalArgumentException("Relevance score must be between 0 and 100");
        }

        List<Certificate> certificates = repository.findByPersonalIdAndMinRelevanceScore(personalId, minScore);
        List<CertificateDto> result = certificates.stream()
                .map(this::toCertificateDto)
                .toList();

        ServiceUtils.logMethodExit("findByMinRelevanceScore", result.size());
        return result;
    }

    public List<CertificateDto> findHighRelevanceCertificates(@Valid @NotNull @Positive Long personalId) {
        ServiceUtils.logMethodEntry("findHighRelevanceCertificates", personalId);
        ServiceUtils.validatePersonalId(personalId);

        // High relevance = 80+ score
        return findByMinRelevanceScore(personalId, 80);
    }


    // ===== STATISTICS =====

    @Cacheable(value = "certificateStats", key = "#personalId")
    public CertificateStatisticsDto getCertificateStatistics(@Valid @NotNull @Positive Long personalId) {
        ServiceUtils.logMethodEntry("getCertificateStatistics", personalId);
        ServiceUtils.validatePersonalId(personalId);

        Long totalCertificates = repository.countByPersonalId(personalId);
        Long verifiedCount = repository.countVerifiedByPersonalId(personalId);
        Double avgRelevanceScore = repository.findAverageRelevanceScoreByPersonalId(personalId);

        Map<String, Long> providerStats = getCertificatesByProvider(personalId);

        // Calculate expiring certificates (next 90 days)
        Long expiringCount = (long) findExpiringCertificates(personalId, 90).size();

        // Get featured count
        Long featuredCount = (long) findFeaturedCertificates(personalId).size();

        CertificateStatisticsDto result = CertificateStatisticsDto.builder()
                .totalCertificates(totalCertificates)
                .verifiedCount(verifiedCount)
                .averageRelevanceScore(avgRelevanceScore != null ? avgRelevanceScore : 0.0)
                .providerDistribution(providerStats)
                .expiringCount(expiringCount)
                .featuredCount(featuredCount)
                .highRelevanceCount((long) findHighRelevanceCertificates(personalId).size())
                .build();

        ServiceUtils.logMethodExit("getCertificateStatistics", result);
        return result;
    }


    // ===== DTO CONVERSION =====

    private CertificateDto toCertificateDto(Certificate certificate) {
        Optional<EntityMetadata> metadata = entityMetadataRepository
                .findByEntityTypeAndEntityId(EntityType.CERTIFICATE, certificate.getId());

        // Get skills gained from this certificate
        List<String> skillsGained = getSkillsGainedFromCertificate(certificate.getId());

        // Determine category name
        String categoryName = certificate.getCertificationCategory() != null ?
                certificate.getCertificationCategory().getName() : "General";

        // Determine colors based on verification status and relevance
        String defaultColor = getColorForCertificate(certificate);
        String defaultIcon = getIconForCategory(categoryName);

        return CertificateDto.builder()
                .id(certificate.getId().toString())
                .name(certificate.getName())
                .issuer(certificate.getProvider())
                .date(ServiceUtils.formatDateAsIso(certificate.getIssueDate()))
                .certificateId(certificate.getCredentialId())
                .description(ServiceUtils.truncateText(certificate.getDescription(), 300))
                .skillsGained(skillsGained)
                .categoryName(categoryName)
                .link(certificate.getCertificateUrl())
                .icon(ServiceUtils.getIconFromMetadata(metadata, defaultIcon))
                .primaryColor(ServiceUtils.getColorFromMetadata(metadata, defaultColor))
                .secondaryColor(ServiceUtils.getColorFromMetadata(metadata, getLightColorForCertificate(certificate)))
                .verified(certificate.getVerified())
                .featured(ServiceUtils.isFeatured(metadata))
                .build();
    }

    // ===== HELPER METHODS =====

    private List<String> getSkillsGainedFromCertificate(Long certificateId) {
        return null;
    }

    private String getColorForCertificate(Certificate certificate) {
        // Priority: Verified > High Relevance > Default
        if (certificate.getVerified() != null && certificate.getVerified()) {
            return "#10B981"; // Green for verified
        }

        if (certificate.getRelevanceScore() != null && certificate.getRelevanceScore() >= 80) {
            return "#3B82F6"; // Blue for high relevance
        }

        return "#6B7280"; // Gray default
    }

    private String getLightColorForCertificate(Certificate certificate) {
        if (certificate.getVerified() != null && certificate.getVerified()) {
            return "#D1FAE5"; // Light Green
        }

        if (certificate.getRelevanceScore() != null && certificate.getRelevanceScore() >= 80) {
            return "#DBEAFE"; // Light Blue
        }

        return "#F3F4F6"; // Light Gray
    }

    private String getIconForCategory(String categoryName) {
        if (categoryName == null) return "certificate";

        return switch (categoryName.toLowerCase()) {
            case "technology", "technical", "programming" -> "code";
            case "cloud", "aws", "azure", "gcp" -> "cloud";
            case "security", "cybersecurity" -> "shield";
            case "project management", "management" -> "briefcase";
            case "language", "languages" -> "globe";
            case "design", "ui/ux" -> "palette";
            case "data", "analytics", "database" -> "database";
            case "networking", "network" -> "network";
            case "certification", "professional" -> "award";
            default -> "certificate";
        };
    }

    // ===== METADATA SUPPORT =====

    @Override
    public List<Certificate> findFeatured() {
        return repository.findAll().stream()
                .filter(certificate -> {
                    Optional<EntityMetadata> metadata = entityMetadataRepository
                            .findByEntityTypeAndEntityId(EntityType.CERTIFICATE, certificate.getId());
                    return ServiceUtils.isFeatured(metadata);
                })
                .toList();
    }



    /**
     * Return all available certification categories
     */
    @Cacheable(value = "certificationCategories")
    public List<CertificationCategoryDto> getCertificationCategories() {
        ServiceUtils.logMethodEntry("getCertificationCategories");

        List<CertificationCategory> categories = this.repository.findAllWithIcon();

        List<CertificationCategoryDto> result = categories.stream()
                .map(this::toCertificationCategoryDto)
                .toList();

        log.debug("Found {} certification categories", result.size());
        ServiceUtils.logMethodExit("getCertificationCategories", result.size());
        return result;
    }

    /**
     * Helper method for DTO conversion
     */
    private CertificationCategoryDto toCertificationCategoryDto(CertificationCategory category) {
        return CertificationCategoryDto.builder()
                .id(category.getId().toString())
                .name(category.getName())
                .icon(category.getIcon() != null ?
                        category.getIcon().getName() :
                        getDefaultCertificationIcon(category.getName()))
                .activeClass(generateActiveClass(category.getName()))
                .hoverClass(generateHoverClass(category.getName()))
                .build();
    }

    /**
     * Generate all CSS classes for categories
     */
    private String generateActiveClass(String categoryName) {
        if (categoryName == null) return "bg-blue-500 text-white";

        return switch (categoryName.toLowerCase()) {
            case "aws", "cloud" -> "bg-orange-500 text-white";
            case "microsoft", "azure" -> "bg-blue-600 text-white";
            case "google", "gcp" -> "bg-green-500 text-white";
            case "oracle", "database" -> "bg-red-500 text-white";
            case "cisco", "networking" -> "bg-blue-700 text-white";
            case "security", "cybersecurity" -> "bg-gray-800 text-white";
            case "programming", "development" -> "bg-purple-500 text-white";
            case "project management" -> "bg-indigo-500 text-white";
            default -> "bg-blue-500 text-white";
        };
    }

    /**
     * Generate all CSS classes for hover state
     */
    private String generateHoverClass(String categoryName) {
        if (categoryName == null) return "hover:bg-blue-600";

        return switch (categoryName.toLowerCase()) {
            case "aws", "cloud" -> "hover:bg-orange-600";
            case "microsoft", "azure" -> "hover:bg-blue-700";
            case "google", "gcp" -> "hover:bg-green-600";
            case "oracle", "database" -> "hover:bg-red-600";
            case "cisco", "networking" -> "hover:bg-blue-800";
            case "security", "cybersecurity" -> "hover:bg-gray-900";
            case "programming", "development" -> "hover:bg-purple-600";
            case "project management" -> "hover:bg-indigo-600";
            default -> "hover:bg-blue-600";
        };
    }

    /**
     * Icon default based on name of the certification category
     */
    private String getDefaultCertificationIcon(String categoryName) {
        if (categoryName == null) return "certificate";

        return switch (categoryName.toLowerCase()) {
            case "aws", "cloud" -> "cloud";
            case "microsoft", "azure" -> "windows";
            case "google", "gcp" -> "chrome";
            case "oracle", "database" -> "database";
            case "cisco", "networking" -> "network-wired";
            case "security", "cybersecurity" -> "shield";
            case "programming", "development" -> "code";
            case "project management" -> "clipboard-list";
            default -> "certificate";
        };
    }
}


