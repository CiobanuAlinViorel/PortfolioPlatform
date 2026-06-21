package com.example.portfolio.profile.api;

import com.example.portfolio.auth.application.JwtService;
import com.example.portfolio.auth.domain.User;
import com.example.portfolio.auth.domain.UserRole;
import com.example.portfolio.auth.persistence.UserRepository;
import com.example.portfolio.profile.application.ProfileService;
import com.example.portfolio.profile.dto.*;
import com.example.portfolio.projects.domain.ProjectStatus;
import com.example.portfolio.shared.config.SecurityConfig;
import com.example.portfolio.skills.domain.ProficiencyLevel;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProfileController.class)
@Import(SecurityConfig.class)
@MockBean(JpaMetamodelMappingContext.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProfileService profileService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    private ProfileSummaryResponse buildFullResponse() {
        return ProfileSummaryResponse.builder()
                .profile(ProfileInfoDto.builder()
                        .id(1L).firstName("John").lastName("Doe").age(30).description("Developer").build())
                .contact(ContactInfoDto.builder()
                        .email("john@test.com").github("john-gh").linkedin("john-li").city("Bucharest").country("Romania").build())
                .topSkills(List.of(
                        SkillSummaryDto.builder().id(1L).name("Java").categoryName("Backend").proficiency(ProficiencyLevel.EXPERT).level(90).build(),
                        SkillSummaryDto.builder().id(2L).name("Spring").categoryName("Backend").proficiency(ProficiencyLevel.ADVANCED).level(80).build()
                ))
                .topProjects(List.of(
                        ProjectSummaryDto.builder().id(1L).title("Portfolio API").status(ProjectStatus.PRODUCTION).year(2024).build()
                ))
                .lastExperience(LastExperienceDto.builder()
                        .companyName("Acme Corp").role("Backend Developer")
                        .startDate(LocalDate.of(2022, 1, 1)).endDate(LocalDate.of(2024, 6, 1))
                        .projectsCount(3).build())
                .build();
    }

    // ── GET /profile/summary — happy path ─────────────────────────────────────

    @Test
    void getSummary_shouldReturn200WithCorrectProfileInfo() throws Exception {
        when(profileService.getProfileSummary()).thenReturn(buildFullResponse());

        mockMvc.perform(get("/profile/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.firstName").value("John"))
                .andExpect(jsonPath("$.profile.lastName").value("Doe"))
                .andExpect(jsonPath("$.profile.age").value(30))
                .andExpect(jsonPath("$.profile.description").value("Developer"));
    }

    @Test
    void getSummary_shouldReturn200WithCorrectContactInfo() throws Exception {
        when(profileService.getProfileSummary()).thenReturn(buildFullResponse());

        mockMvc.perform(get("/profile/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contact.email").value("john@test.com"))
                .andExpect(jsonPath("$.contact.github").value("john-gh"))
                .andExpect(jsonPath("$.contact.city").value("Bucharest"))
                .andExpect(jsonPath("$.contact.country").value("Romania"));
    }

    @Test
    void getSummary_shouldReturn200WithCorrectTopSkills() throws Exception {
        when(profileService.getProfileSummary()).thenReturn(buildFullResponse());

        mockMvc.perform(get("/profile/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topSkills").isArray())
                .andExpect(jsonPath("$.topSkills.length()").value(2))
                .andExpect(jsonPath("$.topSkills[0].name").value("Java"))
                .andExpect(jsonPath("$.topSkills[0].categoryName").value("Backend"))
                .andExpect(jsonPath("$.topSkills[0].proficiency").value("EXPERT"))
                .andExpect(jsonPath("$.topSkills[1].name").value("Spring"));
    }

    @Test
    void getSummary_shouldReturn200WithCorrectTopProjects() throws Exception {
        when(profileService.getProfileSummary()).thenReturn(buildFullResponse());

        mockMvc.perform(get("/profile/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topProjects").isArray())
                .andExpect(jsonPath("$.topProjects.length()").value(1))
                .andExpect(jsonPath("$.topProjects[0].title").value("Portfolio API"))
                .andExpect(jsonPath("$.topProjects[0].status").value("PRODUCTION"))
                .andExpect(jsonPath("$.topProjects[0].year").value(2024));
    }

    @Test
    void getSummary_shouldReturn200WithCorrectLastExperience() throws Exception {
        when(profileService.getProfileSummary()).thenReturn(buildFullResponse());

        mockMvc.perform(get("/profile/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastExperience.companyName").value("Acme Corp"))
                .andExpect(jsonPath("$.lastExperience.role").value("Backend Developer"))
                .andExpect(jsonPath("$.lastExperience.projectsCount").value(3));
    }

    // ── GET /profile/summary — edge cases ─────────────────────────────────────

    @Test
    void getSummary_shouldReturn200WithNullContact_whenProfileHasNoContactInfo() throws Exception {
        ProfileSummaryResponse responseWithoutContact = ProfileSummaryResponse.builder()
                .profile(ProfileInfoDto.builder().firstName("John").lastName("Doe").build())
                .contact(null)
                .topSkills(List.of())
                .topProjects(List.of())
                .lastExperience(null)
                .build();

        when(profileService.getProfileSummary()).thenReturn(responseWithoutContact);

        mockMvc.perform(get("/profile/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contact").doesNotExist())
                .andExpect(jsonPath("$.lastExperience").doesNotExist());
    }

    @Test
    void getSummary_shouldReturn200WithEmptyLists_whenNoFeaturedSkillsOrProjects() throws Exception {
        ProfileSummaryResponse emptyResponse = ProfileSummaryResponse.builder()
                .profile(ProfileInfoDto.builder().firstName("John").lastName("Doe").build())
                .topSkills(List.of())
                .topProjects(List.of())
                .build();

        when(profileService.getProfileSummary()).thenReturn(emptyResponse);

        mockMvc.perform(get("/profile/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topSkills").isArray())
                .andExpect(jsonPath("$.topSkills").isEmpty())
                .andExpect(jsonPath("$.topProjects").isArray())
                .andExpect(jsonPath("$.topProjects").isEmpty());
    }

    // ── GET /profile/summary — error cases ────────────────────────────────────

    @Test
    void getSummary_shouldReturn404_whenProfileDoesNotExist() throws Exception {
        when(profileService.getProfileSummary())
                .thenThrow(new NoSuchElementException("Profile not found"));

        mockMvc.perform(get("/profile/summary"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Profile not found"));
    }

    // ── authentication ────────────────────────────────────────────────────────

    @Test
    void getSummary_shouldBeAccessibleWithoutAuthorizationHeader() throws Exception {
        when(profileService.getProfileSummary()).thenReturn(buildFullResponse());

        mockMvc.perform(get("/profile/summary"))
                .andExpect(status().isOk());

        verify(jwtService, never()).extractEmail(any());
    }

    // ── auth helpers ──────────────────────────────────────────────────────────

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

    private ProfileInfoDto buildProfileInfoDto() {
        return ProfileInfoDto.builder().id(1L).firstName("John").lastName("Doe").age(30).description("Developer").build();
    }

    private ContactInfoDto buildContactInfoDto() {
        return ContactInfoDto.builder().email("john@test.com").github("john-gh").city("Bucharest").country("Romania").build();
    }

    // ── POST /profile — happy path ────────────────────────────────────────────

    @Test
    void createProfile_shouldReturn201WithProfileInfo_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();
        CreateProfileRequest request = CreateProfileRequest.builder()
                .firstName("John").lastName("Doe").age(30).description("Developer").build();
        when(profileService.createProfile(any(CreateProfileRequest.class))).thenReturn(buildProfileInfoDto());

        mockMvc.perform(post("/profile")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.age").value(30));
    }

    // ── POST /profile — validation ────────────────────────────────────────────

    @Test
    void createProfile_shouldReturn400_whenFirstNameIsBlank() throws Exception {
        setupAdminAuth();
        CreateProfileRequest request = CreateProfileRequest.builder()
                .firstName("").lastName("Doe").build();

        mockMvc.perform(post("/profile")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProfile_shouldReturn400_whenLastNameIsBlank() throws Exception {
        setupAdminAuth();
        CreateProfileRequest request = CreateProfileRequest.builder()
                .firstName("John").lastName("").build();

        mockMvc.perform(post("/profile")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProfile_shouldReturn400_whenProfileAlreadyExists() throws Exception {
        setupAdminAuth();
        CreateProfileRequest request = CreateProfileRequest.builder()
                .firstName("John").lastName("Doe").build();
        when(profileService.createProfile(any())).thenThrow(new IllegalArgumentException("A profile already exists"));

        mockMvc.perform(post("/profile")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A profile already exists"));
    }

    // ── POST /profile — security ──────────────────────────────────────────────

    @Test
    void createProfile_shouldReturn401_whenNotAuthenticated() throws Exception {
        CreateProfileRequest request = CreateProfileRequest.builder()
                .firstName("John").lastName("Doe").build();

        mockMvc.perform(post("/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(profileService, never()).createProfile(any());
    }

    @Test
    void createProfile_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();
        CreateProfileRequest request = CreateProfileRequest.builder()
                .firstName("John").lastName("Doe").build();

        mockMvc.perform(post("/profile")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(profileService, never()).createProfile(any());
    }

    // ── PUT /profile — happy path ─────────────────────────────────────────────

    @Test
    void updateProfile_shouldReturn200WithUpdatedInfo_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstName("Updated").age(35).build();
        when(profileService.updateProfile(any(UpdateProfileRequest.class))).thenReturn(buildProfileInfoDto());

        mockMvc.perform(put("/profile")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void updateProfile_shouldReturn404_whenProfileNotFound() throws Exception {
        setupAdminAuth();
        UpdateProfileRequest request = UpdateProfileRequest.builder().firstName("Updated").build();
        when(profileService.updateProfile(any())).thenThrow(new NoSuchElementException("Profile not found"));

        mockMvc.perform(put("/profile")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Profile not found"));
    }

    // ── PUT /profile — security ───────────────────────────────────────────────

    @Test
    void updateProfile_shouldReturn401_whenNotAuthenticated() throws Exception {
        UpdateProfileRequest request = UpdateProfileRequest.builder().firstName("Updated").build();

        mockMvc.perform(put("/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(profileService, never()).updateProfile(any());
    }

    @Test
    void updateProfile_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();
        UpdateProfileRequest request = UpdateProfileRequest.builder().firstName("Updated").build();

        mockMvc.perform(put("/profile")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(profileService, never()).updateProfile(any());
    }

    // ── PUT /profile/contact — happy path ─────────────────────────────────────

    @Test
    void upsertContactInfo_shouldReturn200WithContactInfo_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();
        UpsertContactInfoRequest request = UpsertContactInfoRequest.builder()
                .email("john@test.com").github("john-gh").city("Bucharest").country("Romania").build();
        when(profileService.upsertContactInfo(any(UpsertContactInfoRequest.class))).thenReturn(buildContactInfoDto());

        mockMvc.perform(put("/profile/contact")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@test.com"))
                .andExpect(jsonPath("$.github").value("john-gh"))
                .andExpect(jsonPath("$.city").value("Bucharest"))
                .andExpect(jsonPath("$.country").value("Romania"));
    }

    @Test
    void upsertContactInfo_shouldReturn404_whenProfileNotFound() throws Exception {
        setupAdminAuth();
        UpsertContactInfoRequest request = UpsertContactInfoRequest.builder().email("j@test.com").build();
        when(profileService.upsertContactInfo(any())).thenThrow(new NoSuchElementException("Profile not found"));

        mockMvc.perform(put("/profile/contact")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ── PUT /profile/contact — security ───────────────────────────────────────

    @Test
    void upsertContactInfo_shouldReturn401_whenNotAuthenticated() throws Exception {
        UpsertContactInfoRequest request = UpsertContactInfoRequest.builder().email("j@test.com").build();

        mockMvc.perform(put("/profile/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(profileService, never()).upsertContactInfo(any());
    }

    @Test
    void upsertContactInfo_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();
        UpsertContactInfoRequest request = UpsertContactInfoRequest.builder().email("j@test.com").build();

        mockMvc.perform(put("/profile/contact")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(profileService, never()).upsertContactInfo(any());
    }
}
