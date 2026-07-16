package com.example.portfolio.education.api;

import com.example.portfolio.education.application.EducationService;
import com.example.portfolio.education.domain.EducationLevel;
import com.example.portfolio.education.domain.EducationStatus;
import com.example.portfolio.education.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/education")
@RequiredArgsConstructor
public class EducationController {

    private final EducationService educationService;

    @GetMapping
    public ResponseEntity<List<EducationDto>> getEducations(
            @RequestParam(required = false) EducationLevel level,
            @RequestParam(required = false) EducationStatus status) {
        return ResponseEntity.ok(educationService.getEducations(level, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EducationDetailDto> getEducation(@PathVariable Long id) {
        return ResponseEntity.ok(educationService.getEducationById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EducationDto> createEducation(@Valid @RequestBody CreateEducationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(educationService.createEducation(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EducationDto> updateEducation(
            @PathVariable Long id, @Valid @RequestBody UpdateEducationRequest request) {
        return ResponseEntity.ok(educationService.updateEducation(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEducation(@PathVariable Long id) {
        educationService.deleteEducation(id);
        return ResponseEntity.noContent().build();
    }
}
