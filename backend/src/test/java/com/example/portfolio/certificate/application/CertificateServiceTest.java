package com.example.portfolio.certificate.application;

import com.example.portfolio.certificate.domain.Certificate;
import com.example.portfolio.certificate.domain.CertificationCategory;
import com.example.portfolio.certificate.dto.*;
import com.example.portfolio.certificate.mapper.CertificateMapper;
import com.example.portfolio.certificate.persistence.CertificateCategoryRepository;
import com.example.portfolio.certificate.persistence.CertificateRepository;
import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.profile.persistence.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CertificateServiceTest {

    @Mock private CertificateRepository certificateRepository;
    @Mock private CertificateCategoryRepository certificateCategoryRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private CertificateMapper certificateMapper;

    @InjectMocks
    private CertificateService certificateService;

    private Profile profile;
    private CertificationCategory category;
    private Certificate certificate;
    private CertificateDto certificateDto;
    private CertificationCategoryDto categoryDto;

    @BeforeEach
    void setUp() {
        profile = Profile.builder().firstName("John").lastName("Doe").build();

        category = CertificationCategory.builder()
                .name("Cloud")
                .description("Cloud certifications")
                .industry("IT")
                .sortOrder(1)
                .build();

        certificate = Certificate.builder()
                .name("AWS Certified Developer")
                .provider("Amazon")
                .issueDate(LocalDate.of(2024, 1, 15))
                .certificationCategory(category)
                .profile(profile)
                .build();

        certificateDto = CertificateDto.builder()
                .id(1L)
                .name("AWS Certified Developer")
                .provider("Amazon")
                .issueDate(LocalDate.of(2024, 1, 15))
                .categoryId(1L)
                .categoryName("Cloud")
                .build();

        categoryDto = CertificationCategoryDto.builder()
                .id(1L)
                .name("Cloud")
                .industry("IT")
                .sortOrder(1)
                .build();
    }

    // ── createCertificate ─────────────────────────────────────────────────────

    @Test
    void createCertificate_shouldSaveAndReturnDto_whenProfileAndCategoryExist() {
        CreateCertificateRequest request = CreateCertificateRequest.builder()
                .name("AWS Certified Developer").provider("Amazon")
                .issueDate(LocalDate.of(2024, 1, 15)).categoryId(1L).build();

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(certificateCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(certificateRepository.save(any(Certificate.class))).thenReturn(certificate);
        when(certificateMapper.toCertificateDto(certificate)).thenReturn(certificateDto);

        CertificateDto result = certificateService.createCertificate(request);

        assertThat(result).isEqualTo(certificateDto);
        verify(certificateRepository).save(any(Certificate.class));
    }

    @Test
    void createCertificate_shouldThrowNoSuchElementException_whenProfileNotFound() {
        CreateCertificateRequest request = CreateCertificateRequest.builder()
                .name("AWS").provider("Amazon").issueDate(LocalDate.now()).categoryId(1L).build();
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> certificateService.createCertificate(request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Profile not found");

        verify(certificateRepository, never()).save(any());
    }

    @Test
    void createCertificate_shouldThrowNoSuchElementException_whenCategoryNotFound() {
        CreateCertificateRequest request = CreateCertificateRequest.builder()
                .name("AWS").provider("Amazon").issueDate(LocalDate.now()).categoryId(99L).build();

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(certificateCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> certificateService.createCertificate(request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Category not found");

        verify(certificateRepository, never()).save(any());
    }

    @Test
    void createCertificate_shouldFindExistingCategoryByName_insteadOfCreatingDuplicate() {
        CreateCertificateRequest request = CreateCertificateRequest.builder()
                .name("AWS").provider("Amazon").issueDate(LocalDate.now()).categoryName("Cloud").build();

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(certificateCategoryRepository.findByName("Cloud")).thenReturn(Optional.of(category));
        when(certificateRepository.save(any(Certificate.class))).thenReturn(certificate);
        when(certificateMapper.toCertificateDto(certificate)).thenReturn(certificateDto);

        certificateService.createCertificate(request);

        verify(certificateCategoryRepository, never()).save(any());
    }

    @Test
    void createCertificate_shouldCreateCategory_whenNameDoesNotExist() {
        CreateCertificateRequest request = CreateCertificateRequest.builder()
                .name("AWS").provider("Amazon").issueDate(LocalDate.now()).categoryName("Data Science").build();

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(certificateCategoryRepository.findByName("Data Science")).thenReturn(Optional.empty());
        when(certificateCategoryRepository.save(any(CertificationCategory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(certificateRepository.save(any(Certificate.class))).thenReturn(certificate);
        when(certificateMapper.toCertificateDto(any())).thenReturn(certificateDto);

        certificateService.createCertificate(request);

        var captor = org.mockito.ArgumentCaptor.forClass(CertificationCategory.class);
        verify(certificateCategoryRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Data Science");
    }

    @Test
    void createCertificate_shouldThrowIllegalArgumentException_whenNeitherCategoryIdNorNameProvided() {
        CreateCertificateRequest request = CreateCertificateRequest.builder()
                .name("AWS").provider("Amazon").issueDate(LocalDate.now()).build();

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> certificateService.createCertificate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Either categoryId or categoryName must be provided");

        verify(certificateRepository, never()).save(any());
    }

    @Test
    void createCertificate_shouldDefaultHasExpiryToFalse_whenNotProvided() {
        CreateCertificateRequest request = CreateCertificateRequest.builder()
                .name("AWS").provider("Amazon").issueDate(LocalDate.now()).categoryId(1L)
                .hasExpiry(null).build();

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(certificateCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(certificateRepository.save(any(Certificate.class))).thenReturn(certificate);
        when(certificateMapper.toCertificateDto(any())).thenReturn(certificateDto);

        certificateService.createCertificate(request);

        verify(certificateRepository).save(argThat(c -> !c.getHasExpiry()));
    }

    // ── updateCertificate ─────────────────────────────────────────────────────

    @Test
    void updateCertificate_shouldUpdateAndReturnDto_whenCertificateExists() {
        UpdateCertificateRequest request = UpdateCertificateRequest.builder()
                .name("Updated Name").provider("Updated Provider").build();

        when(certificateRepository.findById(1L)).thenReturn(Optional.of(certificate));
        when(certificateRepository.save(certificate)).thenReturn(certificate);
        when(certificateMapper.toCertificateDto(certificate)).thenReturn(certificateDto);

        CertificateDto result = certificateService.updateCertificate(1L, request);

        assertThat(result).isEqualTo(certificateDto);
        verify(certificateMapper).updateCertificate(request, certificate);
        verify(certificateRepository).save(certificate);
    }

    @Test
    void updateCertificate_shouldUpdateCategory_whenCategoryIdProvided() {
        CertificationCategory newCategory = CertificationCategory.builder().name("Security").build();
        UpdateCertificateRequest request = UpdateCertificateRequest.builder().categoryId(2L).build();

        when(certificateRepository.findById(1L)).thenReturn(Optional.of(certificate));
        when(certificateCategoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));
        when(certificateRepository.save(certificate)).thenReturn(certificate);
        when(certificateMapper.toCertificateDto(certificate)).thenReturn(certificateDto);

        certificateService.updateCertificate(1L, request);

        assertThat(certificate.getCertificationCategory()).isEqualTo(newCategory);
    }

    @Test
    void updateCertificate_shouldRenameSharedCategory_whenIdAndDifferingNameProvided() {
        ReflectionTestUtils.setField(category, "id", 5L);
        UpdateCertificateRequest request = UpdateCertificateRequest.builder()
                .categoryId(5L).categoryName("Cloud Computing").build();

        when(certificateRepository.findById(1L)).thenReturn(Optional.of(certificate));
        when(certificateCategoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(certificateCategoryRepository.save(any(CertificationCategory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(certificateRepository.save(certificate)).thenReturn(certificate);
        when(certificateMapper.toCertificateDto(certificate)).thenReturn(certificateDto);

        certificateService.updateCertificate(1L, request);

        assertThat(category.getName()).isEqualTo("Cloud Computing");
        verify(certificateCategoryRepository).save(category);
    }

    @Test
    void updateCertificate_shouldNotRenameCategory_whenNameMatchesCurrent() {
        ReflectionTestUtils.setField(category, "id", 5L);
        UpdateCertificateRequest request = UpdateCertificateRequest.builder()
                .categoryId(5L).categoryName("Cloud").build();

        when(certificateRepository.findById(1L)).thenReturn(Optional.of(certificate));
        when(certificateCategoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(certificateRepository.save(certificate)).thenReturn(certificate);
        when(certificateMapper.toCertificateDto(certificate)).thenReturn(certificateDto);

        certificateService.updateCertificate(1L, request);

        verify(certificateCategoryRepository, never()).save(any());
    }

    @Test
    void updateCertificate_shouldFindOrCreateCategoryByName_whenOnlyNameProvided() {
        UpdateCertificateRequest request = UpdateCertificateRequest.builder().categoryName("Security").build();

        when(certificateRepository.findById(1L)).thenReturn(Optional.of(certificate));
        when(certificateCategoryRepository.findByName("Security")).thenReturn(Optional.empty());
        when(certificateCategoryRepository.save(any(CertificationCategory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(certificateRepository.save(certificate)).thenReturn(certificate);
        when(certificateMapper.toCertificateDto(certificate)).thenReturn(certificateDto);

        certificateService.updateCertificate(1L, request);

        assertThat(certificate.getCertificationCategory().getName()).isEqualTo("Security");
        verify(certificateCategoryRepository, never()).findById(any());
    }

    @Test
    void updateCertificate_shouldThrowNoSuchElementException_whenCertificateNotFound() {
        UpdateCertificateRequest request = UpdateCertificateRequest.builder().name("X").build();
        when(certificateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> certificateService.updateCertificate(99L, request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Certificate not found");
    }

    @Test
    void updateCertificate_shouldThrowNoSuchElementException_whenNewCategoryNotFound() {
        UpdateCertificateRequest request = UpdateCertificateRequest.builder().categoryId(99L).build();
        when(certificateRepository.findById(1L)).thenReturn(Optional.of(certificate));
        when(certificateCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> certificateService.updateCertificate(1L, request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Category not found");
    }

    // ── deleteCertificate ─────────────────────────────────────────────────────

    @Test
    void deleteCertificate_shouldDeleteById_whenCertificateExists() {
        when(certificateRepository.existsById(1L)).thenReturn(true);

        certificateService.deleteCertificate(1L);

        verify(certificateRepository).deleteById(1L);
    }

    @Test
    void deleteCertificate_shouldThrowNoSuchElementException_whenCertificateNotFound() {
        when(certificateRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> certificateService.deleteCertificate(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Certificate not found");

        verify(certificateRepository, never()).deleteById(any());
    }

    // ── getCertificate ────────────────────────────────────────────────────────

    @Test
    void getCertificate_shouldReturnDto_whenFound() {
        when(certificateRepository.findById(1L)).thenReturn(Optional.of(certificate));
        when(certificateMapper.toCertificateDto(certificate)).thenReturn(certificateDto);

        CertificateDto result = certificateService.getCertificate(1L);

        assertThat(result).isEqualTo(certificateDto);
    }

    @Test
    void getCertificate_shouldThrowNoSuchElementException_whenNotFound() {
        when(certificateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> certificateService.getCertificate(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Certificate not found");
    }

    // ── getAllCertificates ────────────────────────────────────────────────────

    @Test
    void getAllCertificates_shouldReturnAllMapped_whenNoFiltersProvided() {
        when(certificateRepository.findAll(any(Specification.class))).thenReturn(List.of(certificate));
        when(certificateMapper.toCertificateDto(certificate)).thenReturn(certificateDto);

        List<CertificateDto> result = certificateService.getAllCertificates(null, null, null);

        assertThat(result).containsExactly(certificateDto);
        verify(certificateRepository).findAll(any(Specification.class));
    }

    @Test
    void getAllCertificates_shouldApplySpec_whenCategoryIdProvided() {
        when(certificateRepository.findAll(any(Specification.class))).thenReturn(List.of(certificate));
        when(certificateMapper.toCertificateDto(certificate)).thenReturn(certificateDto);

        List<CertificateDto> result = certificateService.getAllCertificates(1L, null, null);

        assertThat(result).hasSize(1);
        verify(certificateRepository).findAll(any(Specification.class));
    }

    @Test
    void getAllCertificates_shouldApplySpec_whenProviderFilterProvided() {
        when(certificateRepository.findAll(any(Specification.class))).thenReturn(List.of(certificate));
        when(certificateMapper.toCertificateDto(certificate)).thenReturn(certificateDto);

        List<CertificateDto> result = certificateService.getAllCertificates(null, "Amazon", null);

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllCertificates_shouldApplySpec_whenVerifiedFilterProvided() {
        when(certificateRepository.findAll(any(Specification.class))).thenReturn(List.of());

        List<CertificateDto> result = certificateService.getAllCertificates(null, null, true);

        assertThat(result).isEmpty();
        verify(certificateMapper, never()).toCertificateDto(any());
    }

    @Test
    void getAllCertificates_shouldReturnEmpty_whenBlankProviderGiven() {
        when(certificateRepository.findAll(any(Specification.class))).thenReturn(List.of());

        List<CertificateDto> result = certificateService.getAllCertificates(null, "  ", null);

        assertThat(result).isEmpty();
    }

    // ── createCategory ────────────────────────────────────────────────────────

    @Test
    void createCategory_shouldSaveAndReturnDto() {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Cloud").description("Cloud certs").industry("IT").sortOrder(1).build();

        when(certificateCategoryRepository.save(any(CertificationCategory.class))).thenReturn(category);
        when(certificateMapper.toCategoryDto(category)).thenReturn(categoryDto);

        CertificationCategoryDto result = certificateService.createCategory(request);

        assertThat(result).isEqualTo(categoryDto);
        verify(certificateCategoryRepository).save(any(CertificationCategory.class));
    }

    @Test
    void createCategory_shouldDefaultSortOrderToZero_whenNotProvided() {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Cloud").sortOrder(null).build();

        when(certificateCategoryRepository.save(any(CertificationCategory.class))).thenReturn(category);
        when(certificateMapper.toCategoryDto(any())).thenReturn(categoryDto);

        certificateService.createCategory(request);

        verify(certificateCategoryRepository).save(argThat(c -> c.getSortOrder() == 0));
    }

    // ── updateCategory ────────────────────────────────────────────────────────

    @Test
    void updateCategory_shouldUpdateAndReturnDto_whenCategoryExists() {
        UpdateCategoryRequest request = UpdateCategoryRequest.builder().name("Updated Cloud").build();

        when(certificateCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(certificateCategoryRepository.save(category)).thenReturn(category);
        when(certificateMapper.toCategoryDto(category)).thenReturn(categoryDto);

        CertificationCategoryDto result = certificateService.updateCategory(1L, request);

        assertThat(result).isEqualTo(categoryDto);
        verify(certificateMapper).updateCategory(request, category);
    }

    @Test
    void updateCategory_shouldThrowNoSuchElementException_whenNotFound() {
        UpdateCategoryRequest request = UpdateCategoryRequest.builder().name("X").build();
        when(certificateCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> certificateService.updateCategory(99L, request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Category not found");
    }

    // ── deleteCategory ────────────────────────────────────────────────────────

    @Test
    void deleteCategory_shouldDeleteById_whenCategoryExists() {
        when(certificateCategoryRepository.existsById(1L)).thenReturn(true);

        certificateService.deleteCategory(1L);

        verify(certificateCategoryRepository).deleteById(1L);
    }

    @Test
    void deleteCategory_shouldThrowNoSuchElementException_whenNotFound() {
        when(certificateCategoryRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> certificateService.deleteCategory(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Category not found");

        verify(certificateCategoryRepository, never()).deleteById(any());
    }

    // ── getCategory ───────────────────────────────────────────────────────────

    @Test
    void getCategory_shouldReturnDto_whenFound() {
        when(certificateCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(certificateMapper.toCategoryDto(category)).thenReturn(categoryDto);

        CertificationCategoryDto result = certificateService.getCategory(1L);

        assertThat(result).isEqualTo(categoryDto);
    }

    @Test
    void getCategory_shouldThrowNoSuchElementException_whenNotFound() {
        when(certificateCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> certificateService.getCategory(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Category not found");
    }

    // ── getAllCategories ──────────────────────────────────────────────────────

    @Test
    void getAllCategories_shouldReturnAllMapped() {
        when(certificateCategoryRepository.findAll()).thenReturn(List.of(category));
        when(certificateMapper.toCategoryDto(category)).thenReturn(categoryDto);

        List<CertificationCategoryDto> result = certificateService.getAllCategories();

        assertThat(result).containsExactly(categoryDto);
    }

    @Test
    void getAllCategories_shouldReturnEmptyList_whenNoCategoriesExist() {
        when(certificateCategoryRepository.findAll()).thenReturn(List.of());

        List<CertificationCategoryDto> result = certificateService.getAllCategories();

        assertThat(result).isEmpty();
        verify(certificateMapper, never()).toCategoryDto(any());
    }
}
