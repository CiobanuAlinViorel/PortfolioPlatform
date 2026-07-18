package com.example.portfolio.experience.application;

import com.example.portfolio.education.domain.CourseProject;
import com.example.portfolio.education.persistence.CourseProjectRepository;
import com.example.portfolio.experience.domain.JobExperience;
import com.example.portfolio.experience.domain.JobProjects;
import com.example.portfolio.experience.domain.VolunteerProject;
import com.example.portfolio.experience.dto.*;
import com.example.portfolio.experience.mapper.JobExperienceMapper;
import com.example.portfolio.experience.persistence.JobExperienceRepository;
import com.example.portfolio.experience.persistence.JobProjectsRepository;
import com.example.portfolio.experience.persistence.VolunteerProjectRepository;
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
class JobExperienceServiceTest {

    @Mock private ProfileRepository profileRepository;
    @Mock private JobExperienceRepository jobExperienceRepository;
    @Mock private JobProjectsRepository jobProjectsRepository;
    @Mock private VolunteerProjectRepository volunteerProjectRepository;
    @Mock private CourseProjectRepository courseProjectRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private JobExperienceMapper jobExperienceMapper;

    @InjectMocks
    private JobExperienceService jobExperienceService;

    private Profile profile;
    private JobExperience job1;
    private JobExperience job2;

    @BeforeEach
    void setUp() {
        profile = Profile.builder().firstName("John").lastName("Doe").build();

        job1 = JobExperience.builder()
                .profile(profile)
                .companyName("Recent Corp")
                .role("Senior Dev")
                .startDate(LocalDate.of(2023, 1, 1))
                .build();
        ReflectionTestUtils.setField(job1, "id", 1L);

        job2 = JobExperience.builder()
                .profile(profile)
                .companyName("Old Corp")
                .role("Junior Dev")
                .startDate(LocalDate.of(2020, 1, 1))
                .endDate(LocalDate.of(2022, 12, 31))
                .build();
        ReflectionTestUtils.setField(job2, "id", 2L);
    }

    // ── getJobExperienceList ──────────────────────────────────────────────────

    @Test
    void getJobExperienceList_shouldReturnListInDescOrder_whenProfileAndJobsExist() {
        JobExperienceListItemDto dto1 = JobExperienceListItemDto.builder().companyName("Recent Corp").build();
        JobExperienceListItemDto dto2 = JobExperienceListItemDto.builder().companyName("Old Corp").build();

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(jobExperienceRepository.findAllByProfileOrderByStartDateDesc(profile)).thenReturn(List.of(job1, job2));
        when(jobExperienceMapper.toListItemDto(job1)).thenReturn(dto1);
        when(jobExperienceMapper.toListItemDto(job2)).thenReturn(dto2);

        List<JobExperienceListItemDto> result = jobExperienceService.getJobExperienceList();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCompanyName()).isEqualTo("Recent Corp");
        assertThat(result.get(1).getCompanyName()).isEqualTo("Old Corp");
    }

    @Test
    void getJobExperienceList_shouldReturnEmptyList_whenNoJobsExist() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(jobExperienceRepository.findAllByProfileOrderByStartDateDesc(profile)).thenReturn(List.of());

        List<JobExperienceListItemDto> result = jobExperienceService.getJobExperienceList();

        assertThat(result).isEmpty();
        verify(jobExperienceMapper, never()).toListItemDto(any());
    }

    @Test
    void getJobExperienceList_shouldThrowNoSuchElementException_whenProfileNotFound() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobExperienceService.getJobExperienceList())
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Profile not found");

        verifyNoInteractions(jobExperienceRepository, jobExperienceMapper);
    }

    // ── getJobExperienceById ──────────────────────────────────────────────────

    @Test
    void getJobExperienceById_shouldReturnDetailWithEmptyProjects_whenNoProjectsLinked() {
        JobExperienceDetailDto dto = JobExperienceDetailDto.builder().companyName("Recent Corp").build();

        when(jobExperienceRepository.findById(1L)).thenReturn(Optional.of(job1));
        when(jobExperienceMapper.toDetailDto(job1)).thenReturn(dto);

        JobExperienceDetailDto result = jobExperienceService.getJobExperienceById(1L);

        assertThat(result.getCompanyName()).isEqualTo("Recent Corp");
        assertThat(result.getProjects()).isEmpty();
    }

    @Test
    void getJobExperienceById_shouldReturnDetailWithProjects_whenProjectsLinked() {
        Project project = Project.builder().profile(profile).title("API Project").status(ProjectStatus.PRODUCTION).complexity(ComplexityLevel.ADVANCED).build();
        ReflectionTestUtils.setField(project, "id", 10L);
        JobProjects link = JobProjects.builder().jobExperience(job1).project(project).build();
        job1.addProject(link);

        JobExperienceDetailDto dto = JobExperienceDetailDto.builder().companyName("Recent Corp").build();
        ProjectInJobDto projectDto = ProjectInJobDto.builder().title("API Project").build();

        when(jobExperienceRepository.findById(1L)).thenReturn(Optional.of(job1));
        when(jobExperienceMapper.toDetailDto(job1)).thenReturn(dto);
        when(jobExperienceMapper.toProjectInJobDto(project)).thenReturn(projectDto);

        JobExperienceDetailDto result = jobExperienceService.getJobExperienceById(1L);

        assertThat(result.getProjects()).hasSize(1);
        assertThat(result.getProjects().get(0).getTitle()).isEqualTo("API Project");
    }

    @Test
    void getJobExperienceById_shouldThrowNoSuchElementException_whenJobNotFound() {
        when(jobExperienceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobExperienceService.getJobExperienceById(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Job experience not found");
    }

    // ── createJobExperience ───────────────────────────────────────────────────

    @Test
    void createJobExperience_shouldSaveAndReturnDetail() {
        CreateJobExperienceRequest request = CreateJobExperienceRequest.builder()
                .companyName("New Corp").role("Lead Dev").startDate(LocalDate.of(2024, 1, 1)).build();
        JobExperience newJob = JobExperience.builder().profile(profile).companyName("New Corp").role("Lead Dev").startDate(LocalDate.of(2024, 1, 1)).build();
        JobExperienceDetailDto dto = JobExperienceDetailDto.builder().companyName("New Corp").role("Lead Dev").build();

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(jobExperienceMapper.toJobExperience(request)).thenReturn(newJob);
        when(jobExperienceRepository.save(newJob)).thenReturn(newJob);
        when(jobExperienceMapper.toDetailDto(newJob)).thenReturn(dto);

        JobExperienceDetailDto result = jobExperienceService.createJobExperience(request);

        assertThat(result.getCompanyName()).isEqualTo("New Corp");
        verify(jobExperienceRepository).save(newJob);
    }

    @Test
    void createJobExperience_shouldThrowNoSuchElementException_whenProfileNotFound() {
        CreateJobExperienceRequest request = CreateJobExperienceRequest.builder()
                .companyName("Corp").role("Dev").startDate(LocalDate.now()).build();

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobExperienceService.createJobExperience(request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Profile not found");

        verifyNoInteractions(jobExperienceRepository);
    }

    @Test
    void createJobExperience_shouldPersistProjects_whenProvided() {
        Project project = Project.builder().profile(profile).title("API").status(ProjectStatus.PRODUCTION).complexity(ComplexityLevel.ADVANCED).build();
        ReflectionTestUtils.setField(project, "id", 10L);

        JobProjectRequest pr = new JobProjectRequest();
        pr.setProjectId(10L);
        CreateJobExperienceRequest request = CreateJobExperienceRequest.builder()
                .companyName("New Corp").role("Lead Dev").startDate(LocalDate.of(2024, 1, 1))
                .projects(List.of(pr)).build();
        JobExperience newJob = JobExperience.builder().profile(profile).companyName("New Corp").role("Lead Dev").startDate(LocalDate.of(2024, 1, 1)).build();
        ReflectionTestUtils.setField(newJob, "id", 5L);

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(jobExperienceMapper.toJobExperience(request)).thenReturn(newJob);
        when(jobExperienceRepository.save(newJob)).thenReturn(newJob);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(jobProjectsRepository.findByProject(project)).thenReturn(Optional.empty());
        when(volunteerProjectRepository.findByProject(project)).thenReturn(Optional.empty());
        when(courseProjectRepository.findByProject(project)).thenReturn(Optional.empty());
        when(jobProjectsRepository.save(any(JobProjects.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jobExperienceMapper.toDetailDto(newJob)).thenReturn(JobExperienceDetailDto.builder().build());
        when(jobExperienceMapper.toProjectInJobDto(project)).thenReturn(ProjectInJobDto.builder().title("API").build());

        JobExperienceDetailDto result = jobExperienceService.createJobExperience(request);

        var captor = ArgumentCaptor.forClass(JobProjects.class);
        verify(jobProjectsRepository).save(captor.capture());
        assertThat(captor.getValue().getProject()).isEqualTo(project);
        assertThat(result.getProjects()).hasSize(1);
    }

    @Test
    void createJobExperience_shouldThrowIllegalArgumentException_whenProjectAlreadyLinkedToVolunteerExperience() {
        Project project = Project.builder().profile(profile).title("API").status(ProjectStatus.PRODUCTION).complexity(ComplexityLevel.ADVANCED).build();
        ReflectionTestUtils.setField(project, "id", 10L);
        VolunteerProject volunteerLink = VolunteerProject.builder().project(project).contributionPercentage(BigDecimal.TEN).build();

        JobProjectRequest pr = new JobProjectRequest();
        pr.setProjectId(10L);
        CreateJobExperienceRequest request = CreateJobExperienceRequest.builder()
                .companyName("New Corp").role("Lead Dev").startDate(LocalDate.of(2024, 1, 1))
                .projects(List.of(pr)).build();
        JobExperience newJob = JobExperience.builder().profile(profile).companyName("New Corp").role("Lead Dev").startDate(LocalDate.of(2024, 1, 1)).build();

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(jobExperienceMapper.toJobExperience(request)).thenReturn(newJob);
        when(jobExperienceRepository.save(newJob)).thenReturn(newJob);
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(jobProjectsRepository.findByProject(project)).thenReturn(Optional.empty());
        when(volunteerProjectRepository.findByProject(project)).thenReturn(Optional.of(volunteerLink));

        assertThatThrownBy(() -> jobExperienceService.createJobExperience(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project is already linked to a volunteer experience");

        verify(jobProjectsRepository, never()).save(any());
    }

    // ── updateJobExperience ───────────────────────────────────────────────────

    @Test
    void updateJobExperience_shouldApplyChangesAndReturnDetail() {
        UpdateJobExperienceRequest request = UpdateJobExperienceRequest.builder()
                .role("Principal Engineer").build();
        JobExperienceDetailDto dto = JobExperienceDetailDto.builder().companyName("Recent Corp").role("Principal Engineer").build();

        when(jobExperienceRepository.findById(1L)).thenReturn(Optional.of(job1));
        when(jobExperienceRepository.save(job1)).thenReturn(job1);
        when(jobExperienceMapper.toDetailDto(job1)).thenReturn(dto);

        JobExperienceDetailDto result = jobExperienceService.updateJobExperience(1L, request);

        assertThat(result.getRole()).isEqualTo("Principal Engineer");
        verify(jobExperienceMapper).updateJobExperience(request, job1);
        verify(jobExperienceRepository).save(job1);
    }

    @Test
    void updateJobExperience_shouldThrowNoSuchElementException_whenJobNotFound() {
        UpdateJobExperienceRequest request = UpdateJobExperienceRequest.builder().role("Dev").build();
        when(jobExperienceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobExperienceService.updateJobExperience(99L, request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Job experience not found");
    }

    @Test
    void updateJobExperience_shouldAddNewProjectLink_whenIdNotProvided() {
        Project project = Project.builder().profile(profile).title("Beach Cleanup").status(ProjectStatus.PRODUCTION).complexity(ComplexityLevel.ADVANCED).build();
        ReflectionTestUtils.setField(project, "id", 15L);

        JobProjectRequest newLink = new JobProjectRequest();
        newLink.setProjectId(15L);
        UpdateJobExperienceRequest request = UpdateJobExperienceRequest.builder().projects(List.of(newLink)).build();

        when(jobExperienceRepository.findById(1L)).thenReturn(Optional.of(job1));
        when(jobExperienceRepository.save(job1)).thenReturn(job1);
        when(jobProjectsRepository.findAllByJobExperience(job1)).thenReturn(List.of());
        when(projectRepository.findById(15L)).thenReturn(Optional.of(project));
        when(jobProjectsRepository.findByProject(project)).thenReturn(Optional.empty());
        when(volunteerProjectRepository.findByProject(project)).thenReturn(Optional.empty());
        when(courseProjectRepository.findByProject(project)).thenReturn(Optional.empty());
        when(jobProjectsRepository.save(any(JobProjects.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jobExperienceMapper.toDetailDto(job1)).thenReturn(JobExperienceDetailDto.builder().build());

        jobExperienceService.updateJobExperience(1L, request);

        assertThat(job1.getProjects()).hasSize(1);
        var captor = ArgumentCaptor.forClass(JobProjects.class);
        verify(jobProjectsRepository).save(captor.capture());
        assertThat(captor.getValue().getProject()).isEqualTo(project);
    }

    @Test
    void updateJobExperience_shouldDeleteProjectLinksNotInRequest() {
        Project project = Project.builder().profile(profile).title("API").status(ProjectStatus.PRODUCTION).complexity(ComplexityLevel.ADVANCED).build();
        ReflectionTestUtils.setField(project, "id", 10L);
        JobProjects toRemove = JobProjects.builder().jobExperience(job1).project(project).build();
        ReflectionTestUtils.setField(toRemove, "id", 5L);
        job1.addProject(toRemove);

        UpdateJobExperienceRequest request = UpdateJobExperienceRequest.builder().projects(List.of()).build();

        when(jobExperienceRepository.findById(1L)).thenReturn(Optional.of(job1));
        when(jobExperienceRepository.save(job1)).thenReturn(job1);
        when(jobProjectsRepository.findAllByJobExperience(job1)).thenReturn(List.of(toRemove));
        when(jobExperienceMapper.toDetailDto(job1)).thenReturn(JobExperienceDetailDto.builder().build());

        jobExperienceService.updateJobExperience(1L, request);

        verify(jobProjectsRepository).delete(toRemove);
        assertThat(job1.getProjects()).doesNotContain(toRemove);
    }

    @Test
    void updateJobExperience_shouldSwapLinkedProjectOnExistingLink() {
        Project oldProject = Project.builder().profile(profile).title("Old").status(ProjectStatus.PRODUCTION).complexity(ComplexityLevel.ADVANCED).build();
        ReflectionTestUtils.setField(oldProject, "id", 10L);
        Project newProject = Project.builder().profile(profile).title("New").status(ProjectStatus.PRODUCTION).complexity(ComplexityLevel.ADVANCED).build();
        ReflectionTestUtils.setField(newProject, "id", 20L);
        JobProjects existingLink = JobProjects.builder().jobExperience(job1).project(oldProject).build();
        ReflectionTestUtils.setField(existingLink, "id", 5L);

        JobProjectRequest updateExisting = new JobProjectRequest();
        updateExisting.setId(5L);
        updateExisting.setProjectId(20L);
        UpdateJobExperienceRequest request = UpdateJobExperienceRequest.builder().projects(List.of(updateExisting)).build();

        when(jobExperienceRepository.findById(1L)).thenReturn(Optional.of(job1));
        when(jobExperienceRepository.save(job1)).thenReturn(job1);
        when(jobProjectsRepository.findAllByJobExperience(job1)).thenReturn(List.of(existingLink));
        when(projectRepository.findById(20L)).thenReturn(Optional.of(newProject));
        when(jobProjectsRepository.findByProject(newProject)).thenReturn(Optional.empty());
        when(volunteerProjectRepository.findByProject(newProject)).thenReturn(Optional.empty());
        when(courseProjectRepository.findByProject(newProject)).thenReturn(Optional.empty());
        when(jobProjectsRepository.save(existingLink)).thenReturn(existingLink);
        when(jobExperienceMapper.toDetailDto(job1)).thenReturn(JobExperienceDetailDto.builder().build());

        jobExperienceService.updateJobExperience(1L, request);

        assertThat(existingLink.getProject()).isEqualTo(newProject);
        verify(jobProjectsRepository, never()).delete(any());
    }

    // ── deleteJobExperience ───────────────────────────────────────────────────

    @Test
    void deleteJobExperience_shouldDeleteJobExperience() {
        when(jobExperienceRepository.findById(1L)).thenReturn(Optional.of(job1));

        jobExperienceService.deleteJobExperience(1L);

        verify(jobExperienceRepository).delete(job1);
    }

    @Test
    void deleteJobExperience_shouldThrowNoSuchElementException_whenJobNotFound() {
        when(jobExperienceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobExperienceService.deleteJobExperience(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Job experience not found");
    }

    // ── addProject ────────────────────────────────────────────────────────────

    @Test
    void addProject_shouldLinkProjectAndReturnDetail() {
        Project project = Project.builder().profile(profile).title("API").status(ProjectStatus.PRODUCTION).complexity(ComplexityLevel.ADVANCED).build();
        ReflectionTestUtils.setField(project, "id", 10L);
        JobProjects savedLink = JobProjects.builder().jobExperience(job1).project(project).build();
        JobExperienceDetailDto dto = JobExperienceDetailDto.builder().companyName("Recent Corp").build();

        when(jobExperienceRepository.findById(1L)).thenReturn(Optional.of(job1));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(jobProjectsRepository.findByProject(project)).thenReturn(Optional.empty());
        when(volunteerProjectRepository.findByProject(project)).thenReturn(Optional.empty());
        when(courseProjectRepository.findByProject(project)).thenReturn(Optional.empty());
        when(jobProjectsRepository.save(any(JobProjects.class))).thenReturn(savedLink);
        when(jobExperienceMapper.toDetailDto(job1)).thenReturn(dto);

        JobExperienceDetailDto result = jobExperienceService.addProject(1L, 10L);

        assertThat(result).isNotNull();
        verify(jobProjectsRepository).save(any(JobProjects.class));
    }

    @Test
    void addProject_shouldThrowIllegalArgumentException_whenProjectAlreadyLinkedToJob() {
        Project project = Project.builder().profile(profile).title("API").status(ProjectStatus.PRODUCTION).complexity(ComplexityLevel.ADVANCED).build();
        ReflectionTestUtils.setField(project, "id", 10L);
        JobProjects existingLink = JobProjects.builder().jobExperience(job2).project(project).build();
        ReflectionTestUtils.setField(existingLink, "id", 77L);

        when(jobExperienceRepository.findById(1L)).thenReturn(Optional.of(job1));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(jobProjectsRepository.findByProject(project)).thenReturn(Optional.of(existingLink));

        assertThatThrownBy(() -> jobExperienceService.addProject(1L, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project is already linked to a job experience");

        verify(jobProjectsRepository, never()).save(any());
    }

    @Test
    void addProject_shouldThrowIllegalArgumentException_whenProjectAlreadyLinkedToVolunteerExperience() {
        Project project = Project.builder().profile(profile).title("API").status(ProjectStatus.PRODUCTION).complexity(ComplexityLevel.ADVANCED).build();
        ReflectionTestUtils.setField(project, "id", 10L);
        VolunteerProject volunteerLink = VolunteerProject.builder().project(project).contributionPercentage(BigDecimal.TEN).build();

        when(jobExperienceRepository.findById(1L)).thenReturn(Optional.of(job1));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(jobProjectsRepository.findByProject(project)).thenReturn(Optional.empty());
        when(volunteerProjectRepository.findByProject(project)).thenReturn(Optional.of(volunteerLink));

        assertThatThrownBy(() -> jobExperienceService.addProject(1L, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project is already linked to a volunteer experience");

        verify(jobProjectsRepository, never()).save(any());
    }

    @Test
    void addProject_shouldThrowIllegalArgumentException_whenProjectAlreadyLinkedToCourse() {
        Project project = Project.builder().profile(profile).title("API").status(ProjectStatus.PRODUCTION).complexity(ComplexityLevel.ADVANCED).build();
        ReflectionTestUtils.setField(project, "id", 10L);
        CourseProject courseLink = CourseProject.builder().project(project).build();
        ReflectionTestUtils.setField(courseLink, "id", 66L);

        when(jobExperienceRepository.findById(1L)).thenReturn(Optional.of(job1));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(jobProjectsRepository.findByProject(project)).thenReturn(Optional.empty());
        when(volunteerProjectRepository.findByProject(project)).thenReturn(Optional.empty());
        when(courseProjectRepository.findByProject(project)).thenReturn(Optional.of(courseLink));

        assertThatThrownBy(() -> jobExperienceService.addProject(1L, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project is already linked to a course");

        verify(jobProjectsRepository, never()).save(any());
    }

    @Test
    void addProject_shouldThrowNoSuchElementException_whenProjectNotFound() {
        when(jobExperienceRepository.findById(1L)).thenReturn(Optional.of(job1));
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobExperienceService.addProject(1L, 99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Project not found");
    }

    // ── removeProject ─────────────────────────────────────────────────────────

    @Test
    void removeProject_shouldUnlinkProjectAndReturnDetail() {
        Project project = Project.builder().profile(profile).title("API").status(ProjectStatus.PRODUCTION).complexity(ComplexityLevel.ADVANCED).build();
        ReflectionTestUtils.setField(project, "id", 10L);
        JobProjects link = JobProjects.builder().jobExperience(job1).project(project).build();
        job1.addProject(link);
        JobExperienceDetailDto dto = JobExperienceDetailDto.builder().companyName("Recent Corp").build();

        when(jobExperienceRepository.findById(1L)).thenReturn(Optional.of(job1));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(jobProjectsRepository.findByJobExperienceAndProject(job1, project)).thenReturn(Optional.of(link));
        when(jobExperienceMapper.toDetailDto(job1)).thenReturn(dto);

        JobExperienceDetailDto result = jobExperienceService.removeProject(1L, 10L);

        assertThat(result).isNotNull();
        verify(jobProjectsRepository).delete(link);
    }

    @Test
    void removeProject_shouldThrowNoSuchElementException_whenLinkNotFound() {
        Project project = Project.builder().profile(profile).title("API").status(ProjectStatus.PRODUCTION).complexity(ComplexityLevel.ADVANCED).build();
        ReflectionTestUtils.setField(project, "id", 10L);

        when(jobExperienceRepository.findById(1L)).thenReturn(Optional.of(job1));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(jobProjectsRepository.findByJobExperienceAndProject(job1, project)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobExperienceService.removeProject(1L, 10L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Project is not linked to this job experience");
    }

    // ── getAvailableProjects ──────────────────────────────────────────────────

    @Test
    void getAvailableProjects_shouldReturnFilteredProjectList() {
        Project p1 = Project.builder().profile(profile).title("Work Project").status(ProjectStatus.PRODUCTION).complexity(ComplexityLevel.ADVANCED).build();
        Project p2 = Project.builder().profile(profile).title("Side Project").status(ProjectStatus.DEVELOPMENT).complexity(ComplexityLevel.INTERMEDIATE).build();
        ProjectInJobDto dto1 = ProjectInJobDto.builder().title("Work Project").build();
        ProjectInJobDto dto2 = ProjectInJobDto.builder().title("Side Project").build();

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(projectRepository.findAvailableForJobExperience(profile)).thenReturn(List.of(p1, p2));
        when(jobExperienceMapper.toProjectInJobDto(p1)).thenReturn(dto1);
        when(jobExperienceMapper.toProjectInJobDto(p2)).thenReturn(dto2);

        List<ProjectInJobDto> result = jobExperienceService.getAvailableProjects();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Work Project");
        assertThat(result.get(1).getTitle()).isEqualTo("Side Project");
    }

    @Test
    void getAvailableProjects_shouldReturnEmptyList_whenNoAvailableProjects() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(projectRepository.findAvailableForJobExperience(profile)).thenReturn(List.of());

        List<ProjectInJobDto> result = jobExperienceService.getAvailableProjects();

        assertThat(result).isEmpty();
    }

    @Test
    void getAvailableProjects_shouldThrowNoSuchElementException_whenProfileNotFound() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobExperienceService.getAvailableProjects())
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Profile not found");
    }
}
