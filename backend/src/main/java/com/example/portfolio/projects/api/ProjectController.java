package com.example.portfolio.projects.api;

import com.example.portfolio.projects.application.ProjectService;
import com.example.portfolio.projects.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // ─────────────────────────── PROJECTS ────────────────────────────

    @GetMapping
    public ResponseEntity<List<ProjectListItemDto>> getProjects() {
        return ResponseEntity.ok(projectService.getProjectsList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDetailDto> getProject(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    /**
     * Multipart: part "data" = JSON (CreateProjectRequest), part "files" = media files (optional, multiple).
     * Files are matched in order to image slots whose imageUrl is null/blank.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProjectDetailDto> createProject(
            @RequestPart("data") @Valid CreateProjectRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.createProject(request, files));
    }

    /**
     * Multipart: part "data" = JSON (UpdateProjectRequest), part "files" = new media files (optional, multiple).
     * Existing image slots that already carry a URL are not re-uploaded.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProjectDetailDto> updateProject(
            @PathVariable Long id,
            @RequestPart("data") UpdateProjectRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        return ResponseEntity.ok(projectService.updateProject(id, request, files));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    // ─────────────────────────── CATEGORIES ──────────────────────────

    @GetMapping("/categories")
    public ResponseEntity<List<ProjectCategoryDto>> getCategories() {
        return ResponseEntity.ok(projectService.getAllCategories());
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<ProjectCategoryDto> getCategory(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getCategory(id));
    }

    @PostMapping("/categories")
    public ResponseEntity<ProjectCategoryDto> createCategory(
            @Valid @RequestBody CreateProjectCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createCategory(request));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<ProjectCategoryDto> updateCategory(
            @PathVariable Long id,
            @RequestBody UpdateProjectCategoryRequest request) {
        return ResponseEntity.ok(projectService.updateCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        projectService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
