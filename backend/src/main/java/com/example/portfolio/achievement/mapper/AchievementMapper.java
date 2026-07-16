package com.example.portfolio.achievement.mapper;

import com.example.portfolio.achievement.domain.Achievement;
import com.example.portfolio.achievement.dto.AchievementDto;
import com.example.portfolio.achievement.dto.UpdateAchievementRequest;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AchievementMapper {

    AchievementDto toAchievementDto(Achievement achievement);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "profile", ignore = true)
    void updateAchievement(UpdateAchievementRequest request, @MappingTarget Achievement achievement);
}
