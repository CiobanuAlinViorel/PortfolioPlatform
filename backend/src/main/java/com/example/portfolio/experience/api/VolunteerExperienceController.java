package com.example.portfolio.experience.api;

import com.example.portfolio.experience.application.VolunteerExperienceService;
import com.example.portfolio.experience.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/experience/volunteers")
@RequiredArgsConstructor
public class VolunteerExperienceController {

    private final VolunteerExperienceService volunteerExperienceService;

    // ── Public ────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<VolunteerExperienceListItemDto>> getVolunteerExperiences() {
        return ResponseEntity.ok(volunteerExperienceService.getVolunteerExperienceList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VolunteerExperienceDetailDto> getVolunteerExperience(@PathVariable Long id) {
        return ResponseEntity.ok(volunteerExperienceService.getVolunteerExperienceById(id));
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VolunteerExperienceDetailDto> createVolunteerExperience(
            @Valid @RequestBody CreateVolunteerExperienceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(volunteerExperienceService.createVolunteerExperience(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VolunteerExperienceDetailDto> updateVolunteerExperience(
            @PathVariable Long id,
            @RequestBody UpdateVolunteerExperienceRequest request) {
        return ResponseEntity.ok(volunteerExperienceService.updateVolunteerExperience(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVolunteerExperience(@PathVariable Long id) {
        volunteerExperienceService.deleteVolunteerExperience(id);
        return ResponseEntity.noContent().build();
    }
}
