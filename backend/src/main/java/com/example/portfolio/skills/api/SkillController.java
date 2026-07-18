package com.example.portfolio.skills.api;

import com.example.portfolio.skills.application.SkillService;
import com.example.portfolio.skills.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    // ─────────────────────────── SKILLS ───────────────────────────

    @GetMapping
    public ResponseEntity<List<SkillListItemDto>> getSkills() {
        return ResponseEntity.ok(skillService.getSkillsList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SkillDetailDto> getSkill(@PathVariable Long id) {
        return ResponseEntity.ok(skillService.getSkillById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SkillDetailDto> createSkill(@Valid @RequestBody CreateSkillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(skillService.createSkill(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SkillDetailDto> updateSkill(@PathVariable Long id, @RequestBody UpdateSkillRequest request) {
        return ResponseEntity.ok(skillService.updateSkill(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        skillService.deleteSkill(id);
        return ResponseEntity.noContent().build();
    }

    // ─────────────────────────── CATEGORIES ───────────────────────

    @GetMapping("/categories")
    public ResponseEntity<List<SkillCategoryDto>> getCategories() {
        return ResponseEntity.ok(skillService.getAllCategories());
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<SkillCategoryDto> getCategory(@PathVariable Long id) {
        return ResponseEntity.ok(skillService.getCategory(id));
    }

    @PostMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SkillCategoryDto> createCategory(@Valid @RequestBody CreateSkillCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(skillService.createCategory(request));
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SkillCategoryDto> updateCategory(
            @PathVariable Long id, @RequestBody UpdateSkillCategoryRequest request) {
        return ResponseEntity.ok(skillService.updateCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        skillService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
