package com.example.portfolio.education.api;

import com.example.portfolio.auth.application.JwtService;
import com.example.portfolio.auth.domain.User;
import com.example.portfolio.auth.domain.UserRole;
import com.example.portfolio.auth.persistence.UserRepository;
import com.example.portfolio.education.application.EducationService;
import com.example.portfolio.education.domain.EducationLevel;
import com.example.portfolio.education.domain.EducationStatus;
import com.example.portfolio.education.dto.*;
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

@WebMvcTest(controllers = EducationController.class)
@Import(SecurityConfig.class)
@MockBean(JpaMetamodelMappingContext.class)
class EducationControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private EducationService educationService;
    @MockBean private JwtService jwtService;
    @MockBean private UserRepository userRepository;

    // ── helpers ───────────────────────────────────────────────────────────────

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

    private EducationDto buildEducationDto() {
        return EducationDto.builder()
                .id(1L).level(EducationLevel.BACHELOR)
                .institution("University of Tech").degree("B.Sc.")
                .fieldOfStudy("Computer Science").location("Bucharest")
                .startDate(LocalDate.of(2020, 9, 1)).endDate(LocalDate.of(2024, 6, 30))
                .status(EducationStatus.COMPLETED).gpa("9.5")
                .build();
    }

    private EducationDetailDto buildEducationDetailDto() {
        return EducationDetailDto.builder()
                .id(1L).level(EducationLevel.BACHELOR)
                .institution("University of Tech").fieldOfStudy("Computer Science")
                .status(EducationStatus.COMPLETED)
                .courses(List.of(
                        CourseDto.builder().id(1L).title("Algorithms").grade("A").year(2021).relevant(true).build()
                ))
                .achievements(List.of(
                        AchievementInEducationDto.builder()
                                .achievementId(1L).achievementTitle("Best Student")
                                .topic("Algorithms").grade(new BigDecimal("10")).maxGrade(new BigDecimal("10"))
                                .semester(1).academicYear(2021).build()
                ))
                .build();
    }

    // ── GET /education — happy path ───────────────────────────────────────────

    @Test
    void getEducations_shouldReturn200WithList_withNoFilters() throws Exception {
        when(educationService.getEducations(null, null)).thenReturn(List.of(buildEducationDto()));

        mockMvc.perform(get("/education"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].institution").value("University of Tech"))
                .andExpect(jsonPath("$[0].level").value("BACHELOR"))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));
    }

    @Test
    void getEducations_shouldReturn200_withLevelFilter() throws Exception {
        when(educationService.getEducations(EducationLevel.BACHELOR, null)).thenReturn(List.of(buildEducationDto()));

        mockMvc.perform(get("/education").param("level", "BACHELOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(educationService).getEducations(EducationLevel.BACHELOR, null);
    }

    @Test
    void getEducations_shouldReturn200_withStatusFilter() throws Exception {
        when(educationService.getEducations(null, EducationStatus.COMPLETED)).thenReturn(List.of(buildEducationDto()));

        mockMvc.perform(get("/education").param("status", "COMPLETED"))
                .andExpect(status().isOk());

        verify(educationService).getEducations(null, EducationStatus.COMPLETED);
    }

    @Test
    void getEducations_shouldReturn200_withBothFilters() throws Exception {
        when(educationService.getEducations(EducationLevel.MASTER, EducationStatus.ONGOING)).thenReturn(List.of());

        mockMvc.perform(get("/education")
                        .param("level", "MASTER")
                        .param("status", "ONGOING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(educationService).getEducations(EducationLevel.MASTER, EducationStatus.ONGOING);
    }

    @Test
    void getEducations_shouldReturn200WithEmptyList_whenNoneExist() throws Exception {
        when(educationService.getEducations(null, null)).thenReturn(List.of());

        mockMvc.perform(get("/education"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getEducations_shouldBeAccessibleWithoutAuthentication() throws Exception {
        when(educationService.getEducations(null, null)).thenReturn(List.of());

        mockMvc.perform(get("/education"))
                .andExpect(status().isOk());

        verify(jwtService, never()).extractEmail(any());
    }

    // ── GET /education/{id} — happy path ─────────────────────────────────────

    @Test
    void getEducation_shouldReturn200WithDetailCoursesAndAchievements_whenFound() throws Exception {
        when(educationService.getEducationById(1L)).thenReturn(buildEducationDetailDto());

        mockMvc.perform(get("/education/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.institution").value("University of Tech"))
                .andExpect(jsonPath("$.level").value("BACHELOR"))
                .andExpect(jsonPath("$.courses").isArray())
                .andExpect(jsonPath("$.courses.length()").value(1))
                .andExpect(jsonPath("$.courses[0].title").value("Algorithms"))
                .andExpect(jsonPath("$.courses[0].grade").value("A"))
                .andExpect(jsonPath("$.achievements").isArray())
                .andExpect(jsonPath("$.achievements.length()").value(1))
                .andExpect(jsonPath("$.achievements[0].achievementTitle").value("Best Student"))
                .andExpect(jsonPath("$.achievements[0].topic").value("Algorithms"));
    }

    @Test
    void getEducation_shouldReturn404_whenNotFound() throws Exception {
        when(educationService.getEducationById(99L))
                .thenThrow(new NoSuchElementException("Education not found"));

        mockMvc.perform(get("/education/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Education not found"));
    }

    @Test
    void getEducation_shouldBeAccessibleWithoutAuthentication() throws Exception {
        when(educationService.getEducationById(1L)).thenReturn(buildEducationDetailDto());

        mockMvc.perform(get("/education/1"))
                .andExpect(status().isOk());

        verify(jwtService, never()).extractEmail(any());
    }

    // ── POST /education — happy path ──────────────────────────────────────────

    @Test
    void createEducation_shouldReturn201WithDto_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();
        CreateEducationRequest request = CreateEducationRequest.builder()
                .level(EducationLevel.BACHELOR).institution("University of Tech")
                .fieldOfStudy("Computer Science").startDate(LocalDate.of(2020, 9, 1))
                .status(EducationStatus.COMPLETED).build();
        when(educationService.createEducation(any(CreateEducationRequest.class))).thenReturn(buildEducationDto());

        mockMvc.perform(post("/education")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.institution").value("University of Tech"))
                .andExpect(jsonPath("$.level").value("BACHELOR"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    // ── POST /education — validation ──────────────────────────────────────────

    @Test
    void createEducation_shouldReturn400_whenInstitutionIsBlank() throws Exception {
        setupAdminAuth();
        CreateEducationRequest request = CreateEducationRequest.builder()
                .level(EducationLevel.BACHELOR).institution("")
                .fieldOfStudy("CS").startDate(LocalDate.now())
                .status(EducationStatus.ONGOING).build();

        mockMvc.perform(post("/education")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(educationService, never()).createEducation(any());
    }

    @Test
    void createEducation_shouldReturn400_whenLevelIsNull() throws Exception {
        setupAdminAuth();
        CreateEducationRequest request = CreateEducationRequest.builder()
                .level(null).institution("University")
                .fieldOfStudy("CS").startDate(LocalDate.now())
                .status(EducationStatus.ONGOING).build();

        mockMvc.perform(post("/education")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEducation_shouldReturn400_whenFieldOfStudyIsBlank() throws Exception {
        setupAdminAuth();
        CreateEducationRequest request = CreateEducationRequest.builder()
                .level(EducationLevel.BACHELOR).institution("University")
                .fieldOfStudy("").startDate(LocalDate.now())
                .status(EducationStatus.ONGOING).build();

        mockMvc.perform(post("/education")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEducation_shouldReturn400_whenStartDateIsNull() throws Exception {
        setupAdminAuth();
        CreateEducationRequest request = CreateEducationRequest.builder()
                .level(EducationLevel.BACHELOR).institution("University")
                .fieldOfStudy("CS").startDate(null)
                .status(EducationStatus.ONGOING).build();

        mockMvc.perform(post("/education")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── POST /education — security ────────────────────────────────────────────

    @Test
    void createEducation_shouldReturn401_whenNotAuthenticated() throws Exception {
        CreateEducationRequest request = CreateEducationRequest.builder()
                .level(EducationLevel.BACHELOR).institution("University")
                .fieldOfStudy("CS").startDate(LocalDate.now())
                .status(EducationStatus.ONGOING).build();

        mockMvc.perform(post("/education")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(educationService, never()).createEducation(any());
    }

    @Test
    void createEducation_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();
        CreateEducationRequest request = CreateEducationRequest.builder()
                .level(EducationLevel.BACHELOR).institution("University")
                .fieldOfStudy("CS").startDate(LocalDate.now())
                .status(EducationStatus.ONGOING).build();

        mockMvc.perform(post("/education")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(educationService, never()).createEducation(any());
    }

    // ── PUT /education/{id} — happy path ──────────────────────────────────────

    @Test
    void updateEducation_shouldReturn200WithDto_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();
        UpdateEducationRequest request = UpdateEducationRequest.builder()
                .gpa("10.0").status(EducationStatus.COMPLETED).build();
        when(educationService.updateEducation(eq(1L), any(UpdateEducationRequest.class)))
                .thenReturn(buildEducationDto());

        mockMvc.perform(put("/education/1")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.institution").value("University of Tech"));
    }

    @Test
    void updateEducation_shouldReturn404_whenNotFound() throws Exception {
        setupAdminAuth();
        UpdateEducationRequest request = UpdateEducationRequest.builder().gpa("9").build();
        when(educationService.updateEducation(eq(99L), any()))
                .thenThrow(new NoSuchElementException("Education not found"));

        mockMvc.perform(put("/education/99")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Education not found"));
    }

    // ── PUT /education/{id} — security ────────────────────────────────────────

    @Test
    void updateEducation_shouldReturn401_whenNotAuthenticated() throws Exception {
        UpdateEducationRequest request = UpdateEducationRequest.builder().gpa("9").build();

        mockMvc.perform(put("/education/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(educationService, never()).updateEducation(any(), any());
    }

    @Test
    void updateEducation_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();
        UpdateEducationRequest request = UpdateEducationRequest.builder().gpa("9").build();

        mockMvc.perform(put("/education/1")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ── DELETE /education/{id} — happy path ───────────────────────────────────

    @Test
    void deleteEducation_shouldReturn204_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();
        doNothing().when(educationService).deleteEducation(1L);

        mockMvc.perform(delete("/education/1")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNoContent());

        verify(educationService).deleteEducation(1L);
    }

    @Test
    void deleteEducation_shouldReturn404_whenNotFound() throws Exception {
        setupAdminAuth();
        doThrow(new NoSuchElementException("Education not found"))
                .when(educationService).deleteEducation(99L);

        mockMvc.perform(delete("/education/99")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Education not found"));
    }

    // ── DELETE /education/{id} — security ─────────────────────────────────────

    @Test
    void deleteEducation_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/education/1"))
                .andExpect(status().isUnauthorized());

        verify(educationService, never()).deleteEducation(any());
    }

    @Test
    void deleteEducation_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();

        mockMvc.perform(delete("/education/1")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden());
    }
}
