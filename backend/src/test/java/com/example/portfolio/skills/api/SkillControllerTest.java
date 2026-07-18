package com.example.portfolio.skills.api;

import com.example.portfolio.auth.application.JwtService;
import com.example.portfolio.auth.domain.User;
import com.example.portfolio.auth.domain.UserRole;
import com.example.portfolio.auth.persistence.UserRepository;
import com.example.portfolio.hobby.domain.ComplexityLevel;
import com.example.portfolio.projects.domain.ProjectStatus;
import com.example.portfolio.shared.config.SecurityConfig;
import com.example.portfolio.skills.application.SkillService;
import com.example.portfolio.skills.domain.ProficiencyLevel;
import com.example.portfolio.skills.dto.*;
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

@WebMvcTest(controllers = SkillController.class)
@Import(SecurityConfig.class)
@MockBean(JpaMetamodelMappingContext.class)
class SkillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SkillService skillService;

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

    private List<SkillListItemDto> buildSkillList() {
        return List.of(
                SkillListItemDto.builder()
                        .id(1L).name("Java").categoryName("Backend")
                        .proficiency(ProficiencyLevel.EXPERT).level(90)
                        .yearsOfExperience(new BigDecimal("5.0"))
                        .description("Primary language").hasCertification(true).learning(false)
                        .build(),
                SkillListItemDto.builder()
                        .id(2L).name("Spring").categoryName("Backend")
                        .proficiency(ProficiencyLevel.ADVANCED).level(80)
                        .yearsOfExperience(new BigDecimal("3.0"))
                        .hasCertification(false).learning(false)
                        .build()
        );
    }

    private SkillDetailDto buildSkillDetail() {
        return SkillDetailDto.builder()
                .id(1L).name("Java").categoryName("Backend")
                .proficiency(ProficiencyLevel.EXPERT).level(90)
                .yearsOfExperience(new BigDecimal("5.0"))
                .description("Primary language")
                .lastUsedDate(LocalDate.of(2024, 1, 1))
                .hasCertification(true).learning(false)
                .tags(List.of("jvm", "oop"))
                .projects(List.of(
                        ProjectInSkillDto.builder()
                                .id(10L).title("Portfolio API")
                                .status(ProjectStatus.PRODUCTION).complexity(ComplexityLevel.ADVANCED)
                                .year(2024).githubUrl("https://github.com/portfolio")
                                .build()
                ))
                .build();
    }

    // ── GET /skills ───────────────────────────────────────────────────────────

    @Test
    void getSkills_shouldReturn200WithSkillList() throws Exception {
        when(skillService.getSkillsList()).thenReturn(buildSkillList());

        mockMvc.perform(get("/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getSkills_shouldReturn200WithCorrectSkillFields() throws Exception {
        when(skillService.getSkillsList()).thenReturn(buildSkillList());

        mockMvc.perform(get("/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Java"))
                .andExpect(jsonPath("$[0].categoryName").value("Backend"))
                .andExpect(jsonPath("$[0].proficiency").value("EXPERT"))
                .andExpect(jsonPath("$[0].level").value(90))
                .andExpect(jsonPath("$[0].hasCertification").value(true));
    }

    @Test
    void getSkills_shouldReturn200WithEmptyList_whenNoSkillsExist() throws Exception {
        when(skillService.getSkillsList()).thenReturn(List.of());

        mockMvc.perform(get("/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getSkills_shouldReturn404_whenProfileNotFound() throws Exception {
        when(skillService.getSkillsList()).thenThrow(new NoSuchElementException("Profile not found"));

        mockMvc.perform(get("/skills"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Profile not found"));
    }

    @Test
    void getSkills_shouldBeAccessibleWithoutAuthentication() throws Exception {
        when(skillService.getSkillsList()).thenReturn(List.of());

        mockMvc.perform(get("/skills"))
                .andExpect(status().isOk());

        verify(jwtService, never()).extractEmail(any());
    }

    // ── GET /skills/{id} ──────────────────────────────────────────────────────

    @Test
    void getSkill_shouldReturn200WithFullDetail() throws Exception {
        when(skillService.getSkillById(1L)).thenReturn(buildSkillDetail());

        mockMvc.perform(get("/skills/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Java"))
                .andExpect(jsonPath("$.categoryName").value("Backend"))
                .andExpect(jsonPath("$.proficiency").value("EXPERT"))
                .andExpect(jsonPath("$.level").value(90))
                .andExpect(jsonPath("$.description").value("Primary language"))
                .andExpect(jsonPath("$.hasCertification").value(true))
                .andExpect(jsonPath("$.learning").value(false));
    }

    @Test
    void getSkill_shouldReturn200WithTagsAndProjects() throws Exception {
        when(skillService.getSkillById(1L)).thenReturn(buildSkillDetail());

        mockMvc.perform(get("/skills/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags.length()").value(2))
                .andExpect(jsonPath("$.tags[0]").value("jvm"))
                .andExpect(jsonPath("$.tags[1]").value("oop"))
                .andExpect(jsonPath("$.projects").isArray())
                .andExpect(jsonPath("$.projects.length()").value(1))
                .andExpect(jsonPath("$.projects[0].title").value("Portfolio API"))
                .andExpect(jsonPath("$.projects[0].status").value("PRODUCTION"))
                .andExpect(jsonPath("$.projects[0].year").value(2024));
    }

    @Test
    void getSkill_shouldReturn200WithEmptyTagsAndProjects_whenNoneExist() throws Exception {
        SkillDetailDto emptyDetail = SkillDetailDto.builder()
                .id(1L).name("Java").proficiency(ProficiencyLevel.EXPERT)
                .tags(List.of()).projects(List.of())
                .build();
        when(skillService.getSkillById(1L)).thenReturn(emptyDetail);

        mockMvc.perform(get("/skills/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags").isEmpty())
                .andExpect(jsonPath("$.projects").isArray())
                .andExpect(jsonPath("$.projects").isEmpty());
    }

    @Test
    void getSkill_shouldReturn404_whenSkillNotFound() throws Exception {
        when(skillService.getSkillById(99L)).thenThrow(new NoSuchElementException("Skill not found"));

        mockMvc.perform(get("/skills/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Skill not found"));
    }

    @Test
    void getSkill_shouldBeAccessibleWithoutAuthentication() throws Exception {
        when(skillService.getSkillById(1L)).thenReturn(buildSkillDetail());

        mockMvc.perform(get("/skills/1"))
                .andExpect(status().isOk());

        verify(jwtService, never()).extractEmail(any());
    }

    // ── POST /skills ──────────────────────────────────────────────────────────

    @Test
    void createSkill_shouldReturn201_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();
        CreateSkillRequest request = new CreateSkillRequest();
        request.setName("Kotlin");
        when(skillService.createSkill(any(CreateSkillRequest.class))).thenReturn(buildSkillDetail());

        mockMvc.perform(post("/skills")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Java"));
    }

    @Test
    void createSkill_shouldReturn400_whenNameIsBlank() throws Exception {
        setupAdminAuth();
        CreateSkillRequest request = new CreateSkillRequest();
        request.setName("");

        mockMvc.perform(post("/skills")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(skillService, never()).createSkill(any());
    }

    @Test
    void createSkill_shouldReturn401_whenNotAuthenticated() throws Exception {
        CreateSkillRequest request = new CreateSkillRequest();
        request.setName("Kotlin");

        mockMvc.perform(post("/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(skillService, never()).createSkill(any());
    }

    @Test
    void createSkill_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();
        CreateSkillRequest request = new CreateSkillRequest();
        request.setName("Kotlin");

        mockMvc.perform(post("/skills")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(skillService, never()).createSkill(any());
    }

    // ── PUT /skills/{id} ──────────────────────────────────────────────────────

    @Test
    void updateSkill_shouldReturn200_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();
        UpdateSkillRequest request = new UpdateSkillRequest();
        request.setLevel(75);
        when(skillService.updateSkill(eq(1L), any(UpdateSkillRequest.class))).thenReturn(buildSkillDetail());

        mockMvc.perform(put("/skills/1")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Java"));
    }

    @Test
    void updateSkill_shouldReturn404_whenSkillNotFound() throws Exception {
        setupAdminAuth();
        UpdateSkillRequest request = new UpdateSkillRequest();
        when(skillService.updateSkill(eq(99L), any())).thenThrow(new NoSuchElementException("Skill not found"));

        mockMvc.perform(put("/skills/99")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Skill not found"));
    }

    @Test
    void updateSkill_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();
        UpdateSkillRequest request = new UpdateSkillRequest();

        mockMvc.perform(put("/skills/1")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(skillService, never()).updateSkill(any(), any());
    }

    // ── DELETE /skills/{id} ───────────────────────────────────────────────────

    @Test
    void deleteSkill_shouldReturn204_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();

        mockMvc.perform(delete("/skills/1")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNoContent());

        verify(skillService).deleteSkill(1L);
    }

    @Test
    void deleteSkill_shouldReturn404_whenSkillNotFound() throws Exception {
        setupAdminAuth();
        doThrow(new NoSuchElementException("Skill not found")).when(skillService).deleteSkill(99L);

        mockMvc.perform(delete("/skills/99")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Skill not found"));
    }

    @Test
    void deleteSkill_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/skills/1"))
                .andExpect(status().isUnauthorized());

        verify(skillService, never()).deleteSkill(any());
    }

    @Test
    void deleteSkill_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();

        mockMvc.perform(delete("/skills/1")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden());

        verify(skillService, never()).deleteSkill(any());
    }

    // ── GET /skills/categories ────────────────────────────────────────────────

    @Test
    void getCategories_shouldReturn200WithList_andBeAccessibleWithoutAuth() throws Exception {
        when(skillService.getAllCategories()).thenReturn(List.of(
                SkillCategoryDto.builder().id(1L).name("Backend").build(),
                SkillCategoryDto.builder().id(2L).name("Frontend").build()
        ));

        mockMvc.perform(get("/skills/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Backend"));

        verify(jwtService, never()).extractEmail(any());
    }

    @Test
    void getCategory_shouldReturn404_whenNotFound() throws Exception {
        when(skillService.getCategory(99L)).thenThrow(new NoSuchElementException("Skill category not found"));

        mockMvc.perform(get("/skills/categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Skill category not found"));
    }

    @Test
    void getCategory_routeShouldNotBeShadowedByIdRoute() throws Exception {
        when(skillService.getAllCategories()).thenReturn(List.of());

        // Regression check: "/skills/categories" must resolve to the categories list endpoint,
        // not attempt to bind "categories" as the {id} path variable on GET /skills/{id}.
        mockMvc.perform(get("/skills/categories"))
                .andExpect(status().isOk());

        verify(skillService, never()).getSkillById(any());
    }

    // ── POST /skills/categories ───────────────────────────────────────────────

    @Test
    void createCategory_shouldReturn201_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();
        CreateSkillCategoryRequest request = new CreateSkillCategoryRequest();
        request.setName("DevOps");
        when(skillService.createCategory(any(CreateSkillCategoryRequest.class)))
                .thenReturn(SkillCategoryDto.builder().id(3L).name("DevOps").build());

        mockMvc.perform(post("/skills/categories")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("DevOps"));
    }

    @Test
    void createCategory_shouldReturn400_whenNameIsBlank() throws Exception {
        setupAdminAuth();
        CreateSkillCategoryRequest request = new CreateSkillCategoryRequest();
        request.setName("  ");

        mockMvc.perform(post("/skills/categories")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(skillService, never()).createCategory(any());
    }

    @Test
    void createCategory_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();
        CreateSkillCategoryRequest request = new CreateSkillCategoryRequest();
        request.setName("DevOps");

        mockMvc.perform(post("/skills/categories")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(skillService, never()).createCategory(any());
    }

    // ── PUT /skills/categories/{id} ───────────────────────────────────────────

    @Test
    void updateCategory_shouldReturn200_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();
        UpdateSkillCategoryRequest request = new UpdateSkillCategoryRequest();
        request.setName("Backend Renamed");
        when(skillService.updateCategory(eq(1L), any(UpdateSkillCategoryRequest.class)))
                .thenReturn(SkillCategoryDto.builder().id(1L).name("Backend Renamed").build());

        mockMvc.perform(put("/skills/categories/1")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Backend Renamed"));
    }

    // ── DELETE /skills/categories/{id} ────────────────────────────────────────

    @Test
    void deleteCategory_shouldReturn204_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();

        mockMvc.perform(delete("/skills/categories/1")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNoContent());

        verify(skillService).deleteCategory(1L);
    }

    @Test
    void deleteCategory_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();

        mockMvc.perform(delete("/skills/categories/1")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden());

        verify(skillService, never()).deleteCategory(any());
    }
}
