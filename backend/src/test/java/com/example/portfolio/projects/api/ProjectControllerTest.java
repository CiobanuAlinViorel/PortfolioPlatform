package com.example.portfolio.projects.api;

import com.example.portfolio.auth.application.JwtService;
import com.example.portfolio.auth.persistence.UserRepository;
import com.example.portfolio.hobby.domain.ComplexityLevel;
import com.example.portfolio.projects.application.ProjectService;
import com.example.portfolio.projects.domain.DifficultyLevel;
import com.example.portfolio.projects.domain.ProjectStatus;
import com.example.portfolio.projects.dto.*;
import com.example.portfolio.shared.config.SecurityConfig;
import com.example.portfolio.skills.domain.ProficiencyLevel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProjectController.class)
@Import(SecurityConfig.class)
@MockBean(JpaMetamodelMappingContext.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    private List<ProjectListItemDto> buildProjectList() {
        return List.of(
                ProjectListItemDto.builder()
                        .id(1L).title("Portfolio API").status(ProjectStatus.PRODUCTION)
                        .complexity(ComplexityLevel.ADVANCED).year(2024)
                        .tags(List.of("java", "spring")).githubUrl("https://github.com/portfolio")
                        .sortOrder(1).build(),
                ProjectListItemDto.builder()
                        .id(2L).title("Blog API").status(ProjectStatus.DEVELOPMENT)
                        .complexity(ComplexityLevel.INTERMEDIATE).year(2023)
                        .tags(List.of("node")).sortOrder(2).build()
        );
    }

    private ProjectDetailDto buildProjectDetail() {
        return ProjectDetailDto.builder()
                .id(1L).title("Portfolio API").description("A portfolio backend")
                .longDescription("Full description here").status(ProjectStatus.PRODUCTION)
                .complexity(ComplexityLevel.ADVANCED).year(2024)
                .tags(List.of("java", "spring")).githubUrl("https://github.com/portfolio")
                .categoryName("Personal").developmentTime(3.5)
                .features(List.of(
                        ProjectFeatureDto.builder().id(1L).title("JWT Auth").sortOrder(1).build()
                ))
                .challenges(List.of(
                        ProjectChallengeDto.builder().id(1L).title("Scalability").difficulty(DifficultyLevel.HIGH).build()
                ))
                .images(List.of(
                        ProjectImageDto.builder().id(1L).title("Screenshot").imageUrl("https://img.png").primary(true).build()
                ))
                .skills(List.of(
                        SkillInProjectDto.builder().id(1L).name("Java").proficiency(ProficiencyLevel.EXPERT).usage(new BigDecimal("0.9")).build()
                ))
                .metrics(ProjectMetricsDto.builder().linesOfCode(5000L).commitsCount(120).build())
                .build();
    }

    // ── GET /projects ─────────────────────────────────────────────────────────

    @Test
    void getProjects_shouldReturn200WithProjectList() throws Exception {
        when(projectService.getProjectsList()).thenReturn(buildProjectList());

        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getProjects_shouldReturn200WithCorrectFields() throws Exception {
        when(projectService.getProjectsList()).thenReturn(buildProjectList());

        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Portfolio API"))
                .andExpect(jsonPath("$[0].status").value("PRODUCTION"))
                .andExpect(jsonPath("$[0].complexity").value("ADVANCED"))
                .andExpect(jsonPath("$[0].year").value(2024))
                .andExpect(jsonPath("$[0].tags[0]").value("java"))
                .andExpect(jsonPath("$[0].githubUrl").value("https://github.com/portfolio"));
    }

    @Test
    void getProjects_shouldReturn200WithEmptyList_whenNoProjectsExist() throws Exception {
        when(projectService.getProjectsList()).thenReturn(List.of());

        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getProjects_shouldReturn404_whenProfileNotFound() throws Exception {
        when(projectService.getProjectsList()).thenThrow(new NoSuchElementException("Profile not found"));

        mockMvc.perform(get("/projects"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Profile not found"));
    }

    @Test
    void getProjects_shouldBeAccessibleWithoutAuthentication() throws Exception {
        when(projectService.getProjectsList()).thenReturn(List.of());

        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk());

        verify(jwtService, never()).extractEmail(any());
    }

    // ── GET /projects/{id} ────────────────────────────────────────────────────

    @Test
    void getProject_shouldReturn200WithFullDetail() throws Exception {
        when(projectService.getProjectById(1L)).thenReturn(buildProjectDetail());

        mockMvc.perform(get("/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Portfolio API"))
                .andExpect(jsonPath("$.description").value("A portfolio backend"))
                .andExpect(jsonPath("$.longDescription").value("Full description here"))
                .andExpect(jsonPath("$.status").value("PRODUCTION"))
                .andExpect(jsonPath("$.complexity").value("ADVANCED"))
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.categoryName").value("Personal"));
    }

    @Test
    void getProject_shouldReturn200WithFeaturesAndChallenges() throws Exception {
        when(projectService.getProjectById(1L)).thenReturn(buildProjectDetail());

        mockMvc.perform(get("/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.features").isArray())
                .andExpect(jsonPath("$.features.length()").value(1))
                .andExpect(jsonPath("$.features[0].title").value("JWT Auth"))
                .andExpect(jsonPath("$.challenges").isArray())
                .andExpect(jsonPath("$.challenges.length()").value(1))
                .andExpect(jsonPath("$.challenges[0].title").value("Scalability"))
                .andExpect(jsonPath("$.challenges[0].difficulty").value("HIGH"));
    }

    @Test
    void getProject_shouldReturn200WithImagesAndSkills() throws Exception {
        when(projectService.getProjectById(1L)).thenReturn(buildProjectDetail());

        mockMvc.perform(get("/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.images").isArray())
                .andExpect(jsonPath("$.images[0].title").value("Screenshot"))
                .andExpect(jsonPath("$.images[0].imageUrl").value("https://img.png"))
                .andExpect(jsonPath("$.images[0].primary").value(true))
                .andExpect(jsonPath("$.skills").isArray())
                .andExpect(jsonPath("$.skills[0].name").value("Java"))
                .andExpect(jsonPath("$.skills[0].proficiency").value("EXPERT"));
    }

    @Test
    void getProject_shouldReturn200WithMetrics() throws Exception {
        when(projectService.getProjectById(1L)).thenReturn(buildProjectDetail());

        mockMvc.perform(get("/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metrics").exists())
                .andExpect(jsonPath("$.metrics.linesOfCode").value(5000))
                .andExpect(jsonPath("$.metrics.commitsCount").value(120));
    }

    @Test
    void getProject_shouldReturn200WithNullMetrics_whenNotPresent() throws Exception {
        ProjectDetailDto noMetrics = ProjectDetailDto.builder()
                .id(1L).title("Blog").status(ProjectStatus.DEVELOPMENT)
                .features(List.of()).challenges(List.of()).images(List.of()).skills(List.of())
                .metrics(null).build();
        when(projectService.getProjectById(1L)).thenReturn(noMetrics);

        mockMvc.perform(get("/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metrics").doesNotExist());
    }

    @Test
    void getProject_shouldReturn404_whenProjectNotFound() throws Exception {
        when(projectService.getProjectById(99L)).thenThrow(new NoSuchElementException("Project not found"));

        mockMvc.perform(get("/projects/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Project not found"));
    }

    @Test
    void getProject_shouldBeAccessibleWithoutAuthentication() throws Exception {
        when(projectService.getProjectById(1L)).thenReturn(buildProjectDetail());

        mockMvc.perform(get("/projects/1"))
                .andExpect(status().isOk());

        verify(jwtService, never()).extractEmail(any());
    }
}
