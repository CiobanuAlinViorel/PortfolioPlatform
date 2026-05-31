package com.example.portofolio.service.personal;

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

import java.util.List;
import java.util.Optional;

/**
 * Interest Service with ServiceUtils
 */
@Service
@Slf4j
public class InterestService extends BaseService<Interest, Long, InterestRepository> {

    private final EntityMetadataRepository entityMetadataRepository;

    @Autowired
    public InterestService(InterestRepository interestRepository,
                           EntityMetadataRepository entityMetadataRepository) {
        super(interestRepository);
        this.entityMetadataRepository = entityMetadataRepository;
    }
    

    @Override
    protected InterestDto toDto(Interest interest) {
        return toInterestDto(interest);
    }

    // ===== CORE INTEREST QUERIES  =====

    @Cacheable(value = "interestsByPersonal", key = "#personalId")
    public List<InterestDto> findByPersonalId(@Valid @NotNull @Positive Long personalId) {
        ServiceUtils.logMethodEntry("findByPersonalId", personalId);
        ServiceUtils.validatePersonalId(personalId);

        List<Interest> interests = repository.findByPersonalId(personalId);
        List<InterestDto> result = interests.stream()
                .map(this::toInterestDto)
                .toList();

        ServiceUtils.logMethodExit("findByPersonalId", result.size());
        return result;
    }

    // ===== DTO CONVERSION =====

    private InterestDto toInterestDto(Interest interest) {
        Optional<EntityMetadata> metadata = entityMetadataRepository
                .findByEntityTypeAndEntityId(EntityType.INTEREST, interest.getId());

        // Basic conversion without discoveries
        String defaultColor = getColorForInterestCategory(interest.getCategory());
        String defaultIcon = getIconForInterestCategory(interest.getCategory());

        return InterestDto.builder()
                .id(interest.getId().toString())
                .name(interest.getDescription()) // Based on DTO structure
                .description(ServiceUtils.truncateText(interest.getDescription(), 300))
                .icon(ServiceUtils.getIconFromMetadata(metadata, defaultIcon))
                .category(ServiceUtils.enumToLowerString(interest.getCategory()))
                .whyInterested(interest.getWhyInterested())
                .recentDiscoveries(List.of()) // Empty for basic conversion
                .primaryColor(ServiceUtils.getColorFromMetadata(metadata, defaultColor))
                .secondaryColor(ServiceUtils.getColorFromMetadata(metadata, getLightColorForInterestCategory(interest.getCategory())))
                .build();
    }

    // ===== HELPER METHODS =====

    private String getColorForInterestCategory(InterestCategory category) {
        if (category == null) return "#6B7280"; // Gray

        return switch (category) {
            case TECHNOLOGY -> "#3B82F6"; // Blue
            case SCIENCE -> "#10B981"; // Green
            case ARTS -> "#EC4899"; // Pink
            case BUSINESS -> "#F59E0B"; // Amber
            case HEALTH -> "#EF4444"; // Red
            case EDUCATION -> "#8B5CF6"; // Purple
            case ENVIRONMENT -> "#84CC16"; // Lime
            case POLITICS -> "#6366F1"; // Indigo
            case TRAVEL -> "#06B6D4"; // Cyan
            case FOOD -> "#F97316"; // Orange
            case SPORTS -> "#059669"; // Emerald
            case ENTERTAINMENT -> "#D946EF"; // Fuchsia
            case LEARNING -> "#c4c400";
            case CULTURE -> "#2a2980";
        };
    }

    private String getLightColorForInterestCategory(InterestCategory category) {
        if (category == null) return "#F3F4F6"; // Light Gray

        return switch (category) {
            case TECHNOLOGY -> "#DBEAFE"; // Light Blue
            case SCIENCE -> "#D1FAE5"; // Light Green
            case ARTS -> "#FCE7F3"; // Light Pink
            case BUSINESS -> "#FEF3C7"; // Light Amber
            case HEALTH -> "#FEE2E2"; // Light Red
            case EDUCATION -> "#F3E8FF"; // Light Purple
            case ENVIRONMENT -> "#ECFCCB"; // Light Lime
            case POLITICS -> "#E0E7FF"; // Light Indigo
            case TRAVEL -> "#CFFAFE"; // Light Cyan
            case FOOD -> "#FED7AA"; // Light Orange
            case SPORTS -> "#D1FAE5"; // Light Emerald
            case ENTERTAINMENT -> "#FAE8FF"; // Light Fuchsia
            case LEARNING -> "#f6ff47";
            case CULTURE -> "#6bebff";
        };
    }

    private String getIconForInterestCategory(InterestCategory category) {
        if (category == null) return "heart";

        return switch (category) {
            case TECHNOLOGY -> "code";
            case SCIENCE -> "beaker";
            case ARTS -> "palette";
            case BUSINESS -> "briefcase";
            case HEALTH -> "heart-pulse";
            case EDUCATION -> "graduation-cap";
            case ENVIRONMENT -> "leaf";
            case POLITICS -> "users";
            case TRAVEL -> "map-pin";
            case FOOD -> "utensils";
            case SPORTS -> "activity";
            case ENTERTAINMENT -> "play";
            case LEARNING -> "book";
            case CULTURE -> "mask";
        };
    }

    // ===== METADATA SUPPORT =====

    @Override
    public List<Interest> findFeatured() {
        return repository.findAll().stream()
                .filter(interest -> {
                    Optional<EntityMetadata> metadata = entityMetadataRepository
                            .findByEntityTypeAndEntityId(EntityType.INTEREST, interest.getId());
                    return ServiceUtils.isFeatured(metadata);
                })
                .toList();
    }

}

