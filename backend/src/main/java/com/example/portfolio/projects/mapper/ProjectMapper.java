package com.example.portfolio.projects.mapper;

import com.example.portfolio.projects.domain.*;
import com.example.portfolio.projects.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(source = "projectCategory.name", target = "categoryName")
    @Mapping(target = "imageUrl", expression = "java(primaryImageUrl(project))")
    ProjectListItemDto toProjectListItemDto(Project project);

    default String primaryImageUrl(Project project) {
        return project.getMedia().stream()
                .filter(m -> Boolean.TRUE.equals(m.getPrimary()))
                .findFirst()
                .map(ProjectMedia::getImageUrl)
                .orElse(null);
    }

    @Mapping(source = "projectCategory.name", target = "categoryName")
    @Mapping(target = "features",     ignore = true)
    @Mapping(target = "challenges",   ignore = true)
    @Mapping(target = "media",        ignore = true)
    @Mapping(target = "skills",       ignore = true)
    @Mapping(target = "metrics",      ignore = true)
    @Mapping(target = "achievements", ignore = true)
    ProjectDetailDto toProjectDetailDto(Project project);

    ProjectFeatureDto toProjectFeatureDto(ProjectFeature feature);

    ProjectChallengeDto toProjectChallengeDto(ProjectChallenge challenge);

    ProjectMediaDto toProjectMediaDto(ProjectMedia media);

    @Mapping(source = "skill.id",         target = "id")
    @Mapping(source = "skill.name",       target = "name")
    @Mapping(source = "skill.proficiency", target = "proficiency")
    SkillInProjectDto toSkillInProjectDto(ProjectSkill projectSkill);

    ProjectMetricsDto toProjectMetricsDto(ProjectMetrics metrics);

    @Mapping(source = "achievement.id",              target = "achievementId")
    @Mapping(source = "achievement.title",           target = "title")
    @Mapping(source = "achievement.description",     target = "description")
    @Mapping(source = "achievement.achievementType", target = "achievementType")
    @Mapping(source = "achievement.achievementDate", target = "achievementDate")
    @Mapping(source = "achievement.recognitionLevel", target = "recognitionLevel")
    @Mapping(source = "achievement.recognizedBy",    target = "recognizedBy")
    @Mapping(source = "achievement.proofUrl",        target = "proofUrl")
    @Mapping(source = "achievement.isFeatured",      target = "isFeatured")
    ProjectAchievementDto toProjectAchievementDto(ProjectAchievement projectAchievement);

    ProjectCategoryDto toProjectCategoryDto(ProjectCategory category);
}
