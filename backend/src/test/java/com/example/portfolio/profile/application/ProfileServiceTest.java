package com.example.portfolio.profile.application;

import com.example.portfolio.experience.domain.JobExperience;
import com.example.portfolio.experience.persistence.JobExperienceRepository;
import com.example.portfolio.profile.domain.ContactInfo;
import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.profile.dto.*;
import com.example.portfolio.profile.mapper.ProfileMapper;
import com.example.portfolio.profile.persistence.ContactInfoRepository;
import com.example.portfolio.profile.persistence.ProfileRepository;
import com.example.portfolio.projects.domain.Project;
import com.example.portfolio.projects.domain.ProjectStatus;
import com.example.portfolio.projects.persistence.ProjectRepository;
import com.example.portfolio.skills.domain.ProficiencyLevel;
import com.example.portfolio.skills.domain.Skill;
import com.example.portfolio.skills.persistence.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock private ProfileRepository profileRepository;
    @Mock private ContactInfoRepository contactInfoRepository;
    @Mock private SkillRepository skillRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private JobExperienceRepository jobExperienceRepository;
    @Mock private ProfileMapper profileMapper;

    @InjectMocks
    private ProfileService profileService;

    private Profile profile;
    private ContactInfo contactInfo;
    private ProfileInfoDto profileInfoDto;
    private ContactInfoDto contactInfoDto;

    @BeforeEach
    void setUp() {
        contactInfo = ContactInfo.builder()
                .email("john@test.com")
                .github("john-github")
                .linkedin("john-linkedin")
                .build();

        profile = Profile.builder()
                .firstName("John")
                .lastName("Doe")
                .age(30)
                .description("Developer")
                .contactInfo(contactInfo)
                .build();

        profileInfoDto = ProfileInfoDto.builder()
                .firstName("John")
                .lastName("Doe")
                .age(30)
                .description("Developer")
                .build();

        contactInfoDto = ContactInfoDto.builder()
                .email("john@test.com")
                .github("john-github")
                .linkedin("john-linkedin")
                .build();
    }

    // ── getProfileSummary — happy paths ───────────────────────────────────────

    @Test
    void getProfileSummary_shouldReturnFullAggregation_whenAllDataIsPresent() {
        Skill skill = Skill.builder().name("Java").proficiency(ProficiencyLevel.EXPERT).sortOrder(1).profile(profile).build();
        Project project = Project.builder().title("App").status(ProjectStatus.PRODUCTION).sortOrder(1).profile(profile).build();
        JobExperience job = JobExperience.builder().companyName("Acme").role("Dev").startDate(LocalDate.of(2022, 1, 1)).profile(profile).build();

        SkillSummaryDto skillDto = SkillSummaryDto.builder().name("Java").proficiency(ProficiencyLevel.EXPERT).build();
        ProjectSummaryDto projectDto = ProjectSummaryDto.builder().title("App").status(ProjectStatus.PRODUCTION).build();
        LastExperienceDto expDto = LastExperienceDto.builder().companyName("Acme").role("Dev").build();

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(skillRepository.findTopByProfile(eq(profile), any(Pageable.class))).thenReturn(List.of(skill));
        when(projectRepository.findTopByProfile(eq(profile), any(Pageable.class))).thenReturn(List.of(project));
        when(jobExperienceRepository.findFirstByProfileOrderByStartDateDesc(profile)).thenReturn(Optional.of(job));
        when(profileMapper.toProfileInfoDto(profile)).thenReturn(profileInfoDto);
        when(profileMapper.toContactInfoDto(contactInfo)).thenReturn(contactInfoDto);
        when(profileMapper.toSkillSummaryDto(skill)).thenReturn(skillDto);
        when(profileMapper.toProjectSummaryDto(project)).thenReturn(projectDto);
        when(profileMapper.toLastExperienceDto(job)).thenReturn(expDto);

        ProfileSummaryResponse result = profileService.getProfileSummary();

        assertThat(result.getProfile()).isEqualTo(profileInfoDto);
        assertThat(result.getContact()).isEqualTo(contactInfoDto);
        assertThat(result.getTopSkills()).containsExactly(skillDto);
        assertThat(result.getTopProjects()).containsExactly(projectDto);
        assertThat(result.getLastExperience()).isEqualTo(expDto);
    }

    @Test
    void getProfileSummary_shouldPassPageRequestWithLimitThree_toSkillAndProjectRepositories() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(skillRepository.findTopByProfile(any(), any())).thenReturn(List.of());
        when(projectRepository.findTopByProfile(any(), any())).thenReturn(List.of());
        when(jobExperienceRepository.findFirstByProfileOrderByStartDateDesc(any())).thenReturn(Optional.empty());
        when(profileMapper.toProfileInfoDto(profile)).thenReturn(profileInfoDto);

        profileService.getProfileSummary();

        verify(skillRepository).findTopByProfile(eq(profile), eq(PageRequest.of(0, 3)));
        verify(projectRepository).findTopByProfile(eq(profile), eq(PageRequest.of(0, 3)));
    }

    // ── getProfileSummary — null / empty cases ────────────────────────────────

    @Test
    void getProfileSummary_shouldReturnNullContact_whenProfileHasNoContactInfo() {
        Profile profileWithoutContact = Profile.builder()
                .firstName("Jane")
                .lastName("Doe")
                .build();

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profileWithoutContact));
        when(skillRepository.findTopByProfile(any(), any())).thenReturn(List.of());
        when(projectRepository.findTopByProfile(any(), any())).thenReturn(List.of());
        when(jobExperienceRepository.findFirstByProfileOrderByStartDateDesc(any())).thenReturn(Optional.empty());
        when(profileMapper.toProfileInfoDto(profileWithoutContact)).thenReturn(profileInfoDto);

        ProfileSummaryResponse result = profileService.getProfileSummary();

        assertThat(result.getContact()).isNull();
        verify(profileMapper, never()).toContactInfoDto(any());
    }

    @Test
    void getProfileSummary_shouldReturnNullLastExperience_whenNoJobExperienceExists() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(skillRepository.findTopByProfile(any(), any())).thenReturn(List.of());
        when(projectRepository.findTopByProfile(any(), any())).thenReturn(List.of());
        when(jobExperienceRepository.findFirstByProfileOrderByStartDateDesc(profile)).thenReturn(Optional.empty());
        when(profileMapper.toProfileInfoDto(profile)).thenReturn(profileInfoDto);
        when(profileMapper.toContactInfoDto(contactInfo)).thenReturn(contactInfoDto);

        ProfileSummaryResponse result = profileService.getProfileSummary();

        assertThat(result.getLastExperience()).isNull();
        verify(profileMapper, never()).toLastExperienceDto(any());
    }

    @Test
    void getProfileSummary_shouldReturnEmptyTopSkills_whenNoSkillsHaveSortOrder() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(skillRepository.findTopByProfile(any(), any())).thenReturn(List.of());
        when(projectRepository.findTopByProfile(any(), any())).thenReturn(List.of());
        when(jobExperienceRepository.findFirstByProfileOrderByStartDateDesc(any())).thenReturn(Optional.empty());
        when(profileMapper.toProfileInfoDto(profile)).thenReturn(profileInfoDto);
        when(profileMapper.toContactInfoDto(contactInfo)).thenReturn(contactInfoDto);

        ProfileSummaryResponse result = profileService.getProfileSummary();

        assertThat(result.getTopSkills()).isEmpty();
        verify(profileMapper, never()).toSkillSummaryDto(any());
    }

    @Test
    void getProfileSummary_shouldReturnEmptyTopProjects_whenNoProjectsHaveSortOrder() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(skillRepository.findTopByProfile(any(), any())).thenReturn(List.of());
        when(projectRepository.findTopByProfile(any(), any())).thenReturn(List.of());
        when(jobExperienceRepository.findFirstByProfileOrderByStartDateDesc(any())).thenReturn(Optional.empty());
        when(profileMapper.toProfileInfoDto(profile)).thenReturn(profileInfoDto);
        when(profileMapper.toContactInfoDto(contactInfo)).thenReturn(contactInfoDto);

        ProfileSummaryResponse result = profileService.getProfileSummary();

        assertThat(result.getTopProjects()).isEmpty();
        verify(profileMapper, never()).toProjectSummaryDto(any());
    }

    @Test
    void getProfileSummary_shouldMapEachSkillAndProject_whenMultipleFeaturedItemsExist() {
        Skill s1 = Skill.builder().name("Java").proficiency(ProficiencyLevel.EXPERT).sortOrder(1).profile(profile).build();
        Skill s2 = Skill.builder().name("Spring").proficiency(ProficiencyLevel.ADVANCED).sortOrder(2).profile(profile).build();
        Project p1 = Project.builder().title("App1").status(ProjectStatus.PRODUCTION).sortOrder(1).profile(profile).build();
        Project p2 = Project.builder().title("App2").status(ProjectStatus.DEVELOPMENT).sortOrder(2).profile(profile).build();

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(skillRepository.findTopByProfile(any(), any())).thenReturn(List.of(s1, s2));
        when(projectRepository.findTopByProfile(any(), any())).thenReturn(List.of(p1, p2));
        when(jobExperienceRepository.findFirstByProfileOrderByStartDateDesc(any())).thenReturn(Optional.empty());
        when(profileMapper.toProfileInfoDto(profile)).thenReturn(profileInfoDto);
        when(profileMapper.toContactInfoDto(contactInfo)).thenReturn(contactInfoDto);

        // Use thenAnswer to distinguish entities by their fields since both have null id
        when(profileMapper.toSkillSummaryDto(any(Skill.class))).thenAnswer(inv -> {
            Skill s = inv.getArgument(0);
            return SkillSummaryDto.builder().name(s.getName()).proficiency(s.getProficiency()).build();
        });
        when(profileMapper.toProjectSummaryDto(any(Project.class))).thenAnswer(inv -> {
            Project p = inv.getArgument(0);
            return ProjectSummaryDto.builder().title(p.getTitle()).status(p.getStatus()).build();
        });

        ProfileSummaryResponse result = profileService.getProfileSummary();

        assertThat(result.getTopSkills()).hasSize(2);
        assertThat(result.getTopSkills()).extracting(SkillSummaryDto::getName)
                .containsExactly("Java", "Spring");
        assertThat(result.getTopProjects()).hasSize(2);
        assertThat(result.getTopProjects()).extracting(ProjectSummaryDto::getTitle)
                .containsExactly("App1", "App2");
    }

    // ── getProfileSummary — error cases ───────────────────────────────────────

    @Test
    void getProfileSummary_shouldThrowNoSuchElementException_whenProfileDoesNotExist() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getProfileSummary())
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Profile not found");

        verify(profileRepository).findFirstByOrderByIdAsc();
        verifyNoInteractions(skillRepository, projectRepository, jobExperienceRepository, profileMapper);
    }

    // ── createProfile ─────────────────────────────────────────────────────────

    @Test
    void createProfile_shouldCreateAndReturnProfileInfoDto_whenNoProfileExists() {
        CreateProfileRequest request = CreateProfileRequest.builder()
                .firstName("Alice").lastName("Smith").age(25).description("Developer")
                .contactInfo(ContactInfoDto.builder().email("alice@test.com").build()).build();
        Profile newProfile = Profile.builder().firstName("Alice").lastName("Smith").age(25).build();
        ProfileInfoDto expectedDto = ProfileInfoDto.builder().firstName("Alice").lastName("Smith").age(25).build();

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());
        when(profileMapper.toProfile(request)).thenReturn(newProfile);
        when(profileRepository.save(newProfile)).thenReturn(newProfile);
        when(profileMapper.toProfileInfoDto(newProfile)).thenReturn(expectedDto);

        ProfileInfoDto result = profileService.createProfile(request);

        assertThat(result).isEqualTo(expectedDto);
        verify(profileRepository).save(newProfile);
    }

    @Test
    void createProfile_shouldThrowIllegalArgumentException_whenProfileAlreadyExists() {
        CreateProfileRequest request = CreateProfileRequest.builder()
                .firstName("Alice").lastName("Smith").build();
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> profileService.createProfile(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A profile already exists");

        verify(profileRepository, never()).save(any());
        verify(profileMapper, never()).toProfile(any());
    }

    @Test
    void createProfile_shouldCallMapperToProfile_withGivenRequest() {
        CreateProfileRequest request = CreateProfileRequest.builder()
                .firstName("Alice").lastName("Smith").imageLink("http://img.png")
                .contactInfo(ContactInfoDto.builder().email("alice@test.com").build()).build();
        Profile newProfile = Profile.builder().firstName("Alice").lastName("Smith").build();

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());
        when(profileMapper.toProfile(request)).thenReturn(newProfile);
        when(profileRepository.save(any())).thenReturn(newProfile);
        when(profileMapper.toProfileInfoDto(any())).thenReturn(profileInfoDto);

        profileService.createProfile(request);

        verify(profileMapper).toProfile(request);
    }

    // ── updateProfile ─────────────────────────────────────────────────────────

    @Test
    void updateProfile_shouldUpdateAndReturnProfileInfoDto_whenProfileExists() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstName("Updated").age(35)
                .contactInfo(ContactInfoDto.builder().email("updated@test.com").build()).build();
        ProfileInfoDto expectedDto = ProfileInfoDto.builder().firstName("Updated").age(35).build();

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(profileRepository.save(profile)).thenReturn(profile);
        when(profileMapper.toProfileInfoDto(profile)).thenReturn(expectedDto);

        ProfileInfoDto result = profileService.updateProfile(request);

        assertThat(result).isEqualTo(expectedDto);
        verify(profileMapper).updateProfile(request, profile);
        verify(profileRepository).save(profile);
    }

    @Test
    void updateProfile_shouldThrowNoSuchElementException_whenNoProfileExists() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstName("Updated").build();
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.updateProfile(request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Profile not found");

        verify(profileRepository, never()).save(any());
    }

    @Test
    void updateProfile_shouldCallMapperUpdateProfile_withRequestAndExistingProfile() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .description("New description")
                .contactInfo(ContactInfoDto.builder().email("existing@test.com").build()).build();

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(profileRepository.save(profile)).thenReturn(profile);
        when(profileMapper.toProfileInfoDto(profile)).thenReturn(profileInfoDto);

        profileService.updateProfile(request);

        verify(profileMapper).updateProfile(request, profile);
    }


}
