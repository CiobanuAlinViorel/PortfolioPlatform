package com.example.portfolio.profile.mapper;

import com.example.portfolio.experience.domain.JobExperience;
import com.example.portfolio.profile.domain.ContactInfo;
import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.profile.dto.*;
import com.example.portfolio.projects.domain.Project;
import com.example.portfolio.skills.domain.Skill;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    // ── Read direction ────────────────────────────────────────────────────────

    ProfileInfoDto toProfileInfoDto(Profile profile);

    ContactInfoDto toContactInfoDto(ContactInfo contactInfo);

    @Mapping(source = "category.name", target = "categoryName")
    SkillSummaryDto toSkillSummaryDto(Skill skill);

    ProjectSummaryDto toProjectSummaryDto(Project project);

    @Mapping(expression = "java(jobExperience.getProjects().size())", target = "projectsCount")
    LastExperienceDto toLastExperienceDto(JobExperience jobExperience);

    // ── Write direction ───────────────────────────────────────────────────────

    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "projects", ignore = true)
    @Mapping(target = "certificates", ignore = true)
    @Mapping(target = "contactInfo", ignore = true)
    Profile toProfile(CreateProfileRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "projects", ignore = true)
    @Mapping(target = "certificates", ignore = true)
    @Mapping(target = "contactInfo", ignore = true)
    void updateProfile(UpdateProfileRequest request, @MappingTarget Profile profile);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "profile", ignore = true)
    void updateContactInfo(UpsertContactInfoRequest request, @MappingTarget ContactInfo contactInfo);
}
