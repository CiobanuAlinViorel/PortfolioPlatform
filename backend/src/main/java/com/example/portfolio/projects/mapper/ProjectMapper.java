package com.example.portfolio.projects.mapper;

import com.example.portfolio.projects.domain.*;
import com.example.portfolio.projects.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(source = "projectCategory.name", target = "categoryName")
    ProjectListItemDto toProjectListItemDto(Project project);

    @Mapping(source = "projectCategory.name", target = "categoryName")
    @Mapping(target = "features", ignore = true)
    @Mapping(target = "challenges", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "metrics", ignore = true)
    ProjectDetailDto toProjectDetailDto(Project project);

    ProjectFeatureDto toProjectFeatureDto(ProjectFeature feature);

    ProjectChallengeDto toProjectChallengeDto(ProjectChallenge challenge);

    ProjectImageDto toProjectImageDto(ProjectImage image);

    @Mapping(source = "skill.id", target = "id")
    @Mapping(source = "skill.name", target = "name")
    @Mapping(source = "skill.proficiency", target = "proficiency")
    SkillInProjectDto toSkillInProjectDto(ProjectSkill projectSkill);

    ProjectMetricsDto toProjectMetricsDto(ProjectMetrics metrics);
}
