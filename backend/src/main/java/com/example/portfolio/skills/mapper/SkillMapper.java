package com.example.portfolio.skills.mapper;

import com.example.portfolio.projects.domain.Project;
import com.example.portfolio.skills.domain.LearningProgress;
import com.example.portfolio.skills.domain.Skill;
import com.example.portfolio.skills.domain.SkillCategory;
import com.example.portfolio.skills.dto.LearningProgressDto;
import com.example.portfolio.skills.dto.ProjectInSkillDto;
import com.example.portfolio.skills.dto.SkillCategoryDto;
import com.example.portfolio.skills.dto.SkillDetailDto;
import com.example.portfolio.skills.dto.SkillListItemDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SkillMapper {

    @Mapping(source = "category.name", target = "categoryName")
    SkillListItemDto toSkillListItemDto(Skill skill);

    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "projects", ignore = true)
    @Mapping(target = "learningProgress", ignore = true)
    SkillDetailDto toSkillDetailDto(Skill skill);

    ProjectInSkillDto toProjectInSkillDto(Project project);

    @Mapping(source = "parent.id", target = "parentId")
    SkillCategoryDto toSkillCategoryDto(SkillCategory category);

    LearningProgressDto toLearningProgressDto(LearningProgress learningProgress);
}
