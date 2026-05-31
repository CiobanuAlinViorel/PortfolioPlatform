package com.example.portofolio.service.portofolio;

import com.example.portofolio.dto.*;
import com.example.portofolio.entity.*;
import com.example.portofolio.entity.enums.EntityType;
import com.example.portofolio.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TimelineService {

    private final AchievementRepository achievementRepository;
    private final EntityMetadataRepository entityMetadataRepository;



    // ===== TIMELINE ITEMS =====

    public List<TimelineItemDto> getTimelineItems(Long personalId) {
        log.debug("Getting timeline items for personal: {}", personalId);

        return achievementRepository.findByPersonalId(personalId)
                .stream()
                .map(this::mapAchievementToTimelineItem)
                .sorted((a, b) -> b.getYear().compareTo(a.getYear()))
                .collect(Collectors.toList());
    }

    // ===== TIMELINE BY CATEGORY =====

    public List<TimelineMilestoneDto> getAchievementTimeline(Long personalId) {
        log.debug("Getting achievement timeline for personal: {}", personalId);

        return achievementRepository.findByPersonalId(personalId)
                .stream()
                .map(this::mapAchievementToMilestone)
                .sorted((a, b) -> b.getYear().compareTo(a.getYear()))
                .collect(Collectors.toList());
    }

    // ===== MAPPING METHODS =====

    private TimelineMilestoneDto mapAchievementToMilestone(Achievement achievement) {
        EntityMetadata metadata = getEntityMetadata(achievement.getId());

        return TimelineMilestoneDto.builder()
                .id(achievement.getId().toString())
                .year(String.valueOf(achievement.getAchievementDate().getYear()))
                .title(achievement.getTitle())
                .category("achievement")
                .description(achievement.getDescription())
                .icon(metadata != null && metadata.getIcon() != null ?
                        metadata.getIcon().getName() : "Award")
                .isActive(false)
                .primaryColor(metadata != null ? metadata.getPrimaryColor() : "#3B82F6")
                .secondaryColor(metadata != null ? metadata.getSecondaryColor() : "#1E40AF")
                .importance(metadata != null ? metadata.getImportance().toString().toLowerCase() : "medium")
                .build();
    }

    private TimelineItemDto mapAchievementToTimelineItem(Achievement achievement) {
        EntityMetadata metadata = getEntityMetadata(achievement.getId());

        return TimelineItemDto.builder()
                .id(achievement.getId().toString())
                .year(String.valueOf(achievement.getAchievementDate().getYear()))
                .title(achievement.getTitle())
                .subtitle(achievement.getAchievementType().toString())
                .description(achievement.getDescription())
                .type("achievement")
                .current(false)
                .link(achievement.getProofUrl())
                .icon(metadata != null && metadata.getIcon() != null ?
                        metadata.getIcon().getName() : "Award")
                .primaryColor(metadata != null ? metadata.getPrimaryColor() : "#3B82F6")
                .secondaryColor(metadata != null ? metadata.getSecondaryColor() : "#1E40AF")
                .build();
    }

    // ===== UTILITY METHODS =====

    private EntityMetadata getEntityMetadata(Long entityId) {
        return entityMetadataRepository.findByEntityTypeAndEntityId(EntityType.ACHIEVEMENT, entityId)
                .orElse(null);
    }

}