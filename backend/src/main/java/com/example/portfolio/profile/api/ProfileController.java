package com.example.portfolio.profile.api;

import com.example.portfolio.profile.application.ProfileService;
import com.example.portfolio.profile.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/summary")
    public ResponseEntity<ProfileSummaryResponse> getSummary() {
        return ResponseEntity.ok(profileService.getProfileSummary());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfileInfoDto> createProfile(@Valid @RequestBody CreateProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(profileService.createProfile(request));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfileInfoDto> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(request));
    }
}
