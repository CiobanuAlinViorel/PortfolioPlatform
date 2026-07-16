package com.example.portfolio.achievement.api;

import com.example.portfolio.achievement.application.AchievementService;
import com.example.portfolio.achievement.domain.AchievementType;
import com.example.portfolio.achievement.domain.RecognitionLevel;
import com.example.portfolio.achievement.dto.AchievementDto;
import com.example.portfolio.achievement.dto.CreateAchievementRequest;
import com.example.portfolio.achievement.dto.UpdateAchievementRequest;
import com.example.portfolio.auth.application.JwtService;
import com.example.portfolio.auth.domain.User;
import com.example.portfolio.auth.domain.UserRole;
import com.example.portfolio.auth.persistence.UserRepository;
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

@WebMvcTest(controllers = AchievementController.class)
@Import(SecurityConfig.class)
@MockBean(JpaMetamodelMappingContext.class)
class AchievementControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AchievementService achievementService;
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

    private AchievementDto buildAchievementDto() {
        return AchievementDto.builder()
                .id(1L).title("Best Developer Award")
                .description("Awarded for outstanding performance")
                .achievementType(AchievementType.AWARD)
                .achievementDate(LocalDate.of(2024, 3, 15))
                .recognitionLevel(RecognitionLevel.NATIONAL)
                .recognizedBy("Tech Corp").isFeatured(true).sortOrder(1)
                .build();
    }

    // ── GET /achievements — happy path ────────────────────────────────────────

    @Test
    void getAchievements_shouldReturn200WithList_withNoFilters() throws Exception {
        when(achievementService.getAchievements(null, null, null)).thenReturn(List.of(buildAchievementDto()));

        mockMvc.perform(get("/achievements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Best Developer Award"))
                .andExpect(jsonPath("$[0].achievementType").value("AWARD"))
                .andExpect(jsonPath("$[0].recognitionLevel").value("NATIONAL"))
                .andExpect(jsonPath("$[0].isFeatured").value(true));
    }

    @Test
    void getAchievements_shouldReturn200_withTypeFilter() throws Exception {
        when(achievementService.getAchievements(AchievementType.AWARD, null, null))
                .thenReturn(List.of(buildAchievementDto()));

        mockMvc.perform(get("/achievements").param("type", "AWARD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(achievementService).getAchievements(AchievementType.AWARD, null, null);
    }

    @Test
    void getAchievements_shouldReturn200_withRecognitionLevelFilter() throws Exception {
        when(achievementService.getAchievements(null, RecognitionLevel.NATIONAL, null))
                .thenReturn(List.of(buildAchievementDto()));

        mockMvc.perform(get("/achievements").param("recognitionLevel", "NATIONAL"))
                .andExpect(status().isOk());

        verify(achievementService).getAchievements(null, RecognitionLevel.NATIONAL, null);
    }

    @Test
    void getAchievements_shouldReturn200_withIsFeaturedFilter() throws Exception {
        when(achievementService.getAchievements(null, null, true))
                .thenReturn(List.of(buildAchievementDto()));

        mockMvc.perform(get("/achievements").param("isFeatured", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isFeatured").value(true));

        verify(achievementService).getAchievements(null, null, true);
    }

    @Test
    void getAchievements_shouldReturn200_withAllFilters() throws Exception {
        when(achievementService.getAchievements(AchievementType.AWARD, RecognitionLevel.NATIONAL, true))
                .thenReturn(List.of(buildAchievementDto()));

        mockMvc.perform(get("/achievements")
                        .param("type", "AWARD")
                        .param("recognitionLevel", "NATIONAL")
                        .param("isFeatured", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(achievementService).getAchievements(AchievementType.AWARD, RecognitionLevel.NATIONAL, true);
    }

    @Test
    void getAchievements_shouldReturn200WithEmptyList_whenNoneExist() throws Exception {
        when(achievementService.getAchievements(null, null, null)).thenReturn(List.of());

        mockMvc.perform(get("/achievements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getAchievements_shouldBeAccessibleWithoutAuthentication() throws Exception {
        when(achievementService.getAchievements(null, null, null)).thenReturn(List.of());

        mockMvc.perform(get("/achievements"))
                .andExpect(status().isOk());

        verify(jwtService, never()).extractEmail(any());
    }

    // ── GET /achievements/{id} ────────────────────────────────────────────────

    @Test
    void getAchievement_shouldReturn200WithDto_whenFound() throws Exception {
        when(achievementService.getAchievementById(1L)).thenReturn(buildAchievementDto());

        mockMvc.perform(get("/achievements/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Best Developer Award"))
                .andExpect(jsonPath("$.recognizedBy").value("Tech Corp"));
    }

    @Test
    void getAchievement_shouldReturn404_whenNotFound() throws Exception {
        when(achievementService.getAchievementById(99L))
                .thenThrow(new NoSuchElementException("Achievement not found"));

        mockMvc.perform(get("/achievements/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Achievement not found"));
    }

    @Test
    void getAchievement_shouldBeAccessibleWithoutAuthentication() throws Exception {
        when(achievementService.getAchievementById(1L)).thenReturn(buildAchievementDto());

        mockMvc.perform(get("/achievements/1"))
                .andExpect(status().isOk());

        verify(jwtService, never()).extractEmail(any());
    }

    // ── POST /achievements — happy path ───────────────────────────────────────

    @Test
    void createAchievement_shouldReturn201WithDto_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();
        CreateAchievementRequest request = CreateAchievementRequest.builder()
                .title("Best Developer Award").description("Outstanding performance")
                .achievementType(AchievementType.AWARD).achievementDate(LocalDate.of(2024, 3, 15))
                .recognitionLevel(RecognitionLevel.NATIONAL).isFeatured(true).build();
        when(achievementService.createAchievement(any(CreateAchievementRequest.class)))
                .thenReturn(buildAchievementDto());

        mockMvc.perform(post("/achievements")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Best Developer Award"))
                .andExpect(jsonPath("$.achievementType").value("AWARD"));
    }

    // ── POST /achievements — validation ───────────────────────────────────────

    @Test
    void createAchievement_shouldReturn400_whenTitleIsBlank() throws Exception {
        setupAdminAuth();
        CreateAchievementRequest request = CreateAchievementRequest.builder()
                .title("").description("Desc")
                .achievementType(AchievementType.AWARD).achievementDate(LocalDate.now()).build();

        mockMvc.perform(post("/achievements")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(achievementService, never()).createAchievement(any());
    }

    @Test
    void createAchievement_shouldReturn400_whenDescriptionIsBlank() throws Exception {
        setupAdminAuth();
        CreateAchievementRequest request = CreateAchievementRequest.builder()
                .title("Award").description("")
                .achievementType(AchievementType.AWARD).achievementDate(LocalDate.now()).build();

        mockMvc.perform(post("/achievements")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAchievement_shouldReturn400_whenTypeIsNull() throws Exception {
        setupAdminAuth();
        CreateAchievementRequest request = CreateAchievementRequest.builder()
                .title("Award").description("Desc")
                .achievementType(null).achievementDate(LocalDate.now()).build();

        mockMvc.perform(post("/achievements")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAchievement_shouldReturn400_whenDateIsNull() throws Exception {
        setupAdminAuth();
        CreateAchievementRequest request = CreateAchievementRequest.builder()
                .title("Award").description("Desc")
                .achievementType(AchievementType.AWARD).achievementDate(null).build();

        mockMvc.perform(post("/achievements")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── POST /achievements — security ─────────────────────────────────────────

    @Test
    void createAchievement_shouldReturn401_whenNotAuthenticated() throws Exception {
        CreateAchievementRequest request = CreateAchievementRequest.builder()
                .title("Award").description("Desc")
                .achievementType(AchievementType.AWARD).achievementDate(LocalDate.now()).build();

        mockMvc.perform(post("/achievements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(achievementService, never()).createAchievement(any());
    }

    @Test
    void createAchievement_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();
        CreateAchievementRequest request = CreateAchievementRequest.builder()
                .title("Award").description("Desc")
                .achievementType(AchievementType.AWARD).achievementDate(LocalDate.now()).build();

        mockMvc.perform(post("/achievements")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(achievementService, never()).createAchievement(any());
    }

    // ── PUT /achievements/{id} ────────────────────────────────────────────────

    @Test
    void updateAchievement_shouldReturn200WithDto_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();
        UpdateAchievementRequest request = UpdateAchievementRequest.builder()
                .title("Updated Title").isFeatured(false).build();
        when(achievementService.updateAchievement(eq(1L), any(UpdateAchievementRequest.class)))
                .thenReturn(buildAchievementDto());

        mockMvc.perform(put("/achievements/1")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Best Developer Award"));
    }

    @Test
    void updateAchievement_shouldReturn404_whenNotFound() throws Exception {
        setupAdminAuth();
        UpdateAchievementRequest request = UpdateAchievementRequest.builder().title("X").build();
        when(achievementService.updateAchievement(eq(99L), any()))
                .thenThrow(new NoSuchElementException("Achievement not found"));

        mockMvc.perform(put("/achievements/99")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Achievement not found"));
    }

    @Test
    void updateAchievement_shouldReturn401_whenNotAuthenticated() throws Exception {
        UpdateAchievementRequest request = UpdateAchievementRequest.builder().title("X").build();

        mockMvc.perform(put("/achievements/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(achievementService, never()).updateAchievement(any(), any());
    }

    @Test
    void updateAchievement_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();
        UpdateAchievementRequest request = UpdateAchievementRequest.builder().title("X").build();

        mockMvc.perform(put("/achievements/1")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ── DELETE /achievements/{id} ─────────────────────────────────────────────

    @Test
    void deleteAchievement_shouldReturn204_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();
        doNothing().when(achievementService).deleteAchievement(1L);

        mockMvc.perform(delete("/achievements/1")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNoContent());

        verify(achievementService).deleteAchievement(1L);
    }

    @Test
    void deleteAchievement_shouldReturn404_whenNotFound() throws Exception {
        setupAdminAuth();
        doThrow(new NoSuchElementException("Achievement not found"))
                .when(achievementService).deleteAchievement(99L);

        mockMvc.perform(delete("/achievements/99")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Achievement not found"));
    }

    @Test
    void deleteAchievement_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/achievements/1"))
                .andExpect(status().isUnauthorized());

        verify(achievementService, never()).deleteAchievement(any());
    }

    @Test
    void deleteAchievement_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();

        mockMvc.perform(delete("/achievements/1")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden());
    }
}
