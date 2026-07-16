package com.example.portfolio.achievement.api;

import com.example.portfolio.achievement.application.AchievementService;
import com.example.portfolio.achievement.domain.AchievementType;
import com.example.portfolio.achievement.domain.RecognitionLevel;
import com.example.portfolio.achievement.dto.AchievementDto;
import com.example.portfolio.achievement.dto.CreateAchievementRequest;
import com.example.portfolio.achievement.dto.UpdateAchievementRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    @GetMapping
    public ResponseEntity<List<AchievementDto>> getAchievements(
            @RequestParam(required = false) AchievementType type,
            @RequestParam(required = false) RecognitionLevel recognitionLevel,
            @RequestParam(required = false) Boolean isFeatured) {
        return ResponseEntity.ok(achievementService.getAchievements(type, recognitionLevel, isFeatured));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AchievementDto> getAchievement(@PathVariable Long id) {
        return ResponseEntity.ok(achievementService.getAchievementById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AchievementDto> createAchievement(@Valid @RequestBody CreateAchievementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(achievementService.createAchievement(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AchievementDto> updateAchievement(
            @PathVariable Long id, @Valid @RequestBody UpdateAchievementRequest request) {
        return ResponseEntity.ok(achievementService.updateAchievement(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAchievement(@PathVariable Long id) {
        achievementService.deleteAchievement(id);
        return ResponseEntity.noContent().build();
    }
}
