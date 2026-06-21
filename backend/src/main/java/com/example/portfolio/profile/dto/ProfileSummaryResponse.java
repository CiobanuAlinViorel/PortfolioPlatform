package com.example.portfolio.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileSummaryResponse {
    private ProfileInfoDto profile;
    private ContactInfoDto contact;
    private List<SkillSummaryDto> topSkills;
    private List<ProjectSummaryDto> topProjects;
    private LastExperienceDto lastExperience;
}
