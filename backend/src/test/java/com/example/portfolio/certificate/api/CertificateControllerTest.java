package com.example.portfolio.certificate.api;

import com.example.portfolio.auth.application.JwtService;
import com.example.portfolio.auth.domain.User;
import com.example.portfolio.auth.domain.UserRole;
import com.example.portfolio.auth.persistence.UserRepository;
import com.example.portfolio.certificate.application.CertificateService;
import com.example.portfolio.certificate.dto.*;
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

@WebMvcTest(controllers = CertificateController.class)
@Import(SecurityConfig.class)
@MockBean(JpaMetamodelMappingContext.class)
class CertificateControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CertificateService certificateService;
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

    private CertificateDto buildCertificateDto() {
        return CertificateDto.builder()
                .id(1L).name("AWS Certified Developer").provider("Amazon")
                .issueDate(LocalDate.of(2024, 1, 15))
                .categoryId(1L).categoryName("Cloud")
                .hasExpiry(false).verified(false).relevanceScore(80)
                .build();
    }

    private CertificationCategoryDto buildCategoryDto() {
        return CertificationCategoryDto.builder()
                .id(1L).name("Cloud").description("Cloud certs").industry("IT").sortOrder(1)
                .build();
    }

    // ── GET /certificates — happy path ────────────────────────────────────────

    @Test
    void getAllCertificates_shouldReturn200WithList_withNoFilters() throws Exception {
        when(certificateService.getAllCertificates(null, null, null))
                .thenReturn(List.of(buildCertificateDto()));

        mockMvc.perform(get("/certificates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("AWS Certified Developer"))
                .andExpect(jsonPath("$[0].provider").value("Amazon"))
                .andExpect(jsonPath("$[0].categoryName").value("Cloud"));
    }

    @Test
    void getAllCertificates_shouldReturn200_withCategoryIdFilter() throws Exception {
        when(certificateService.getAllCertificates(1L, null, null))
                .thenReturn(List.of(buildCertificateDto()));

        mockMvc.perform(get("/certificates").param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(certificateService).getAllCertificates(1L, null, null);
    }

    @Test
    void getAllCertificates_shouldReturn200_withProviderFilter() throws Exception {
        when(certificateService.getAllCertificates(null, "Amazon", null))
                .thenReturn(List.of(buildCertificateDto()));

        mockMvc.perform(get("/certificates").param("provider", "Amazon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].provider").value("Amazon"));

        verify(certificateService).getAllCertificates(null, "Amazon", null);
    }

    @Test
    void getAllCertificates_shouldReturn200_withVerifiedFilter() throws Exception {
        when(certificateService.getAllCertificates(null, null, true))
                .thenReturn(List.of(buildCertificateDto()));

        mockMvc.perform(get("/certificates").param("verified", "true"))
                .andExpect(status().isOk());

        verify(certificateService).getAllCertificates(null, null, true);
    }

    @Test
    void getAllCertificates_shouldReturn200WithEmptyList_whenNoCertificatesExist() throws Exception {
        when(certificateService.getAllCertificates(null, null, null)).thenReturn(List.of());

        mockMvc.perform(get("/certificates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getAllCertificates_shouldBeAccessibleWithoutAuthentication() throws Exception {
        when(certificateService.getAllCertificates(null, null, null)).thenReturn(List.of());

        mockMvc.perform(get("/certificates"))
                .andExpect(status().isOk());

        verify(jwtService, never()).extractEmail(any());
    }

    // ── GET /certificates/{id} — happy path ───────────────────────────────────

    @Test
    void getCertificate_shouldReturn200WithDto_whenFound() throws Exception {
        when(certificateService.getCertificate(1L)).thenReturn(buildCertificateDto());

        mockMvc.perform(get("/certificates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("AWS Certified Developer"))
                .andExpect(jsonPath("$.categoryName").value("Cloud"));
    }

    @Test
    void getCertificate_shouldReturn404_whenNotFound() throws Exception {
        when(certificateService.getCertificate(99L))
                .thenThrow(new NoSuchElementException("Certificate not found"));

        mockMvc.perform(get("/certificates/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Certificate not found"));
    }

    @Test
    void getCertificate_shouldBeAccessibleWithoutAuthentication() throws Exception {
        when(certificateService.getCertificate(1L)).thenReturn(buildCertificateDto());

        mockMvc.perform(get("/certificates/1"))
                .andExpect(status().isOk());

        verify(jwtService, never()).extractEmail(any());
    }

    // ── POST /certificates — happy path ───────────────────────────────────────

    @Test
    void createCertificate_shouldReturn201WithDto_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();
        CreateCertificateRequest request = CreateCertificateRequest.builder()
                .name("AWS Certified Developer").provider("Amazon")
                .issueDate(LocalDate.of(2024, 1, 15)).categoryId(1L).build();
        when(certificateService.createCertificate(any(CreateCertificateRequest.class)))
                .thenReturn(buildCertificateDto());

        mockMvc.perform(post("/certificates")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("AWS Certified Developer"))
                .andExpect(jsonPath("$.provider").value("Amazon"));
    }

    @Test
    void createCertificate_shouldReturn201_whenOnlyCategoryNameProvided() throws Exception {
        setupAdminAuth();
        CreateCertificateRequest request = CreateCertificateRequest.builder()
                .name("AWS Certified Developer").provider("Amazon")
                .issueDate(LocalDate.of(2024, 1, 15)).categoryName("Cloud").build();
        when(certificateService.createCertificate(any(CreateCertificateRequest.class)))
                .thenReturn(buildCertificateDto());

        mockMvc.perform(post("/certificates")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("AWS Certified Developer"));
    }

    // ── POST /certificates — validation ───────────────────────────────────────

    @Test
    void createCertificate_shouldReturn400_whenNeitherCategoryIdNorNameProvided() throws Exception {
        setupAdminAuth();
        CreateCertificateRequest request = CreateCertificateRequest.builder()
                .name("AWS").provider("Amazon").issueDate(LocalDate.now()).build();
        when(certificateService.createCertificate(any()))
                .thenThrow(new IllegalArgumentException("Either categoryId or categoryName must be provided"));

        mockMvc.perform(post("/certificates")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Either categoryId or categoryName must be provided"));
    }


    @Test
    void createCertificate_shouldReturn400_whenNameIsBlank() throws Exception {
        setupAdminAuth();
        CreateCertificateRequest request = CreateCertificateRequest.builder()
                .name("").provider("Amazon").issueDate(LocalDate.now()).categoryId(1L).build();

        mockMvc.perform(post("/certificates")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(certificateService, never()).createCertificate(any());
    }

    @Test
    void createCertificate_shouldReturn400_whenProviderIsBlank() throws Exception {
        setupAdminAuth();
        CreateCertificateRequest request = CreateCertificateRequest.builder()
                .name("AWS").provider("").issueDate(LocalDate.now()).categoryId(1L).build();

        mockMvc.perform(post("/certificates")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCertificate_shouldReturn400_whenIssueDateIsNull() throws Exception {
        setupAdminAuth();
        CreateCertificateRequest request = CreateCertificateRequest.builder()
                .name("AWS").provider("Amazon").issueDate(null).categoryId(1L).build();

        mockMvc.perform(post("/certificates")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCertificate_shouldReturn400_whenCategoryNotFound() throws Exception {
        setupAdminAuth();
        CreateCertificateRequest request = CreateCertificateRequest.builder()
                .name("AWS").provider("Amazon").issueDate(LocalDate.now()).categoryId(99L).build();
        when(certificateService.createCertificate(any()))
                .thenThrow(new NoSuchElementException("Category not found"));

        mockMvc.perform(post("/certificates")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category not found"));
    }

    // ── POST /certificates — security ─────────────────────────────────────────

    @Test
    void createCertificate_shouldReturn401_whenNotAuthenticated() throws Exception {
        CreateCertificateRequest request = CreateCertificateRequest.builder()
                .name("AWS").provider("Amazon").issueDate(LocalDate.now()).categoryId(1L).build();

        mockMvc.perform(post("/certificates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(certificateService, never()).createCertificate(any());
    }

    @Test
    void createCertificate_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();
        CreateCertificateRequest request = CreateCertificateRequest.builder()
                .name("AWS").provider("Amazon").issueDate(LocalDate.now()).categoryId(1L).build();

        mockMvc.perform(post("/certificates")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(certificateService, never()).createCertificate(any());
    }

    // ── PUT /certificates/{id} — happy path ───────────────────────────────────

    @Test
    void updateCertificate_shouldReturn200WithDto_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();
        UpdateCertificateRequest request = UpdateCertificateRequest.builder()
                .name("Updated Name").provider("Updated Provider").build();
        when(certificateService.updateCertificate(eq(1L), any(UpdateCertificateRequest.class)))
                .thenReturn(buildCertificateDto());

        mockMvc.perform(put("/certificates/1")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("AWS Certified Developer"));
    }

    @Test
    void updateCertificate_shouldReturn404_whenCertificateNotFound() throws Exception {
        setupAdminAuth();
        UpdateCertificateRequest request = UpdateCertificateRequest.builder().name("X").build();
        when(certificateService.updateCertificate(eq(99L), any()))
                .thenThrow(new NoSuchElementException("Certificate not found"));

        mockMvc.perform(put("/certificates/99")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Certificate not found"));
    }

    // ── PUT /certificates/{id} — security ────────────────────────────────────

    @Test
    void updateCertificate_shouldReturn401_whenNotAuthenticated() throws Exception {
        UpdateCertificateRequest request = UpdateCertificateRequest.builder().name("X").build();

        mockMvc.perform(put("/certificates/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(certificateService, never()).updateCertificate(any(), any());
    }

    @Test
    void updateCertificate_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();
        UpdateCertificateRequest request = UpdateCertificateRequest.builder().name("X").build();

        mockMvc.perform(put("/certificates/1")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ── DELETE /certificates/{id} — happy path ────────────────────────────────

    @Test
    void deleteCertificate_shouldReturn204_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();
        doNothing().when(certificateService).deleteCertificate(1L);

        mockMvc.perform(delete("/certificates/1")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNoContent());

        verify(certificateService).deleteCertificate(1L);
    }

    @Test
    void deleteCertificate_shouldReturn404_whenCertificateNotFound() throws Exception {
        setupAdminAuth();
        doThrow(new NoSuchElementException("Certificate not found"))
                .when(certificateService).deleteCertificate(99L);

        mockMvc.perform(delete("/certificates/99")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Certificate not found"));
    }

    // ── DELETE /certificates/{id} — security ──────────────────────────────────

    @Test
    void deleteCertificate_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/certificates/1"))
                .andExpect(status().isUnauthorized());

        verify(certificateService, never()).deleteCertificate(any());
    }

    @Test
    void deleteCertificate_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();

        mockMvc.perform(delete("/certificates/1")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden());
    }

    // ── GET /certificates/categories — happy path ────────────────────────────

    @Test
    void getAllCategories_shouldReturn200WithList() throws Exception {
        when(certificateService.getAllCategories()).thenReturn(List.of(buildCategoryDto()));

        mockMvc.perform(get("/certificates/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Cloud"))
                .andExpect(jsonPath("$[0].industry").value("IT"));
    }

    @Test
    void getAllCategories_shouldBeAccessibleWithoutAuthentication() throws Exception {
        when(certificateService.getAllCategories()).thenReturn(List.of());

        mockMvc.perform(get("/certificates/categories"))
                .andExpect(status().isOk());

        verify(jwtService, never()).extractEmail(any());
    }

    // ── GET /certificates/categories/{id} ────────────────────────────────────

    @Test
    void getCategory_shouldReturn200WithDto_whenFound() throws Exception {
        when(certificateService.getCategory(1L)).thenReturn(buildCategoryDto());

        mockMvc.perform(get("/certificates/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Cloud"));
    }

    @Test
    void getCategory_shouldReturn404_whenNotFound() throws Exception {
        when(certificateService.getCategory(99L))
                .thenThrow(new NoSuchElementException("Category not found"));

        mockMvc.perform(get("/certificates/categories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category not found"));
    }

    // ── POST /certificates/categories — happy path ───────────────────────────

    @Test
    void createCategory_shouldReturn201WithDto_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Cloud").description("Cloud certs").industry("IT").sortOrder(1).build();
        when(certificateService.createCategory(any(CreateCategoryRequest.class)))
                .thenReturn(buildCategoryDto());

        mockMvc.perform(post("/certificates/categories")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Cloud"))
                .andExpect(jsonPath("$.industry").value("IT"));
    }

    @Test
    void createCategory_shouldReturn400_whenNameIsBlank() throws Exception {
        setupAdminAuth();
        CreateCategoryRequest request = CreateCategoryRequest.builder().name("").build();

        mockMvc.perform(post("/certificates/categories")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(certificateService, never()).createCategory(any());
    }

    // ── POST /certificates/categories — security ─────────────────────────────

    @Test
    void createCategory_shouldReturn401_whenNotAuthenticated() throws Exception {
        CreateCategoryRequest request = CreateCategoryRequest.builder().name("Cloud").build();

        mockMvc.perform(post("/certificates/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(certificateService, never()).createCategory(any());
    }

    @Test
    void createCategory_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();
        CreateCategoryRequest request = CreateCategoryRequest.builder().name("Cloud").build();

        mockMvc.perform(post("/certificates/categories")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ── PUT /certificates/categories/{id} ────────────────────────────────────

    @Test
    void updateCategory_shouldReturn200WithDto_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();
        UpdateCategoryRequest request = UpdateCategoryRequest.builder().name("Updated Cloud").build();
        when(certificateService.updateCategory(eq(1L), any(UpdateCategoryRequest.class)))
                .thenReturn(buildCategoryDto());

        mockMvc.perform(put("/certificates/categories/1")
                        .header("Authorization", "Bearer admin-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cloud"));
    }

    @Test
    void updateCategory_shouldReturn401_whenNotAuthenticated() throws Exception {
        UpdateCategoryRequest request = UpdateCategoryRequest.builder().name("X").build();

        mockMvc.perform(put("/certificates/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(certificateService, never()).updateCategory(any(), any());
    }

    @Test
    void updateCategory_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();
        UpdateCategoryRequest request = UpdateCategoryRequest.builder().name("X").build();

        mockMvc.perform(put("/certificates/categories/1")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ── DELETE /certificates/categories/{id} ─────────────────────────────────

    @Test
    void deleteCategory_shouldReturn204_whenAdminAuthenticated() throws Exception {
        setupAdminAuth();
        doNothing().when(certificateService).deleteCategory(1L);

        mockMvc.perform(delete("/certificates/categories/1")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNoContent());

        verify(certificateService).deleteCategory(1L);
    }

    @Test
    void deleteCategory_shouldReturn404_whenCategoryNotFound() throws Exception {
        setupAdminAuth();
        doThrow(new NoSuchElementException("Category not found"))
                .when(certificateService).deleteCategory(99L);

        mockMvc.perform(delete("/certificates/categories/99")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category not found"));
    }

    @Test
    void deleteCategory_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/certificates/categories/1"))
                .andExpect(status().isUnauthorized());

        verify(certificateService, never()).deleteCategory(any());
    }

    @Test
    void deleteCategory_shouldReturn403_whenUserRole() throws Exception {
        setupUserAuth();

        mockMvc.perform(delete("/certificates/categories/1")
                        .header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden());
    }
}
