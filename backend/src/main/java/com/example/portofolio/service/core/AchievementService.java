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

import java.util.List;
import java.util.Optional;

/**
 * Achievement Service with ServiceUtils
 */
@Service
@Slf4j
public class AchievementService extends BaseService<Achievement, Long, AchievementRepository> {

    private final EntityMetadataRepository entityMetadataRepository;

    @Autowired
    public AchievementService(AchievementRepository achievementRepository,
                              EntityMetadataRepository entityMetadataRepository) {
        super(achievementRepository);
        this.entityMetadataRepository = entityMetadataRepository;
    }



    @Override
    protected AchievementDto toDto(Achievement achievement) {
        return toAchievementDto(achievement);
    }

    // ===== CORE ACHIEVEMENT QUERIES =====

    @Cacheable(value = "achievementsByPersonal", key = "#personalId")
    public List<AchievementDto> findByPersonalId(@Valid @NotNull @Positive Long personalId) {
        ServiceUtils.logMethodEntry("findByPersonalId", personalId);
        ServiceUtils.validatePersonalId(personalId);

        List<Achievement> achievements = repository.findByPersonalIdOrderByAchievementDateDesc(personalId);
        List<AchievementDto> result = achievements.stream()
                .map(this::toAchievementDto)
                .toList();

        ServiceUtils.logMethodExit("findByPersonalId", result.size());
        return result;
    }


    // ===== ENTITY-RELATED ACHIEVEMENTS =====

    public List<AchievementDto> findByEntityTypeAndEntityId(@Valid @NotNull @Positive Long personalId,
                                                            @Valid @NotNull EntityType entityType,
                                                            @Valid @NotNull @Positive Long entityId) {
        ServiceUtils.logMethodEntry("findByEntityTypeAndEntityId", personalId, entityType, entityId);
        ServiceUtils.validatePersonalId(personalId);
        ServiceUtils.validateEntityId(entityId);

        List<Achievement> achievements = null;
        List<AchievementDto> result = null;
        ServiceUtils.logMethodExit("findByEntityTypeAndEntityId", result.size());
        return result;
    }

    // ===== DTO CONVERSION =====

    private AchievementDto toAchievementDto(Achievement achievement) {
        Optional<EntityMetadata> metadata = entityMetadataRepository
                .findByEntityTypeAndEntityId(EntityType.ACHIEVEMENT, achievement.getId());

        String formattedDate = ServiceUtils.formatDateAsIso(achievement.getAchievementDate());

        // Determine color based on recognition level
        String defaultColor = getColorForRecognitionLevel(achievement.getRecognitionLevel());
        String defaultIcon = getIconForAchievementType(achievement.getAchievementType());

        return AchievementDto.builder()
                .id(achievement.getId().toString())
                .title(achievement.getTitle())
                .description(ServiceUtils.truncateText(achievement.getDescription(), 300))
                .date(formattedDate)
                .type(ServiceUtils.enumToLowerString(achievement.getAchievementType()))
                .icon(ServiceUtils.getIconFromMetadata(metadata, defaultIcon))
                .primaryColor(ServiceUtils.getColorFromMetadata(metadata, defaultColor))
                .secondaryColor(ServiceUtils.getColorFromMetadata(metadata, getLightColorForRecognitionLevel(achievement.getRecognitionLevel())))
                .recognitionLevel(ServiceUtils.enumToLowerString(achievement.getRecognitionLevel()))
                .build();
    }

    // ===== HELPER METHODS =====

    private String getColorForRecognitionLevel(RecognitionLevel level) {
        if (level == null) return "#6B7280"; // Gray

        return switch (level) {
            case INTERNATIONAL -> "#7C3AED"; // Purple
            case NATIONAL -> "#DC2626"; // Red
            case REGIONAL -> "#EA580C"; // Orange
            case LOCAL -> "#059669"; // Green
            case INSTITUTIONAL -> "#0284C7"; // Blue
        };
    }

    private String getLightColorForRecognitionLevel(RecognitionLevel level) {
        if (level == null) return "#F3F4F6"; // Light Gray

        return switch (level) {
            case INTERNATIONAL -> "#EDE9FE"; // Light Purple
            case NATIONAL -> "#FEE2E2"; // Light Red
            case REGIONAL -> "#FED7AA"; // Light Orange
            case LOCAL -> "#D1FAE5"; // Light Green
            case INSTITUTIONAL -> "#DBEAFE"; // Light Blue
        };
    }

    private String getIconForAchievementType(AchievementType type) {
        if (type == null) return "award";

        return "something";
    }

    // ===== METADATA SUPPORT =====

    @Override
    public List<Achievement> findFeatured() {
        return repository.findAll().stream()
                .filter(achievement -> {
                    Optional<EntityMetadata> metadata = entityMetadataRepository
                            .findByEntityTypeAndEntityId(EntityType.ACHIEVEMENT, achievement.getId());
                    return ServiceUtils.isFeatured(metadata);
                })
                .toList();
    }


}


