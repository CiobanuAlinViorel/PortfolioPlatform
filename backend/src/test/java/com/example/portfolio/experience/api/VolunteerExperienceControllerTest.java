package com.example.portfolio.experience.api;

import com.example.portfolio.auth.application.JwtService;
import com.example.portfolio.auth.domain.User;
import com.example.portfolio.auth.domain.UserRole;
import com.example.portfolio.auth.persistence.UserRepository;
import com.example.portfolio.experience.application.VolunteerExperienceService;
import com.example.portfolio.experience.domain.VolunteerStatus;
import com.example.portfolio.experience.domain.VolunteerType;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = VolunteerExperienceController.class)
@Import({SecurityConfig.class, JacksonConfig.class})
@MockBean(JpaMetamodelMappingContext.class)
class VolunteerExperienceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VolunteerExperienceService volunteerExperienceService;

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

    private CreateVolunteerExperienceRequest buildCreateRequest() {
        CreateVolunteerExperienceRequest request = new CreateVolunteerExperienceRequest();
        request.setOrganization("Red Cross");
        request.setRole("Coordinator");
        request.setType(VolunteerType.NGO);
        request.setStartDate(LocalDate.of(2022, 1, 1));
        request.setStatus(VolunteerStatus.ONGOING);
        return request;
    }

    private List<VolunteerExperienceListItemDto> buildList() {
        return List.of(
                VolunteerExperienceListItemDto.builder()
                        .id(1L).organization("Red Cross").role("Coordinator")
                        .type(VolunteerType.NGO).status(VolunteerStatus.ONGOING)
                        .startDate(LocalDate.of(2022, 1, 1)).projectsCount(1).build()
        );
    }

    private VolunteerExperienceDetailDto buildDetail() {
        return VolunteerExperienceDetailDto.builder()
                .id(1L).organization("Red Cross").role("Coordinator")
                .type(VolunteerType.NGO).status(VolunteerStatus.ONGOING)
                .startDate(LocalDate.of(2022, 1, 1))
                .responsibilities(List.of(
                        VolunteerResponsibilityDto.builder().id(1L).description("Organize donations").build()
                ))
                .projects(List.of(
                        VolunteerProjectDto.builder().id(1L).contributionPercentage(new BigDecimal("40.0"))
                                .project(ProjectInVolunteerDto.builder().id(10L).title("Water Wells").status(ProjectStatus.PRODUCTION).build())
                                .build()
                ))
                .build();
    }

    // ── GET /experience/volunteers ────────────────────────────────────────────

    @Test
    void getVolunteerExperiences_shouldReturn200WithList() throws Exception {
        when(volunteerExperienceService.getVolunteerExperienceList()).thenReturn(buildList());

        mockMvc.perform(get("/experience/volunteers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].organization").value("Red Cross"))
                .andExpect(jsonPath("$[0].type").value("NGO"));
    }

    @Test
    void getVolunteerExperiences_shouldReturn404_whenProfileNotFound() throws Exception {
        when(volunteerExperienceService.getVolunteerExperienceList()).thenThrow(new NoSuchElementException("Profile not found"));

        mockMvc.perform(get("/experience/volunteers"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Profile not found"));
    }

    @Test
    void getVolunteerExperiences_shouldBeAccessibleWithoutAuthentication() throws Exception {
        when(volunteerExperienceService.getVolunteerExperienceList()).thenReturn(List.of());

        mockMvc.perform(get("/experience/volunteers"))
                .andExpect(status().isOk());

        verify(jwtService, never()).extractEmail(any());
    }

    // ── GET /experience/volunteers/{id} ───────────────────────────────────────

    @Test
    void getVolunteerExperience_shouldReturn200WithResponsibilitiesAndProjects() throws Exception {
        when(volunteerExperienceService.getVolunteerExperienceById(1L)).thenReturn(buildDetail());

        mockMvc.perform(get("/experience/volunteers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responsibilities[0].description").value("Organize donations"))
                .andExpect(jsonPath("$.projects[0].contributionPercentage").value(40.0))
                .andExpect(jsonPath("$.projects[0].project.title").value("Water Wells"));
    }

    @Test
    void getVolunteerExperience_shouldReturn404_whenNotFound() throws Exception {
        when(volunteerExperienceService.getVolunteerExperienceById(99L))
                .thenThrow(new NoSuchElementException("Volunteer experience not found"));

        mockMvc.perform(get("/experience/volunteers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Volunteer experience not found"));
    }

    // ── POST /experience/volunteers ───────────────────────────────────────────

    @Test
    void createVolunteerExperience_shouldReturn201_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();
        when(volunteerExperienceService.createVolunteerExperience(any())).thenReturn(buildDetail());

        mockMvc.perform(post("/experience/volunteers")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.organization").value("Red Cross"));
    }

    @Test
    void createVolunteerExperience_shouldReturn400_whenOrganizationIsBlank() throws Exception {
        setupAdminAuth();
        CreateVolunteerExperienceRequest request = buildCreateRequest();
        request.setOrganization("");

        mockMvc.perform(post("/experience/volunteers")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(volunteerExperienceService, never()).createVolunteerExperience(any());
    }

    @Test
    void createVolunteerExperience_shouldReturn400_whenTypeIsMissing() throws Exception {
        setupAdminAuth();
        CreateVolunteerExperienceRequest request = buildCreateRequest();
        request.setType(null);

        mockMvc.perform(post("/experience/volunteers")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createVolunteerExperience_shouldReturn400_whenProjectAlreadyLinked() throws Exception {
        setupAdminAuth();
        when(volunteerExperienceService.createVolunteerExperience(any()))
                .thenThrow(new IllegalArgumentException("Project is already linked to a volunteer experience"));

        mockMvc.perform(post("/experience/volunteers")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Project is already linked to a volunteer experience"));
    }

    @Test
    void createVolunteerExperience_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/experience/volunteers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateRequest())))
                .andExpect(status().isUnauthorized());

        verify(volunteerExperienceService, never()).createVolunteerExperience(any());
    }

    @Test
    void createVolunteerExperience_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();

        mockMvc.perform(post("/experience/volunteers")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateRequest())))
                .andExpect(status().isForbidden());

        verify(volunteerExperienceService, never()).createVolunteerExperience(any());
    }

    // ── PUT /experience/volunteers/{id} ───────────────────────────────────────

    @Test
    void updateVolunteerExperience_shouldReturn200_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();
        UpdateVolunteerExperienceRequest request = new UpdateVolunteerExperienceRequest();
        request.setRole("Lead Coordinator");
        when(volunteerExperienceService.updateVolunteerExperience(eq(1L), any())).thenReturn(buildDetail());

        mockMvc.perform(put("/experience/volunteers/1")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organization").value("Red Cross"));
    }

    @Test
    void updateVolunteerExperience_shouldReturn404_whenNotFound() throws Exception {
        setupAdminAuth();
        UpdateVolunteerExperienceRequest request = new UpdateVolunteerExperienceRequest();
        when(volunteerExperienceService.updateVolunteerExperience(eq(99L), any()))
                .thenThrow(new NoSuchElementException("Volunteer experience not found"));

        mockMvc.perform(put("/experience/volunteers/99")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Volunteer experience not found"));
    }

    @Test
    void updateVolunteerExperience_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();
        UpdateVolunteerExperienceRequest request = new UpdateVolunteerExperienceRequest();

        mockMvc.perform(put("/experience/volunteers/1")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(volunteerExperienceService, never()).updateVolunteerExperience(any(), any());
    }

    // ── DELETE /experience/volunteers/{id} ────────────────────────────────────

    @Test
    void deleteVolunteerExperience_shouldReturn204_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();

        mockMvc.perform(delete("/experience/volunteers/1")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNoContent());

        verify(volunteerExperienceService).deleteVolunteerExperience(1L);
    }

    @Test
    void deleteVolunteerExperience_shouldReturn404_whenNotFound() throws Exception {
        setupAdminAuth();
        doThrow(new NoSuchElementException("Volunteer experience not found"))
                .when(volunteerExperienceService).deleteVolunteerExperience(99L);

        mockMvc.perform(delete("/experience/volunteers/99")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Volunteer experience not found"));
    }

    @Test
    void deleteVolunteerExperience_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/experience/volunteers/1"))
                .andExpect(status().isUnauthorized());

        verify(volunteerExperienceService, never()).deleteVolunteerExperience(any());
    }

    @Test
    void deleteVolunteerExperience_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();

        mockMvc.perform(delete("/experience/volunteers/1")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden());

        verify(volunteerExperienceService, never()).deleteVolunteerExperience(any());
    }
}
