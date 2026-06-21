package com.example.portfolio.experience.api;

import com.example.portfolio.auth.application.JwtService;
import com.example.portfolio.auth.domain.User;
import com.example.portfolio.auth.domain.UserRole;
import com.example.portfolio.auth.persistence.UserRepository;
import com.example.portfolio.experience.application.JobExperienceService;
import com.example.portfolio.experience.dto.*;
import com.example.portfolio.projects.domain.ProjectStatus;
import com.example.portfolio.shared.config.JacksonConfig;
import com.example.portfolio.shared.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = JobExperienceController.class)
@Import({SecurityConfig.class, JacksonConfig.class})
@MockBean(JpaMetamodelMappingContext.class)
class JobExperienceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JobExperienceService jobExperienceService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    private void setupAdminAuth() {
        User admin = User.builder().email("admin@test.com").password("enc").role(UserRole.ADMIN).emailVerified(true).build();
        when(jwtService.extractEmail("admin-token")).thenReturn("admin@test.com");
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(jwtService.isTokenValid(eq("admin-token"), any())).thenReturn(true);
    }

    private void setupUserAuth() {
        User user = User.builder().email("user@test.com").password("enc").role(UserRole.USER).emailVerified(true).build();
        when(jwtService.extractEmail("user-token")).thenReturn("user@test.com");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(eq("user-token"), any())).thenReturn(true);
    }

    private List<JobExperienceListItemDto> buildJobList() {
        return List.of(
                JobExperienceListItemDto.builder()
                        .id(1L).companyName("Recent Corp").role("Senior Dev")
                        .startDate(LocalDate.of(2023, 1, 1)).projectsCount(2).build(),
                JobExperienceListItemDto.builder()
                        .id(2L).companyName("Old Corp").role("Junior Dev")
                        .startDate(LocalDate.of(2020, 1, 1)).endDate(LocalDate.of(2022, 12, 31)).projectsCount(1).build()
        );
    }

    private JobExperienceDetailDto buildJobDetail() {
        return JobExperienceDetailDto.builder()
                .id(1L).companyName("Recent Corp").role("Senior Dev")
                .startDate(LocalDate.of(2023, 1, 1))
                .projects(List.of(
                        ProjectInJobDto.builder().id(10L).title("API Project").status(ProjectStatus.PRODUCTION).year(2023).build()
                ))
                .build();
    }

    // ── GET /experience/jobs ──────────────────────────────────────────────────

    @Test
    void getJobExperiences_shouldReturn200WithList() throws Exception {
        when(jobExperienceService.getJobExperienceList()).thenReturn(buildJobList());

        mockMvc.perform(get("/experience/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getJobExperiences_shouldReturn200WithCorrectFields() throws Exception {
        when(jobExperienceService.getJobExperienceList()).thenReturn(buildJobList());

        mockMvc.perform(get("/experience/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].companyName").value("Recent Corp"))
                .andExpect(jsonPath("$[0].role").value("Senior Dev"))
                .andExpect(jsonPath("$[0].projectsCount").value(2))
                .andExpect(jsonPath("$[1].companyName").value("Old Corp"))
                .andExpect(jsonPath("$[1].endDate").value("2022-12-31"));
    }

    @Test
    void getJobExperiences_shouldReturn200WithEmptyList_whenNoJobsExist() throws Exception {
        when(jobExperienceService.getJobExperienceList()).thenReturn(List.of());

        mockMvc.perform(get("/experience/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getJobExperiences_shouldReturn404_whenProfileNotFound() throws Exception {
        when(jobExperienceService.getJobExperienceList()).thenThrow(new NoSuchElementException("Profile not found"));

        mockMvc.perform(get("/experience/jobs"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Profile not found"));
    }

    @Test
    void getJobExperiences_shouldBeAccessibleWithoutAuthentication() throws Exception {
        when(jobExperienceService.getJobExperienceList()).thenReturn(List.of());

        mockMvc.perform(get("/experience/jobs"))
                .andExpect(status().isOk());

        verify(jwtService, never()).extractEmail(any());
    }

    // ── GET /experience/jobs/{id} ─────────────────────────────────────────────

    @Test
    void getJobExperience_shouldReturn200WithDetail() throws Exception {
        when(jobExperienceService.getJobExperienceById(1L)).thenReturn(buildJobDetail());

        mockMvc.perform(get("/experience/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.companyName").value("Recent Corp"))
                .andExpect(jsonPath("$.role").value("Senior Dev"));
    }

    @Test
    void getJobExperience_shouldReturn200WithProjects() throws Exception {
        when(jobExperienceService.getJobExperienceById(1L)).thenReturn(buildJobDetail());

        mockMvc.perform(get("/experience/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projects").isArray())
                .andExpect(jsonPath("$.projects.length()").value(1))
                .andExpect(jsonPath("$.projects[0].title").value("API Project"))
                .andExpect(jsonPath("$.projects[0].status").value("PRODUCTION"));
    }

    @Test
    void getJobExperience_shouldReturn404_whenJobNotFound() throws Exception {
        when(jobExperienceService.getJobExperienceById(99L)).thenThrow(new NoSuchElementException("Job experience not found"));

        mockMvc.perform(get("/experience/jobs/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Job experience not found"));
    }

    @Test
    void getJobExperience_shouldBeAccessibleWithoutAuthentication() throws Exception {
        when(jobExperienceService.getJobExperienceById(1L)).thenReturn(buildJobDetail());

        mockMvc.perform(get("/experience/jobs/1"))
                .andExpect(status().isOk());

        verify(jwtService, never()).extractEmail(any());
    }

    // ── POST /experience/jobs ─────────────────────────────────────────────────

    @Test
    void createJobExperience_shouldReturn201WithDetail_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();
        CreateJobExperienceRequest request = CreateJobExperienceRequest.builder()
                .companyName("New Corp").role("Lead Dev").startDate(LocalDate.of(2024, 1, 1)).build();
        when(jobExperienceService.createJobExperience(any())).thenReturn(buildJobDetail());

        mockMvc.perform(post("/experience/jobs")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.companyName").value("Recent Corp"));
    }

    @Test
    void createJobExperience_shouldReturn400_whenCompanyNameIsBlank() throws Exception {
        setupAdminAuth();
        CreateJobExperienceRequest request = CreateJobExperienceRequest.builder()
                .companyName("").role("Dev").startDate(LocalDate.now()).build();

        mockMvc.perform(post("/experience/jobs")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createJobExperience_shouldReturn400_whenStartDateIsNull() throws Exception {
        setupAdminAuth();
        CreateJobExperienceRequest request = CreateJobExperienceRequest.builder()
                .companyName("Corp").role("Dev").startDate(null).build();

        mockMvc.perform(post("/experience/jobs")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createJobExperience_shouldReturn401_whenNotAuthenticated() throws Exception {
        CreateJobExperienceRequest request = CreateJobExperienceRequest.builder()
                .companyName("Corp").role("Dev").startDate(LocalDate.now()).build();

        mockMvc.perform(post("/experience/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(jobExperienceService, never()).createJobExperience(any());
    }

    @Test
    void createJobExperience_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();
        CreateJobExperienceRequest request = CreateJobExperienceRequest.builder()
                .companyName("Corp").role("Dev").startDate(LocalDate.now()).build();

        mockMvc.perform(post("/experience/jobs")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(jobExperienceService, never()).createJobExperience(any());
    }

    // ── PUT /experience/jobs/{id} ─────────────────────────────────────────────

    @Test
    void updateJobExperience_shouldReturn200WithUpdatedDetail_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();
        UpdateJobExperienceRequest request = UpdateJobExperienceRequest.builder().role("Principal Engineer").build();
        when(jobExperienceService.updateJobExperience(eq(1L), any())).thenReturn(buildJobDetail());

        mockMvc.perform(put("/experience/jobs/1")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Recent Corp"));
    }

    @Test
    void updateJobExperience_shouldReturn404_whenJobNotFound() throws Exception {
        setupAdminAuth();
        UpdateJobExperienceRequest request = UpdateJobExperienceRequest.builder().role("Dev").build();
        when(jobExperienceService.updateJobExperience(eq(99L), any()))
                .thenThrow(new NoSuchElementException("Job experience not found"));

        mockMvc.perform(put("/experience/jobs/99")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Job experience not found"));
    }

    @Test
    void updateJobExperience_shouldReturn401_whenNotAuthenticated() throws Exception {
        UpdateJobExperienceRequest request = UpdateJobExperienceRequest.builder().role("Dev").build();

        mockMvc.perform(put("/experience/jobs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateJobExperience_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();
        UpdateJobExperienceRequest request = UpdateJobExperienceRequest.builder().role("Dev").build();

        mockMvc.perform(put("/experience/jobs/1")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ── DELETE /experience/jobs/{id} ──────────────────────────────────────────

    @Test
    void deleteJobExperience_shouldReturn204_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();
        doNothing().when(jobExperienceService).deleteJobExperience(1L);

        mockMvc.perform(delete("/experience/jobs/1")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNoContent());

        verify(jobExperienceService).deleteJobExperience(1L);
    }

    @Test
    void deleteJobExperience_shouldReturn404_whenJobNotFound() throws Exception {
        setupAdminAuth();
        doThrow(new NoSuchElementException("Job experience not found"))
                .when(jobExperienceService).deleteJobExperience(99L);

        mockMvc.perform(delete("/experience/jobs/99")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Job experience not found"));
    }

    @Test
    void deleteJobExperience_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/experience/jobs/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteJobExperience_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();

        mockMvc.perform(delete("/experience/jobs/1")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden());
    }

    // ── POST /experience/jobs/{id}/projects/{projectId} ───────────────────────

    @Test
    void addProject_shouldReturn200WithUpdatedDetail_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();
        when(jobExperienceService.addProject(1L, 10L)).thenReturn(buildJobDetail());

        mockMvc.perform(post("/experience/jobs/1/projects/10")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Recent Corp"))
                .andExpect(jsonPath("$.projects").isArray());
    }

    @Test
    void addProject_shouldReturn400_whenProjectAlreadyLinked() throws Exception {
        setupAdminAuth();
        when(jobExperienceService.addProject(1L, 10L))
                .thenThrow(new IllegalArgumentException("Project is already linked to a job experience"));

        mockMvc.perform(post("/experience/jobs/1/projects/10")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Project is already linked to a job experience"));
    }

    @Test
    void addProject_shouldReturn404_whenJobNotFound() throws Exception {
        setupAdminAuth();
        when(jobExperienceService.addProject(99L, 10L))
                .thenThrow(new NoSuchElementException("Job experience not found"));

        mockMvc.perform(post("/experience/jobs/99/projects/10")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addProject_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/experience/jobs/1/projects/10"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addProject_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();

        mockMvc.perform(post("/experience/jobs/1/projects/10")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden());
    }

    // ── DELETE /experience/jobs/{id}/projects/{projectId} ─────────────────────

    @Test
    void removeProject_shouldReturn200WithUpdatedDetail_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();
        JobExperienceDetailDto detailWithNoProjects = JobExperienceDetailDto.builder()
                .id(1L).companyName("Recent Corp").role("Senior Dev")
                .projects(List.of()).build();
        when(jobExperienceService.removeProject(1L, 10L)).thenReturn(detailWithNoProjects);

        mockMvc.perform(delete("/experience/jobs/1/projects/10")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projects").isEmpty());
    }

    @Test
    void removeProject_shouldReturn404_whenLinkNotFound() throws Exception {
        setupAdminAuth();
        when(jobExperienceService.removeProject(1L, 99L))
                .thenThrow(new NoSuchElementException("Project is not linked to this job experience"));

        mockMvc.perform(delete("/experience/jobs/1/projects/99")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Project is not linked to this job experience"));
    }

    @Test
    void removeProject_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/experience/jobs/1/projects/10"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /experience/jobs/available-projects ───────────────────────────────

    @Test
    void getAvailableProjects_shouldReturn200WithList_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();
        List<ProjectInJobDto> available = List.of(
                ProjectInJobDto.builder().id(5L).title("Work Project").status(ProjectStatus.PRODUCTION).build()
        );
        when(jobExperienceService.getAvailableProjects()).thenReturn(available);

        mockMvc.perform(get("/experience/jobs/available-projects")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Work Project"));
    }

    @Test
    void getAvailableProjects_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/experience/jobs/available-projects"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAvailableProjects_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();

        mockMvc.perform(get("/experience/jobs/available-projects")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden());
    }
}
