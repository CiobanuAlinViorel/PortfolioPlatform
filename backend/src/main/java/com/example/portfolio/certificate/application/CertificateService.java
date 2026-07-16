package com.example.portfolio.certificate.application;

import com.example.portfolio.certificate.domain.Certificate;
import com.example.portfolio.certificate.domain.CertificationCategory;
import com.example.portfolio.certificate.dto.*;
import com.example.portfolio.certificate.mapper.CertificateMapper;
import com.example.portfolio.certificate.persistence.CertificateCategoryRepository;
import com.example.portfolio.certificate.persistence.CertificateRepository;
import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.profile.persistence.ProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final CertificateCategoryRepository certificateCategoryRepository;
    private final ProfileRepository profileRepository;
    private final CertificateMapper certificateMapper;

    @Transactional
    public CertificateDto createCertificate(CreateCertificateRequest request) {
        Profile profile = profileRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new NoSuchElementException("Profile not found"));
        CertificationCategory category = certificateCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NoSuchElementException("Category not found"));

        Certificate certificate = Certificate.builder()
                .profile(profile)
                .name(request.getName())
                .provider(request.getProvider())
                .credentialId(request.getCredentialId())
                .certificateUrl(request.getCertificateUrl())
                .issueDate(request.getIssueDate())
                .expiryDate(request.getExpiryDate())
                .hasExpiry(request.getHasExpiry() != null ? request.getHasExpiry() : false)
                .description(request.getDescription())
                .score(request.getScore())
                .relevanceScore(request.getRelevanceScore() != null ? request.getRelevanceScore() : 50)
                .certificationCategory(category)
                .build();

        return certificateMapper.toCertificateDto(certificateRepository.save(certificate));
    }

    @Transactional
    public CertificateDto updateCertificate(Long id, UpdateCertificateRequest request) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Certificate not found"));

        if (request.getCategoryId() != null) {
            CertificationCategory category = certificateCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NoSuchElementException("Category not found"));
            certificate.setCertificationCategory(category);
        }

        certificateMapper.updateCertificate(request, certificate);
        return certificateMapper.toCertificateDto(certificateRepository.save(certificate));
    }

    @Transactional
    public void deleteCertificate(Long id) {
        if (!certificateRepository.existsById(id)) {
            throw new NoSuchElementException("Certificate not found");
        }
        certificateRepository.deleteById(id);
    }

    @Transactional
    public CertificateDto getCertificate(Long id) {
        return certificateMapper.toCertificateDto(
                certificateRepository.findById(id)
                        .orElseThrow(() -> new NoSuchElementException("Certificate not found"))
        );
    }

    @Transactional
    public List<CertificateDto> getAllCertificates(Long categoryId, String provider, Boolean verified) {
        List<Specification<Certificate>> specs = new ArrayList<>();

        if (categoryId != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("certificationCategory").get("id"), categoryId));
        }
        if (provider != null && !provider.isBlank()) {
            specs.add((root, query, cb) -> cb.like(cb.lower(root.get("provider")), "%" + provider.toLowerCase() + "%"));
        }
        if (verified != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("verified"), verified));
        }

        return certificateRepository.findAll(Specification.allOf(specs))
                .stream()
                .map(certificateMapper::toCertificateDto)
                .toList();
    }

    @Transactional
    public CertificationCategoryDto createCategory(CreateCategoryRequest request) {
        CertificationCategory category = CertificationCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .industry(request.getIndustry())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();
        return certificateMapper.toCategoryDto(certificateCategoryRepository.save(category));
    }

    @Transactional
    public CertificationCategoryDto updateCategory(Long id, UpdateCategoryRequest request) {
        CertificationCategory category = certificateCategoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Category not found"));
        certificateMapper.updateCategory(request, category);
        return certificateMapper.toCategoryDto(certificateCategoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!certificateCategoryRepository.existsById(id)) {
            throw new NoSuchElementException("Category not found");
        }
        certificateCategoryRepository.deleteById(id);
    }

    @Transactional
    public CertificationCategoryDto getCategory(Long id) {
        return certificateMapper.toCategoryDto(
                certificateCategoryRepository.findById(id)
                        .orElseThrow(() -> new NoSuchElementException("Category not found"))
        );
    }

    @Transactional
    public List<CertificationCategoryDto> getAllCategories() {
        return certificateCategoryRepository.findAll()
                .stream()
                .map(certificateMapper::toCategoryDto)
                .toList();
    }
}
