package com.example.portfolio.hobby.api;

import com.example.portfolio.hobby.application.HobbyService;
import com.example.portfolio.hobby.domain.ActivityLevel;
import com.example.portfolio.hobby.domain.HobbyCategory;
import com.example.portfolio.hobby.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hobbies")
@RequiredArgsConstructor
public class HobbyController {

    private final HobbyService hobbyService;

    @GetMapping
    public ResponseEntity<List<HobbyDto>> getHobbies(
            @RequestParam(required = false) HobbyCategory category,
            @RequestParam(required = false) ActivityLevel activityLevel) {
        return ResponseEntity.ok(hobbyService.getHobbies(category, activityLevel));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HobbyDetailDto> getHobby(@PathVariable Long id) {
        return ResponseEntity.ok(hobbyService.getHobbyById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HobbyDto> createHobby(@Valid @RequestBody CreateHobbyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hobbyService.createHobby(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HobbyDto> updateHobby(
            @PathVariable Long id, @Valid @RequestBody UpdateHobbyRequest request) {
        return ResponseEntity.ok(hobbyService.updateHobby(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteHobby(@PathVariable Long id) {
        hobbyService.deleteHobby(id);
        return ResponseEntity.noContent().build();
    }
}
