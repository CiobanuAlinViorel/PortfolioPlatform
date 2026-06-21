package com.example.portfolio.experience.api;

import com.example.portfolio.experience.application.JobExperienceService;
import com.example.portfolio.experience.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/experience/jobs")
@RequiredArgsConstructor
public class JobExperienceController {

    private final JobExperienceService jobExperienceService;

    // ── Public ────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<JobExperienceListItemDto>> getJobExperiences() {
        return ResponseEntity.ok(jobExperienceService.getJobExperienceList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobExperienceDetailDto> getJobExperience(@PathVariable Long id) {
        return ResponseEntity.ok(jobExperienceService.getJobExperienceById(id));
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobExperienceDetailDto> createJobExperience(@Valid @RequestBody CreateJobExperienceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobExperienceService.createJobExperience(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobExperienceDetailDto> updateJobExperience(
            @PathVariable Long id,
            @RequestBody UpdateJobExperienceRequest request) {
        return ResponseEntity.ok(jobExperienceService.updateJobExperience(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteJobExperience(@PathVariable Long id) {
        jobExperienceService.deleteJobExperience(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/projects/{projectId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobExperienceDetailDto> addProject(
            @PathVariable Long id,
            @PathVariable Long projectId) {
        return ResponseEntity.ok(jobExperienceService.addProject(id, projectId));
    }

    @DeleteMapping("/{id}/projects/{projectId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JobExperienceDetailDto> removeProject(
            @PathVariable Long id,
            @PathVariable Long projectId) {
        return ResponseEntity.ok(jobExperienceService.removeProject(id, projectId));
    }

    @GetMapping("/available-projects")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProjectInJobDto>> getAvailableProjects() {
        return ResponseEntity.ok(jobExperienceService.getAvailableProjects());
    }
}
