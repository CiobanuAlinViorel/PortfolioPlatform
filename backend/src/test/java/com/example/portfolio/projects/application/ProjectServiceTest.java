package com.example.portfolio.projects.application;

import com.example.portfolio.hobby.domain.ComplexityLevel;
import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.profile.persistence.ProfileRepository;
import com.example.portfolio.projects.domain.*;
import com.example.portfolio.projects.dto.*;
import com.example.portfolio.projects.mapper.ProjectMapper;
import com.example.portfolio.projects.persistence.ProjectRepository;
import com.example.portfolio.skills.domain.ProficiencyLevel;
import com.example.portfolio.skills.domain.Skill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock private ProfileRepository profileRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectService projectService;

    private Profile profile;
    private Project project;

    @BeforeEach
    void setUp() {
        profile = Profile.builder().firstName("John").lastName("Doe").build();
        project = Project.builder()
                .profile(profile)
                .title("Portfolio API")
                .status(ProjectStatus.PRODUCTION)
                .complexity(ComplexityLevel.ADVANCED)
                .year(2024)
                .build();
    }

    // ── getProjectsList ───────────────────────────────────────────────────────

    @Test
    void getProjectsList_shouldReturnMappedList_whenProfileAndProjectsExist() {
        Project project2 = Project.builder().profile(profile).title("Blog").status(ProjectStatus.DEVELOPMENT).complexity(ComplexityLevel.INTERMEDIATE).build();
        ProjectListItemDto dto1 = ProjectListItemDto.builder().title("Portfolio API").status(ProjectStatus.PRODUCTION).build();
        ProjectListItemDto dto2 = ProjectListItemDto.builder().title("Blog").status(ProjectStatus.DEVELOPMENT).build();

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(projectRepository.findAllByProfile(profile)).thenReturn(List.of(project, project2));
        when(projectMapper.toProjectListItemDto(any(Project.class))).thenAnswer(inv -> {
            Project p = inv.getArgument(0);
            return p.getTitle().equals("Portfolio API") ? dto1 : dto2;
        });

        List<ProjectListItemDto> result = projectService.getProjectsList();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Portfolio API");
        assertThat(result.get(1).getTitle()).isEqualTo("Blog");
    }

    @Test
    void getProjectsList_shouldReturnEmptyList_whenNoProjectsExist() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(projectRepository.findAllByProfile(profile)).thenReturn(List.of());

        List<ProjectListItemDto> result = projectService.getProjectsList();

        assertThat(result).isEmpty();
        verify(projectMapper, never()).toProjectListItemDto(any());
    }

    @Test
    void getProjectsList_shouldCallRepositoryWithProfile() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(projectRepository.findAllByProfile(profile)).thenReturn(List.of());

        projectService.getProjectsList();

        verify(projectRepository).findAllByProfile(profile);
    }

    @Test
    void getProjectsList_shouldThrowNoSuchElementException_whenNoProfileExists() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectsList())
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Profile not found");

        verifyNoInteractions(projectRepository, projectMapper);
    }

    // ── getProjectById ────────────────────────────────────────────────────────

    @Test
    void getProjectById_shouldReturnDetailWithEmptySubCollections_whenProjectExistsWithNoSubEntities() {
        ProjectDetailDto baseDto = ProjectDetailDto.builder().title("Portfolio API").build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMapper.toProjectDetailDto(project)).thenReturn(baseDto);

        ProjectDetailDto result = projectService.getProjectById(1L);

        assertThat(result.getTitle()).isEqualTo("Portfolio API");
        assertThat(result.getFeatures()).isEmpty();
        assertThat(result.getChallenges()).isEmpty();
        assertThat(result.getImages()).isEmpty();
        assertThat(result.getSkills()).isEmpty();
        assertThat(result.getMetrics()).isNull();
    }

    @Test
    void getProjectById_shouldReturnDetailWithFeaturesSortedBySortOrder() {
        ProjectFeature f1 = ProjectFeature.builder().title("Auth").sortOrder(2).project(project).build();
        ProjectFeature f2 = ProjectFeature.builder().title("API").sortOrder(1).project(project).build();
        ReflectionTestUtils.setField(f1, "id", 1L);
        ReflectionTestUtils.setField(f2, "id", 2L);
        project.addFeature(f1);
        project.addFeature(f2);

        ProjectDetailDto baseDto = ProjectDetailDto.builder().title("Portfolio API").build();
        ProjectFeatureDto fd1 = ProjectFeatureDto.builder().title("Auth").sortOrder(2).build();
        ProjectFeatureDto fd2 = ProjectFeatureDto.builder().title("API").sortOrder(1).build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMapper.toProjectDetailDto(project)).thenReturn(baseDto);
        when(projectMapper.toProjectFeatureDto(any(ProjectFeature.class))).thenAnswer(inv -> {
            ProjectFeature f = inv.getArgument(0);
            return f.getTitle().equals("Auth") ? fd1 : fd2;
        });

        ProjectDetailDto result = projectService.getProjectById(1L);

        assertThat(result.getFeatures()).hasSize(2);
        assertThat(result.getFeatures().get(0).getTitle()).isEqualTo("API");
        assertThat(result.getFeatures().get(1).getTitle()).isEqualTo("Auth");
    }

    @Test
    void getProjectById_shouldReturnDetailWithChallenges() {
        ProjectChallenge challenge = ProjectChallenge.builder()
                .title("Scalability").description("Handle 1000 rps").solution("Added caching").difficulty(DifficultyLevel.HIGH).project(project).build();
        ReflectionTestUtils.setField(challenge, "id", 1L);
        project.addChallenge(challenge);

        ProjectDetailDto baseDto = ProjectDetailDto.builder().title("Portfolio API").build();
        ProjectChallengeDto challengeDto = ProjectChallengeDto.builder().title("Scalability").difficulty(DifficultyLevel.HIGH).build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMapper.toProjectDetailDto(project)).thenReturn(baseDto);
        when(projectMapper.toProjectChallengeDto(challenge)).thenReturn(challengeDto);

        ProjectDetailDto result = projectService.getProjectById(1L);

        assertThat(result.getChallenges()).hasSize(1);
        assertThat(result.getChallenges().get(0).getTitle()).isEqualTo("Scalability");
    }

    @Test
    void getProjectById_shouldReturnDetailWithSkills() {
        Skill skill = Skill.builder().name("Java").proficiency(ProficiencyLevel.EXPERT).profile(profile).build();
        ProjectSkill ps = ProjectSkill.builder().skill(skill).project(project).usage(new BigDecimal("0.9")).build();
        ReflectionTestUtils.setField(ps, "id", 1L);
        project.addSkill(ps);

        ProjectDetailDto baseDto = ProjectDetailDto.builder().title("Portfolio API").build();
        SkillInProjectDto skillDto = SkillInProjectDto.builder().name("Java").proficiency(ProficiencyLevel.EXPERT).usage(new BigDecimal("0.9")).build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMapper.toProjectDetailDto(project)).thenReturn(baseDto);
        when(projectMapper.toSkillInProjectDto(ps)).thenReturn(skillDto);

        ProjectDetailDto result = projectService.getProjectById(1L);

        assertThat(result.getSkills()).hasSize(1);
        assertThat(result.getSkills().get(0).getName()).isEqualTo("Java");
        assertThat(result.getSkills().get(0).getUsage()).isEqualByComparingTo("0.9");
    }

    @Test
    void getProjectById_shouldIncludeMetrics_whenPresent() {
        ProjectMetrics metrics = ProjectMetrics.builder().project(project).linesOfCode(5000L).commitsCount(120).build();
        ReflectionTestUtils.setField(metrics, "id", 1L);
        project.setMetrics(metrics);

        ProjectDetailDto baseDto = ProjectDetailDto.builder().title("Portfolio API").build();
        ProjectMetricsDto metricsDto = ProjectMetricsDto.builder().linesOfCode(5000L).commitsCount(120).build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMapper.toProjectDetailDto(project)).thenReturn(baseDto);
        when(projectMapper.toProjectMetricsDto(metrics)).thenReturn(metricsDto);

        ProjectDetailDto result = projectService.getProjectById(1L);

        assertThat(result.getMetrics()).isNotNull();
        assertThat(result.getMetrics().getLinesOfCode()).isEqualTo(5000L);
        assertThat(result.getMetrics().getCommitsCount()).isEqualTo(120);
    }

    @Test
    void getProjectById_shouldThrowNoSuchElementException_whenProjectNotFound() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectById(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Project not found");

        verify(projectMapper, never()).toProjectDetailDto(any());
    }
}
