package com.example.portfolio.experience.application;

import com.example.portfolio.experience.domain.ImpactLevel;
import com.example.portfolio.experience.domain.VolunteerExperience;
import com.example.portfolio.experience.domain.VolunteerProject;
import com.example.portfolio.experience.domain.VolunteerResponsibility;
import com.example.portfolio.experience.domain.VolunteerStatus;
import com.example.portfolio.experience.domain.VolunteerType;
import com.example.portfolio.experience.dto.*;
import com.example.portfolio.experience.domain.JobProjects;
import com.example.portfolio.experience.mapper.VolunteerExperienceMapper;
import com.example.portfolio.experience.persistence.JobProjectsRepository;
import com.example.portfolio.experience.persistence.VolunteerExperienceRepository;
import com.example.portfolio.experience.persistence.VolunteerProjectRepository;
import com.example.portfolio.experience.persistence.VolunteerResponsibilityRepository;
import com.example.portfolio.hobby.domain.ComplexityLevel;
import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.profile.persistence.ProfileRepository;
import com.example.portfolio.projects.domain.Project;
import com.example.portfolio.projects.domain.ProjectStatus;
import com.example.portfolio.projects.persistence.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VolunteerExperienceServiceTest {

    @Mock private ProfileRepository profileRepository;
    @Mock private VolunteerExperienceRepository volunteerExperienceRepository;
    @Mock private VolunteerProjectRepository volunteerProjectRepository;
    @Mock private VolunteerResponsibilityRepository volunteerResponsibilityRepository;
    @Mock private JobProjectsRepository jobProjectsRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private VolunteerExperienceMapper volunteerExperienceMapper;

    @InjectMocks
    private VolunteerExperienceService volunteerExperienceService;

    private Profile profile;
    private VolunteerExperience volunteerExperience;

    @BeforeEach
    void setUp() {
        profile = Profile.builder().firstName("John").lastName("Doe").build();

        volunteerExperience = VolunteerExperience.builder()
                .profile(profile)
                .organization("Red Cross")
                .role("Coordinator")
                .type(VolunteerType.NGO)
                .startDate(LocalDate.of(2022, 1, 1))
                .status(VolunteerStatus.ONGOING)
                .build();
        ReflectionTestUtils.setField(volunteerExperience, "id", 1L);
    }

    private Project buildProject(long id, String title) {
        Project project = Project.builder()
                .profile(profile).title(title)
                .status(ProjectStatus.PRODUCTION).complexity(ComplexityLevel.ADVANCED)
                .build();
        ReflectionTestUtils.setField(project, "id", id);
        return project;
    }

    // ── getVolunteerExperienceList ───────────────────────────────────────────

    @Test
    void getVolunteerExperienceList_shouldReturnMappedList_whenProfileAndExperiencesExist() {
        VolunteerExperienceListItemDto dto = VolunteerExperienceListItemDto.builder().organization("Red Cross").build();

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(volunteerExperienceRepository.findAllByProfileOrderByStartDateDesc(profile)).thenReturn(List.of(volunteerExperience));
        when(volunteerExperienceMapper.toListItemDto(volunteerExperience)).thenReturn(dto);

        List<VolunteerExperienceListItemDto> result = volunteerExperienceService.getVolunteerExperienceList();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrganization()).isEqualTo("Red Cross");
    }

    @Test
    void getVolunteerExperienceList_shouldThrowNoSuchElementException_whenProfileNotFound() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> volunteerExperienceService.getVolunteerExperienceList())
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Profile not found");

        verifyNoInteractions(volunteerExperienceRepository, volunteerExperienceMapper);
    }

    // ── getVolunteerExperienceById ────────────────────────────────────────────

    @Test
    void getVolunteerExperienceById_shouldReturnDetailWithEmptyCollections_whenNoneLinked() {
        VolunteerExperienceDetailDto dto = VolunteerExperienceDetailDto.builder().organization("Red Cross").build();

        when(volunteerExperienceRepository.findById(1L)).thenReturn(Optional.of(volunteerExperience));
        when(volunteerExperienceMapper.toDetailDto(volunteerExperience)).thenReturn(dto);

        VolunteerExperienceDetailDto result = volunteerExperienceService.getVolunteerExperienceById(1L);

        assertThat(result.getResponsibilities()).isEmpty();
        assertThat(result.getProjects()).isEmpty();
    }

    @Test
    void getVolunteerExperienceById_shouldReturnResponsibilitiesSortedBySortOrder() {
        VolunteerResponsibility r1 = VolunteerResponsibility.builder().volunteerExperience(volunteerExperience).description("Second").sortOrder(2).build();
        VolunteerResponsibility r2 = VolunteerResponsibility.builder().volunteerExperience(volunteerExperience).description("First").sortOrder(1).build();
        ReflectionTestUtils.setField(r1, "id", 1L);
        ReflectionTestUtils.setField(r2, "id", 2L);
        volunteerExperience.addResponsibility(r1);
        volunteerExperience.addResponsibility(r2);

        VolunteerExperienceDetailDto dto = VolunteerExperienceDetailDto.builder().organization("Red Cross").build();
        when(volunteerExperienceRepository.findById(1L)).thenReturn(Optional.of(volunteerExperience));
        when(volunteerExperienceMapper.toDetailDto(volunteerExperience)).thenReturn(dto);
        when(volunteerExperienceMapper.toResponsibilityDto(r1)).thenReturn(VolunteerResponsibilityDto.builder().description("Second").sortOrder(2).build());
        when(volunteerExperienceMapper.toResponsibilityDto(r2)).thenReturn(VolunteerResponsibilityDto.builder().description("First").sortOrder(1).build());

        VolunteerExperienceDetailDto result = volunteerExperienceService.getVolunteerExperienceById(1L);

        assertThat(result.getResponsibilities()).extracting(VolunteerResponsibilityDto::getDescription)
                .containsExactly("First", "Second");
    }

    @Test
    void getVolunteerExperienceById_shouldReturnProjectsWithContributionPercentage() {
        Project project = buildProject(10L, "Water Wells");
        VolunteerProject link = VolunteerProject.builder()
                .volunteerExperience(volunteerExperience).project(project)
                .contributionPercentage(new BigDecimal("40.0")).build();
        ReflectionTestUtils.setField(link, "id", 5L);
        volunteerExperience.addProject(link);

        VolunteerExperienceDetailDto dto = VolunteerExperienceDetailDto.builder().organization("Red Cross").build();
        when(volunteerExperienceRepository.findById(1L)).thenReturn(Optional.of(volunteerExperience));
        when(volunteerExperienceMapper.toDetailDto(volunteerExperience)).thenReturn(dto);
        when(volunteerExperienceMapper.toProjectInVolunteerDto(project))
                .thenReturn(ProjectInVolunteerDto.builder().id(10L).title("Water Wells").build());

        VolunteerExperienceDetailDto result = volunteerExperienceService.getVolunteerExperienceById(1L);

        assertThat(result.getProjects()).hasSize(1);
        assertThat(result.getProjects().get(0).getId()).isEqualTo(5L);
        assertThat(result.getProjects().get(0).getContributionPercentage()).isEqualByComparingTo("40.0");
        assertThat(result.getProjects().get(0).getProject().getTitle()).isEqualTo("Water Wells");
    }

    @Test
    void getVolunteerExperienceById_shouldThrowNoSuchElementException_whenNotFound() {
        when(volunteerExperienceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> volunteerExperienceService.getVolunteerExperienceById(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Volunteer experience not found");
    }

    // ── createVolunteerExperience ─────────────────────────────────────────────

    @Test
    void createVolunteerExperience_shouldPersistResponsibilitiesAndProjects_whenProvided() {
        CreateVolunteerExperienceRequest request = new CreateVolunteerExperienceRequest();
        request.setOrganization("Red Cross");
        request.setRole("Coordinator");
        request.setType(VolunteerType.NGO);
        request.setStartDate(LocalDate.of(2022, 1, 1));
        request.setStatus(VolunteerStatus.ONGOING);

        VolunteerResponsibilityRequest rr = new VolunteerResponsibilityRequest();
        rr.setDescription("Organize donations");
        request.setResponsibilities(List.of(rr));

        VolunteerProjectRequest pr = new VolunteerProjectRequest();
        pr.setProjectId(10L);
        pr.setContributionPercentage(new BigDecimal("30.0"));
        request.setProjects(List.of(pr));

        Project project = buildProject(10L, "Water Wells");

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(volunteerExperienceMapper.toVolunteerExperience(request)).thenReturn(volunteerExperience);
        when(volunteerExperienceRepository.save(volunteerExperience)).thenReturn(volunteerExperience);
        when(volunteerResponsibilityRepository.save(any(VolunteerResponsibility.class))).thenAnswer(inv -> inv.getArgument(0));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(volunteerProjectRepository.findByProject(project)).thenReturn(Optional.empty());
        when(jobProjectsRepository.findByProject(project)).thenReturn(Optional.empty());
        when(volunteerProjectRepository.save(any(VolunteerProject.class))).thenAnswer(inv -> inv.getArgument(0));
        when(volunteerExperienceMapper.toDetailDto(volunteerExperience)).thenReturn(VolunteerExperienceDetailDto.builder().build());
        when(volunteerExperienceMapper.toProjectInVolunteerDto(project)).thenReturn(ProjectInVolunteerDto.builder().title("Water Wells").build());

        VolunteerExperienceDetailDto result = volunteerExperienceService.createVolunteerExperience(request);

        var respCaptor = ArgumentCaptor.forClass(VolunteerResponsibility.class);
        verify(volunteerResponsibilityRepository).save(respCaptor.capture());
        assertThat(respCaptor.getValue().getDescription()).isEqualTo("Organize donations");
        assertThat(respCaptor.getValue().getImpactLevel()).isEqualTo(ImpactLevel.MEDIUM);

        var projCaptor = ArgumentCaptor.forClass(VolunteerProject.class);
        verify(volunteerProjectRepository).save(projCaptor.capture());
        assertThat(projCaptor.getValue().getProject()).isEqualTo(project);
        assertThat(projCaptor.getValue().getContributionPercentage()).isEqualByComparingTo("30.0");

        assertThat(result.getResponsibilities()).hasSize(1);
        assertThat(result.getProjects()).hasSize(1);
    }

    @Test
    void createVolunteerExperience_shouldThrowIllegalArgumentException_whenProjectAlreadyLinkedElsewhere() {
        CreateVolunteerExperienceRequest request = new CreateVolunteerExperienceRequest();
        request.setOrganization("Red Cross");
        request.setRole("Coordinator");
        request.setType(VolunteerType.NGO);
        request.setStartDate(LocalDate.of(2022, 1, 1));
        request.setStatus(VolunteerStatus.ONGOING);

        VolunteerProjectRequest pr = new VolunteerProjectRequest();
        pr.setProjectId(10L);
        pr.setContributionPercentage(new BigDecimal("30.0"));
        request.setProjects(List.of(pr));

        Project project = buildProject(10L, "Water Wells");
        VolunteerProject alreadyLinked = VolunteerProject.builder().project(project).contributionPercentage(BigDecimal.TEN).build();
        ReflectionTestUtils.setField(alreadyLinked, "id", 99L);

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(volunteerExperienceMapper.toVolunteerExperience(request)).thenReturn(volunteerExperience);
        when(volunteerExperienceRepository.save(volunteerExperience)).thenReturn(volunteerExperience);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(volunteerProjectRepository.findByProject(project)).thenReturn(Optional.of(alreadyLinked));

        assertThatThrownBy(() -> volunteerExperienceService.createVolunteerExperience(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project is already linked to a volunteer experience");

        verify(volunteerProjectRepository, never()).save(any());
    }

    @Test
    void createVolunteerExperience_shouldThrowIllegalArgumentException_whenProjectAlreadyLinkedToJobExperience() {
        CreateVolunteerExperienceRequest request = new CreateVolunteerExperienceRequest();
        request.setOrganization("Red Cross");
        request.setRole("Coordinator");
        request.setType(VolunteerType.NGO);
        request.setStartDate(LocalDate.of(2022, 1, 1));
        request.setStatus(VolunteerStatus.ONGOING);

        VolunteerProjectRequest pr = new VolunteerProjectRequest();
        pr.setProjectId(10L);
        pr.setContributionPercentage(new BigDecimal("30.0"));
        request.setProjects(List.of(pr));

        Project project = buildProject(10L, "Water Wells");
        JobProjects jobLink = JobProjects.builder().project(project).build();
        ReflectionTestUtils.setField(jobLink, "id", 55L);

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(volunteerExperienceMapper.toVolunteerExperience(request)).thenReturn(volunteerExperience);
        when(volunteerExperienceRepository.save(volunteerExperience)).thenReturn(volunteerExperience);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(volunteerProjectRepository.findByProject(project)).thenReturn(Optional.empty());
        when(jobProjectsRepository.findByProject(project)).thenReturn(Optional.of(jobLink));

        assertThatThrownBy(() -> volunteerExperienceService.createVolunteerExperience(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project is already linked to a job experience");

        verify(volunteerProjectRepository, never()).save(any());
    }

    @Test
    void createVolunteerExperience_shouldThrowNoSuchElementException_whenProjectIdDoesNotExist() {
        CreateVolunteerExperienceRequest request = new CreateVolunteerExperienceRequest();
        request.setOrganization("Red Cross");
        request.setRole("Coordinator");
        request.setType(VolunteerType.NGO);
        request.setStartDate(LocalDate.of(2022, 1, 1));
        request.setStatus(VolunteerStatus.ONGOING);

        VolunteerProjectRequest pr = new VolunteerProjectRequest();
        pr.setProjectId(404L);
        pr.setContributionPercentage(BigDecimal.TEN);
        request.setProjects(List.of(pr));

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(volunteerExperienceMapper.toVolunteerExperience(request)).thenReturn(volunteerExperience);
        when(volunteerExperienceRepository.save(volunteerExperience)).thenReturn(volunteerExperience);
        when(projectRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> volunteerExperienceService.createVolunteerExperience(request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Project not found");
    }

    @Test
    void createVolunteerExperience_shouldThrowNoSuchElementException_whenProfileNotFound() {
        CreateVolunteerExperienceRequest request = new CreateVolunteerExperienceRequest();
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> volunteerExperienceService.createVolunteerExperience(request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Profile not found");

        verifyNoInteractions(volunteerExperienceRepository);
    }

    // ── updateVolunteerExperience ─────────────────────────────────────────────

    @Test
    void updateVolunteerExperience_shouldApplyScalarChanges() {
        UpdateVolunteerExperienceRequest request = new UpdateVolunteerExperienceRequest();
        request.setRole("Lead Coordinator");

        when(volunteerExperienceRepository.findById(1L)).thenReturn(Optional.of(volunteerExperience));
        when(volunteerExperienceRepository.save(volunteerExperience)).thenReturn(volunteerExperience);
        when(volunteerExperienceMapper.toDetailDto(volunteerExperience)).thenReturn(VolunteerExperienceDetailDto.builder().build());

        volunteerExperienceService.updateVolunteerExperience(1L, request);

        verify(volunteerExperienceMapper).updateVolunteerExperience(request, volunteerExperience);
        verify(volunteerExperienceRepository).save(volunteerExperience);
    }

    @Test
    void updateVolunteerExperience_shouldUpdateExistingResponsibilityAndCreateNewOne() {
        VolunteerResponsibility existing = VolunteerResponsibility.builder()
                .volunteerExperience(volunteerExperience).description("Old task").sortOrder(0).build();
        ReflectionTestUtils.setField(existing, "id", 10L);

        VolunteerResponsibilityRequest updateExisting = new VolunteerResponsibilityRequest();
        updateExisting.setId(10L);
        updateExisting.setDescription("Updated task");
        VolunteerResponsibilityRequest createNew = new VolunteerResponsibilityRequest();
        createNew.setDescription("New task");

        UpdateVolunteerExperienceRequest request = new UpdateVolunteerExperienceRequest();
        request.setResponsibilities(List.of(updateExisting, createNew));

        when(volunteerExperienceRepository.findById(1L)).thenReturn(Optional.of(volunteerExperience));
        when(volunteerExperienceRepository.save(volunteerExperience)).thenReturn(volunteerExperience);
        when(volunteerResponsibilityRepository.findAllByVolunteerExperienceOrderBySortOrderAsc(volunteerExperience))
                .thenReturn(List.of(existing));
        when(volunteerResponsibilityRepository.save(any(VolunteerResponsibility.class))).thenAnswer(inv -> inv.getArgument(0));
        when(volunteerExperienceMapper.toDetailDto(volunteerExperience)).thenReturn(VolunteerExperienceDetailDto.builder().build());

        volunteerExperienceService.updateVolunteerExperience(1L, request);

        assertThat(existing.getDescription()).isEqualTo("Updated task");
        verify(volunteerResponsibilityRepository, never()).delete(existing);
        var captor = ArgumentCaptor.forClass(VolunteerResponsibility.class);
        verify(volunteerResponsibilityRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(VolunteerResponsibility::getDescription)
                .containsExactlyInAnyOrder("Updated task", "New task");
    }

    @Test
    void updateVolunteerExperience_shouldDeleteResponsibilitiesNotInRequest() {
        VolunteerResponsibility toRemove = VolunteerResponsibility.builder()
                .volunteerExperience(volunteerExperience).description("Obsolete").build();
        ReflectionTestUtils.setField(toRemove, "id", 20L);
        volunteerExperience.addResponsibility(toRemove);

        UpdateVolunteerExperienceRequest request = new UpdateVolunteerExperienceRequest();
        request.setResponsibilities(List.of());

        when(volunteerExperienceRepository.findById(1L)).thenReturn(Optional.of(volunteerExperience));
        when(volunteerExperienceRepository.save(volunteerExperience)).thenReturn(volunteerExperience);
        when(volunteerResponsibilityRepository.findAllByVolunteerExperienceOrderBySortOrderAsc(volunteerExperience))
                .thenReturn(List.of(toRemove));
        when(volunteerExperienceMapper.toDetailDto(volunteerExperience)).thenReturn(VolunteerExperienceDetailDto.builder().build());

        volunteerExperienceService.updateVolunteerExperience(1L, request);

        verify(volunteerResponsibilityRepository).delete(toRemove);
        assertThat(volunteerExperience.getResponsibilities()).doesNotContain(toRemove);
    }

    @Test
    void updateVolunteerExperience_shouldUpdateContributionPercentageOnExistingProjectLink() {
        Project project = buildProject(10L, "Water Wells");
        VolunteerProject existingLink = VolunteerProject.builder()
                .volunteerExperience(volunteerExperience).project(project)
                .contributionPercentage(new BigDecimal("20.0")).build();
        ReflectionTestUtils.setField(existingLink, "id", 5L);

        VolunteerProjectRequest updateExisting = new VolunteerProjectRequest();
        updateExisting.setId(5L);
        updateExisting.setProjectId(10L);
        updateExisting.setContributionPercentage(new BigDecimal("55.0"));

        UpdateVolunteerExperienceRequest request = new UpdateVolunteerExperienceRequest();
        request.setProjects(List.of(updateExisting));

        when(volunteerExperienceRepository.findById(1L)).thenReturn(Optional.of(volunteerExperience));
        when(volunteerExperienceRepository.save(volunteerExperience)).thenReturn(volunteerExperience);
        when(volunteerProjectRepository.findAllByVolunteerExperience(volunteerExperience)).thenReturn(List.of(existingLink));
        when(volunteerProjectRepository.save(any(VolunteerProject.class))).thenAnswer(inv -> inv.getArgument(0));
        when(volunteerExperienceMapper.toDetailDto(volunteerExperience)).thenReturn(VolunteerExperienceDetailDto.builder().build());

        volunteerExperienceService.updateVolunteerExperience(1L, request);

        assertThat(existingLink.getContributionPercentage()).isEqualByComparingTo("55.0");
        verify(projectRepository, never()).findById(any());
        verify(volunteerProjectRepository, never()).delete(any());
    }

    @Test
    void updateVolunteerExperience_shouldDeleteProjectLinksNotInRequest() {
        Project project = buildProject(10L, "Water Wells");
        VolunteerProject toRemove = VolunteerProject.builder()
                .volunteerExperience(volunteerExperience).project(project)
                .contributionPercentage(BigDecimal.TEN).build();
        ReflectionTestUtils.setField(toRemove, "id", 5L);
        volunteerExperience.addProject(toRemove);

        UpdateVolunteerExperienceRequest request = new UpdateVolunteerExperienceRequest();
        request.setProjects(List.of());

        when(volunteerExperienceRepository.findById(1L)).thenReturn(Optional.of(volunteerExperience));
        when(volunteerExperienceRepository.save(volunteerExperience)).thenReturn(volunteerExperience);
        when(volunteerProjectRepository.findAllByVolunteerExperience(volunteerExperience)).thenReturn(List.of(toRemove));
        when(volunteerExperienceMapper.toDetailDto(volunteerExperience)).thenReturn(VolunteerExperienceDetailDto.builder().build());

        volunteerExperienceService.updateVolunteerExperience(1L, request);

        verify(volunteerProjectRepository).delete(toRemove);
        assertThat(volunteerExperience.getVolunteerProjects()).doesNotContain(toRemove);
    }

    @Test
    void updateVolunteerExperience_shouldAddNewProjectLink_whenIdNotProvided() {
        Project project = buildProject(15L, "Beach Cleanup");
        VolunteerProjectRequest newLink = new VolunteerProjectRequest();
        newLink.setProjectId(15L);
        newLink.setContributionPercentage(new BigDecimal("60.0"));

        UpdateVolunteerExperienceRequest request = new UpdateVolunteerExperienceRequest();
        request.setProjects(List.of(newLink));

        when(volunteerExperienceRepository.findById(1L)).thenReturn(Optional.of(volunteerExperience));
        when(volunteerExperienceRepository.save(volunteerExperience)).thenReturn(volunteerExperience);
        when(volunteerProjectRepository.findAllByVolunteerExperience(volunteerExperience)).thenReturn(List.of());
        when(projectRepository.findById(15L)).thenReturn(Optional.of(project));
        when(volunteerProjectRepository.findByProject(project)).thenReturn(Optional.empty());
        when(jobProjectsRepository.findByProject(project)).thenReturn(Optional.empty());
        when(volunteerProjectRepository.save(any(VolunteerProject.class))).thenAnswer(inv -> inv.getArgument(0));
        when(volunteerExperienceMapper.toDetailDto(volunteerExperience)).thenReturn(VolunteerExperienceDetailDto.builder().build());

        volunteerExperienceService.updateVolunteerExperience(1L, request);

        assertThat(volunteerExperience.getVolunteerProjects()).hasSize(1);
        var captor = ArgumentCaptor.forClass(VolunteerProject.class);
        verify(volunteerProjectRepository).save(captor.capture());
        assertThat(captor.getValue().getProject()).isEqualTo(project);
    }

    @Test
    void updateVolunteerExperience_shouldThrowNoSuchElementException_whenNotFound() {
        UpdateVolunteerExperienceRequest request = new UpdateVolunteerExperienceRequest();
        when(volunteerExperienceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> volunteerExperienceService.updateVolunteerExperience(99L, request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Volunteer experience not found");
    }

    // ── deleteVolunteerExperience ─────────────────────────────────────────────

    @Test
    void deleteVolunteerExperience_shouldDeleteExperience_whenFound() {
        when(volunteerExperienceRepository.findById(1L)).thenReturn(Optional.of(volunteerExperience));

        volunteerExperienceService.deleteVolunteerExperience(1L);

        verify(volunteerExperienceRepository).delete(volunteerExperience);
    }

    @Test
    void deleteVolunteerExperience_shouldThrowNoSuchElementException_whenNotFound() {
        when(volunteerExperienceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> volunteerExperienceService.deleteVolunteerExperience(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Volunteer experience not found");

        verify(volunteerExperienceRepository, never()).delete(any());
    }
}
