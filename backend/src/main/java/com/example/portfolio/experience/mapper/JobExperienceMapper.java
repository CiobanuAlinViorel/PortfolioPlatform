package com.example.portfolio.experience.mapper;

import com.example.portfolio.experience.domain.JobExperience;
import com.example.portfolio.experience.dto.*;
import com.example.portfolio.projects.domain.Project;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface JobExperienceMapper {

    @Mapping(expression = "java(jobExperience.getProjects().size())", target = "projectsCount")
    JobExperienceListItemDto toListItemDto(JobExperience jobExperience);

    @Mapping(target = "projects", ignore = true)
    JobExperienceDetailDto toDetailDto(JobExperience jobExperience);

    ProjectInJobDto toProjectInJobDto(Project project);

    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "projects", ignore = true)
    JobExperience toJobExperience(CreateJobExperienceRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "projects", ignore = true)
    void updateJobExperience(UpdateJobExperienceRequest request, @MappingTarget JobExperience jobExperience);
}
