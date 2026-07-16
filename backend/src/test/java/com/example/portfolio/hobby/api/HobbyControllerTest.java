package com.example.portfolio.hobby.api;

import com.example.portfolio.auth.application.JwtService;
import com.example.portfolio.auth.domain.User;
import com.example.portfolio.auth.domain.UserRole;
import com.example.portfolio.auth.persistence.UserRepository;
import com.example.portfolio.experience.domain.ImpactLevel;
import com.example.portfolio.hobby.application.HobbyService;
import com.example.portfolio.hobby.domain.ActivityLevel;
import com.example.portfolio.hobby.domain.ComplexityLevel;
import com.example.portfolio.hobby.domain.HobbyCategory;
import com.example.portfolio.hobby.dto.*;
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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = HobbyController.class)
@Import(SecurityConfig.class)
@MockBean(JpaMetamodelMappingContext.class)
class HobbyControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private HobbyService hobbyService;
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

    private HobbyDto buildHobbyDto() {
        return HobbyDto.builder()
                .id(1L).name("Chess").description("Strategic board game")
                .category(HobbyCategory.GAMING).activityLevel(ActivityLevel.REGULAR)
                .complexityLevel(ComplexityLevel.ADVANCED).impactOnWork(ImpactLevel.HIGH)
                .yearsActive(5L)
                .build();
    }

    private HobbyDetailDto buildHobbyDetailDto() {
        return HobbyDetailDto.builder()
                .id(1L).name("Chess").category(HobbyCategory.GAMING)
                .activityLevel(ActivityLevel.REGULAR)
                .skills(List.of(
                        SkillInHobbyDto.builder().skillId(1L).skillName("Problem Solving")
                                .usagePercentage(new BigDecimal("0.8")).build()
                ))
                .build();
    }

    // ── GET /hobbies — happy path ─────────────────────────────────────────────

    @Test
    void getHobbies_shouldReturn200WithList_withNoFilters() throws Exception {
        when(hobbyService.getHobbies(null, null)).thenReturn(List.of(buildHobbyDto()));

        mockMvc.perform(get("/hobbies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Chess"))
                .andExpect(jsonPath("$[0].category").value("GAMING"))
                .andExpect(jsonPath("$[0].activityLevel").value("REGULAR"));
    }

    @Test
    void getHobbies_shouldReturn200_withCategoryFilter() throws Exception {
        when(hobbyService.getHobbies(HobbyCategory.GAMING, null)).thenReturn(List.of(buildHobbyDto()));

        mockMvc.perform(get("/hobbies").param("category", "GAMING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(hobbyService).getHobbies(HobbyCategory.GAMING, null);
    }

    @Test
    void getHobbies_shouldReturn200_withActivityLevelFilter() throws Exception {
        when(hobbyService.getHobbies(null, ActivityLevel.REGULAR)).thenReturn(List.of(buildHobbyDto()));

        mockMvc.perform(get("/hobbies").param("activityLevel", "REGULAR"))
                .andExpect(status().isOk());

        verify(hobbyService).getHobbies(null, ActivityLevel.REGULAR);
    }

    @Test
    void getHobbies_shouldReturn200_withBothFilters() throws Exception {
        when(hobbyService.getHobbies(HobbyCategory.GAMING, ActivityLevel.DAILY)).thenReturn(List.of());

        mockMvc.perform(get("/hobbies")
                        .param("category", "GAMING")
                        .param("activityLevel", "DAILY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(hobbyService).getHobbies(HobbyCategory.GAMING, ActivityLevel.DAILY);
    }

    @Test
    void getHobbies_shouldReturn200WithEmptyList_whenNoneExist() throws Exception {
        when(hobbyService.getHobbies(null, null)).thenReturn(List.of());

        mockMvc.perform(get("/hobbies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getHobbies_shouldBeAccessibleWithoutAuthentication() throws Exception {
        when(hobbyService.getHobbies(null, null)).thenReturn(List.of());

        mockMvc.perform(get("/hobbies"))
                .andExpect(status().isOk());

        verify(jwtService, never()).extractEmail(any());
    }

    // ── GET /hobbies/{id} — happy path ────────────────────────────────────────

    @Test
    void getHobby_shouldReturn200WithDetailAndSkills_whenFound() throws Exception {
        when(hobbyService.getHobbyById(1L)).thenReturn(buildHobbyDetailDto());

        mockMvc.perform(get("/hobbies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Chess"))
                .andExpect(jsonPath("$.category").value("GAMING"))
                .andExpect(jsonPath("$.skills").isArray())
                .andExpect(jsonPath("$.skills.length()").value(1))
                .andExpect(jsonPath("$.skills[0].skillName").value("Problem Solving"))
                .andExpect(jsonPath("$.skills[0].usagePercentage").value(0.8));
    }

    @Test
    void getHobby_shouldReturn404_whenNotFound() throws Exception {
        when(hobbyService.getHobbyById(99L)).thenThrow(new NoSuchElementException("Hobby not found"));

        mockMvc.perform(get("/hobbies/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Hobby not found"));
    }

    @Test
    void getHobby_shouldBeAccessibleWithoutAuthentication() throws Exception {
        when(hobbyService.getHobbyById(1L)).thenReturn(buildHobbyDetailDto());

        mockMvc.perform(get("/hobbies/1"))
                .andExpect(status().isOk());

        verify(jwtService, never()).extractEmail(any());
    }

    // ── POST /hobbies — happy path ────────────────────────────────────────────

    @Test
    void createHobby_shouldReturn201WithDto_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();
        CreateHobbyRequest request = CreateHobbyRequest.builder()
                .name("Chess").category(HobbyCategory.GAMING)
                .activityLevel(ActivityLevel.REGULAR).build();
        when(hobbyService.createHobby(any(CreateHobbyRequest.class))).thenReturn(buildHobbyDto());

        mockMvc.perform(post("/hobbies")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Chess"))
                .andExpect(jsonPath("$.category").value("GAMING"));
    }

    // ── POST /hobbies — validation ────────────────────────────────────────────

    @Test
    void createHobby_shouldReturn400_whenNameIsBlank() throws Exception {
        setupAdminAuth();
        CreateHobbyRequest request = CreateHobbyRequest.builder()
                .name("").category(HobbyCategory.GAMING).build();

        mockMvc.perform(post("/hobbies")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(hobbyService, never()).createHobby(any());
    }

    @Test
    void createHobby_shouldReturn400_whenCategoryIsNull() throws Exception {
        setupAdminAuth();
        CreateHobbyRequest request = CreateHobbyRequest.builder()
                .name("Chess").category(null).build();

        mockMvc.perform(post("/hobbies")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── POST /hobbies — security ──────────────────────────────────────────────

    @Test
    void createHobby_shouldReturn401_whenNotAuthenticated() throws Exception {
        CreateHobbyRequest request = CreateHobbyRequest.builder()
                .name("Chess").category(HobbyCategory.GAMING).build();

        mockMvc.perform(post("/hobbies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(hobbyService, never()).createHobby(any());
    }

    @Test
    void createHobby_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();
        CreateHobbyRequest request = CreateHobbyRequest.builder()
                .name("Chess").category(HobbyCategory.GAMING).build();

        mockMvc.perform(post("/hobbies")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(hobbyService, never()).createHobby(any());
    }

    // ── PUT /hobbies/{id} — happy path ────────────────────────────────────────

    @Test
    void updateHobby_shouldReturn200WithDto_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();
        UpdateHobbyRequest request = UpdateHobbyRequest.builder()
                .name("Advanced Chess").activityLevel(ActivityLevel.DAILY).build();
        when(hobbyService.updateHobby(eq(1L), any(UpdateHobbyRequest.class))).thenReturn(buildHobbyDto());

        mockMvc.perform(put("/hobbies/1")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Chess"));
    }

    @Test
    void updateHobby_shouldReturn404_whenHobbyNotFound() throws Exception {
        setupAdminAuth();
        UpdateHobbyRequest request = UpdateHobbyRequest.builder().name("X").build();
        when(hobbyService.updateHobby(eq(99L), any()))
                .thenThrow(new NoSuchElementException("Hobby not found"));

        mockMvc.perform(put("/hobbies/99")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Hobby not found"));
    }

    // ── PUT /hobbies/{id} — security ─────────────────────────────────────────

    @Test
    void updateHobby_shouldReturn401_whenNotAuthenticated() throws Exception {
        UpdateHobbyRequest request = UpdateHobbyRequest.builder().name("X").build();

        mockMvc.perform(put("/hobbies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(hobbyService, never()).updateHobby(any(), any());
    }

    @Test
    void updateHobby_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();
        UpdateHobbyRequest request = UpdateHobbyRequest.builder().name("X").build();

        mockMvc.perform(put("/hobbies/1")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ── DELETE /hobbies/{id} — happy path ─────────────────────────────────────

    @Test
    void deleteHobby_shouldReturn204_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();
        doNothing().when(hobbyService).deleteHobby(1L);

        mockMvc.perform(delete("/hobbies/1")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNoContent());

        verify(hobbyService).deleteHobby(1L);
    }

    @Test
    void deleteHobby_shouldReturn404_whenHobbyNotFound() throws Exception {
        setupAdminAuth();
        doThrow(new NoSuchElementException("Hobby not found")).when(hobbyService).deleteHobby(99L);

        mockMvc.perform(delete("/hobbies/99")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Hobby not found"));
    }

    // ── DELETE /hobbies/{id} — security ───────────────────────────────────────

    @Test
    void deleteHobby_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/hobbies/1"))
                .andExpect(status().isUnauthorized());

        verify(hobbyService, never()).deleteHobby(any());
    }

    @Test
    void deleteHobby_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();

        mockMvc.perform(delete("/hobbies/1")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden());
    }
}
