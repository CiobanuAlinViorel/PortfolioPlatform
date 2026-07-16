package com.example.portfolio.certificate.api;

import com.example.portfolio.certificate.application.CertificateService;
import com.example.portfolio.certificate.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    // ── Certificate endpoints ─────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<CertificateDto>> getAllCertificates(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) Boolean verified) {
        return ResponseEntity.ok(certificateService.getAllCertificates(categoryId, provider, verified));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CertificateDto> getCertificate(@PathVariable Long id) {
        return ResponseEntity.ok(certificateService.getCertificate(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CertificateDto> createCertificate(@Valid @RequestBody CreateCertificateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(certificateService.createCertificate(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CertificateDto> updateCertificate(
            @PathVariable Long id, @Valid @RequestBody UpdateCertificateRequest request) {
        return ResponseEntity.ok(certificateService.updateCertificate(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCertificate(@PathVariable Long id) {
        certificateService.deleteCertificate(id);
        return ResponseEntity.noContent().build();
    }

    // ── Category endpoints ────────────────────────────────────────────────────

    @GetMapping("/categories")
    public ResponseEntity<List<CertificationCategoryDto>> getAllCategories() {
        return ResponseEntity.ok(certificateService.getAllCategories());
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<CertificationCategoryDto> getCategory(@PathVariable Long id) {
        return ResponseEntity.ok(certificateService.getCategory(id));
    }

    @PostMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CertificationCategoryDto> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(certificateService.createCategory(request));
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CertificationCategoryDto> updateCategory(
            @PathVariable Long id, @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(certificateService.updateCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        certificateService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
