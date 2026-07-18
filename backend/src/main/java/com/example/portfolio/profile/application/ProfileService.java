package com.example.portfolio.profile.application;

import com.example.portfolio.experience.persistence.JobExperienceRepository;
import com.example.portfolio.profile.domain.ContactInfo;
import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.profile.dto.*;
import com.example.portfolio.profile.mapper.ProfileMapper;
import com.example.portfolio.profile.persistence.ContactInfoRepository;
import com.example.portfolio.profile.persistence.ProfileRepository;
import com.example.portfolio.projects.domain.Project;
import com.example.portfolio.projects.persistence.ProjectRepository;
import com.example.portfolio.skills.domain.Skill;
import com.example.portfolio.skills.persistence.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ContactInfoRepository contactInfoRepository;
    private final SkillRepository skillRepository;
    private final ProjectRepository projectRepository;
    private final JobExperienceRepository jobExperienceRepository;
    private final ProfileMapper profileMapper;

    @Transactional(readOnly = true)
    public ProfileSummaryResponse getProfileSummary() {
        Profile profile = profileRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new NoSuchElementException("Profile not found"));

        List<Skill> topSkills = skillRepository.findTopByProfile(profile, PageRequest.of(0, 3));
        List<Project> topProjects = projectRepository.findTopByProfile(profile, PageRequest.of(0, 3));

        return ProfileSummaryResponse.builder()
                .profile(profileMapper.toProfileInfoDto(profile))
                .contact(profile.getContactInfo() != null
                        ? profileMapper.toContactInfoDto(profile.getContactInfo())
                        : null)
                .topSkills(topSkills.stream().map(profileMapper::toSkillSummaryDto).toList())
                .topProjects(topProjects.stream().map(profileMapper::toProjectSummaryDto).toList())
                .lastExperience(jobExperienceRepository
                        .findFirstByProfileOrderByStartDateDesc(profile)
                        .map(profileMapper::toLastExperienceDto)
                        .orElse(null))
                .build();
    }

    @Transactional
    public ProfileInfoDto createProfile(CreateProfileRequest request) {
        if (profileRepository.findFirstByOrderByIdAsc().isPresent()) {
            throw new IllegalArgumentException("A profile already exists");
        }
        Profile profile = profileMapper.toProfile(request);
        ContactInfo contactInfo = ContactInfo.builder()
                .profile(profile)
                .phone(request.getContactInfo().getPhone())
                .email(request.getContactInfo().getEmail())
                .github(request.getContactInfo().getGithub())
                .linkedin(request.getContactInfo().getLinkedin())
                .city(request.getContactInfo().getCity())
                .country(request.getContactInfo().getCountry())
                .build();
        contactInfoRepository.save(contactInfo);
        return profileMapper.toProfileInfoDto(profileRepository.save(profile));
    }

    @Transactional
    public ProfileInfoDto updateProfile(UpdateProfileRequest request) {
        Profile profile = profileRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new NoSuchElementException("Profile not found"));
        profileMapper.updateProfile(request, profile);
        ContactInfo contactInfo = ContactInfo.builder()
                .profile(profile)
                .phone(request.getContactInfo().getPhone())
                .email(request.getContactInfo().getEmail())
                .github(request.getContactInfo().getGithub())
                .linkedin(request.getContactInfo().getLinkedin())
                .city(request.getContactInfo().getCity())
                .country(request.getContactInfo().getCountry())
                .build();
        contactInfoRepository.save(contactInfo);
        return profileMapper.toProfileInfoDto(profileRepository.save(profile));
    }
}
