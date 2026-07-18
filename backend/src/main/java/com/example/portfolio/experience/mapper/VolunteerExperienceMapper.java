package com.example.portfolio.experience.mapper;

import com.example.portfolio.experience.domain.VolunteerExperience;
import com.example.portfolio.experience.domain.VolunteerResponsibility;
import com.example.portfolio.experience.dto.*;
import com.example.portfolio.projects.domain.Project;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface VolunteerExperienceMapper {

    @Mapping(expression = "java(volunteerExperience.getVolunteerProjects().size())", target = "projectsCount")
    VolunteerExperienceListItemDto toListItemDto(VolunteerExperience volunteerExperience);

    @Mapping(target = "responsibilities", ignore = true)
    @Mapping(target = "projects", ignore = true)
    VolunteerExperienceDetailDto toDetailDto(VolunteerExperience volunteerExperience);

    VolunteerResponsibilityDto toResponsibilityDto(VolunteerResponsibility responsibility);

    ProjectInVolunteerDto toProjectInVolunteerDto(Project project);

    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "responsibilities", ignore = true)
    @Mapping(target = "volunteerProjects", ignore = true)
    VolunteerExperience toVolunteerExperience(CreateVolunteerExperienceRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "responsibilities", ignore = true)
    @Mapping(target = "volunteerProjects", ignore = true)
    void updateVolunteerExperience(UpdateVolunteerExperienceRequest request, @MappingTarget VolunteerExperience volunteerExperience);
}
