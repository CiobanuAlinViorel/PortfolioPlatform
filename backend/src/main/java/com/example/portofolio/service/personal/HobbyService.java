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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Hobby Service with ServiceUtils
 */
@Service
@Slf4j
public class HobbyService extends BaseService<Hobby, Long, HobbyRepository> {

    private final EntityMetadataRepository entityMetadataRepository;
    private final AchievementRepository achievementRepository;

    @Autowired
    public HobbyService(HobbyRepository hobbyRepository,
                        EntityMetadataRepository entityMetadataRepository,
                        AchievementRepository achievementRepository) {
        super(hobbyRepository);
        this.entityMetadataRepository = entityMetadataRepository;
        this.achievementRepository = achievementRepository;
    }



    @Override
    protected HobbyDto toDto(Hobby hobby) {
        return toHobbyDto(hobby);
    }

    // ===== CORE HOBBY QUERIES =====

    @Cacheable(value = "hobbiesByPersonal", key = "#personalId")
    public List<HobbyDto> findByPersonalId(@Valid @NotNull @Positive Long personalId) {
        ServiceUtils.logMethodEntry("findByPersonalId", personalId);
        ServiceUtils.validatePersonalId(personalId);

        List<Hobby> hobbies = repository.findByPersonalId(personalId);
        List<HobbyDto> result = hobbies.stream()
                .map(this::toHobbyDto)
                .toList();

        ServiceUtils.logMethodExit("findByPersonalId", result.size());
        return result;
    }


    // ===== ACHIEVEMENTS INTEGRATION =====

    public List<AchievementDto> findHobbyAchievements(@Valid @NotNull @Positive Long personalId,
                                                      @Valid @NotNull @Positive Long hobbyId) {
        ServiceUtils.logMethodEntry("findHobbyAchievements", personalId, hobbyId);
        ServiceUtils.validatePersonalId(personalId);
        ServiceUtils.validateEntityId(hobbyId);

        List<Achievement> achievements = new ArrayList<>();
        List<AchievementDto> result = achievements.stream()
                .map(this::toAchievementDto)
                .toList();

        ServiceUtils.logMethodExit("findHobbyAchievements", result.size());
        return result;
    }

    // ===== DTO CONVERSION =====

    private HobbyDto toHobbyDto(Hobby hobby) {
        Optional<EntityMetadata> metadata = entityMetadataRepository
                .findByEntityTypeAndEntityId(EntityType.HOBBY, hobby.getId());

        // Get achievements for this hobby
        List<AchievementDto> achievements = findHobbyAchievements(
                hobby.getPersonal().getId(), hobby.getId());

        // Get related skills (simplified - could be enhanced)
        List<String> relatedSkills = getRelatedSkills();

        // Determine colors based on category and activity level
        String defaultColor = getColorForHobbyCategory(hobby.getCategory());
        String defaultIcon = getIconForHobbyCategory(hobby.getCategory());

        return HobbyDto.builder()
                .id(hobby.getId().toString())
                .name(hobby.getName())
                .description(ServiceUtils.truncateText(hobby.getDescription(), 300))
                .icon(ServiceUtils.getIconFromMetadata(metadata, defaultIcon))
                .category(ServiceUtils.enumToLowerString(hobby.getCategory()))
                .yearsActive(hobby.getYearsActive() != null ? hobby.getYearsActive().doubleValue() : null)
                .complexityLevel(ServiceUtils.enumToLowerString(hobby.getComplexityLevel()))
                .relatedSkills(relatedSkills)
                .impactOnWork(ServiceUtils.enumToLowerString(hobby.getImpactOnWork()))
                .favoriteAspect(hobby.getFavoriteAspect())
                .achievements(achievements)
                .primaryColor(ServiceUtils.getColorFromMetadata(metadata, defaultColor))
                .secondaryColor(ServiceUtils.getColorFromMetadata(metadata, getLightColorForHobbyCategory(hobby.getCategory())))
                .build();
    }

    private AchievementDto toAchievementDto(Achievement achievement) {
        Optional<EntityMetadata> metadata = entityMetadataRepository
                .findByEntityTypeAndEntityId(EntityType.ACHIEVEMENT, achievement.getId());

        String formattedDate = ServiceUtils.formatDateAsIso(achievement.getAchievementDate());

        return AchievementDto.builder()
                .id(achievement.getId().toString())
                .title(achievement.getTitle())
                .description(ServiceUtils.truncateText(achievement.getDescription(), 200))
                .date(formattedDate)
                .type(ServiceUtils.enumToLowerString(achievement.getAchievementType()))
                .icon(ServiceUtils.getIconFromMetadata(metadata, "award"))
                .primaryColor(ServiceUtils.getColorFromMetadata(metadata, "#F59E0B"))
                .secondaryColor(ServiceUtils.getColorFromMetadata(metadata, "#FEF3C7"))
                .recognitionLevel(ServiceUtils.enumToLowerString(achievement.getRecognitionLevel()))
                .build();
    }

    // ===== HELPER METHODS =====

    private List<String> getRelatedSkills() {
        // Simplified implementation - could be enhanced with actual skill relationships
        // For now, return empty list or derive from hobby category
        return List.of();
    }

    private String getColorForHobbyCategory(HobbyCategory category) {
        if (category == null) return "#6B7280"; // Gray

        return switch (category) {
            case CREATIVE -> "#EC4899"; // Pink
            case SPORTS -> "#10B981"; // Green
            case TECHNOLOGY -> "#3B82F6"; // Blue
            case LEARNING -> "#8B5CF6"; // Purple
            case MUSIC -> "#F59E0B"; // Amber
            case TRAVEL -> "#06B6D4"; // Cyan
            case COOKING -> "#EF4444"; // Red
            case GARDENING -> "#84CC16"; // Lime
            case READING -> "#6366F1"; // Indigo
            case GAMING -> "#F97316"; // Orange
            case SOCIAL -> "#3619f7"; // Blue
        };
    }

    private String getLightColorForHobbyCategory(HobbyCategory category) {
        if (category == null) return "#F3F4F6"; // Light Gray

        return switch (category) {
            case CREATIVE -> "#FCE7F3"; // Light Pink
            case SPORTS -> "#D1FAE5"; // Light Green
            case TECHNOLOGY -> "#DBEAFE"; // Light Blue
            case LEARNING -> "#F3E8FF"; // Light Purple
            case MUSIC -> "#FEF3C7"; // Light Amber
            case TRAVEL -> "#CFFAFE"; // Light Cyan
            case COOKING -> "#FEE2E2"; // Light Red
            case GARDENING -> "#ECFCCB"; // Light Lime
            case READING -> "#E0E7FF"; // Light Indigo
            case GAMING -> "#FED7AA"; // Light Orange
            case SOCIAL -> "#CFFAFE";
        };
    }

    private String getIconForHobbyCategory(HobbyCategory category) {
        if (category == null) return "heart";

        return switch (category) {
            case CREATIVE -> "palette";
            case SPORTS -> "activity";
            case TECHNOLOGY -> "code";
            case LEARNING -> "book-open";
            case MUSIC -> "music";
            case TRAVEL -> "map-pin";
            case COOKING -> "chef-hat";
            case GARDENING -> "flower";
            case READING -> "book";
            case GAMING -> "gamepad-2";
            case SOCIAL -> "people";
        };
    }

    // ===== METADATA SUPPORT =====

    @Override
    public List<Hobby> findFeatured() {
        return repository.findAll().stream()
                .filter(hobby -> {
                    Optional<EntityMetadata> metadata = entityMetadataRepository
                            .findByEntityTypeAndEntityId(EntityType.HOBBY, hobby.getId());
                    return ServiceUtils.isFeatured(metadata);
                })
                .toList();
    }

}
