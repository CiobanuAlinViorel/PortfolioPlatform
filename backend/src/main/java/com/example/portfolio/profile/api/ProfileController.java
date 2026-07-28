package com.example.portfolio.profile.api;

import com.example.portfolio.profile.application.ProfileService;
import com.example.portfolio.profile.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/summary")
    public ResponseEntity<ProfileSummaryResponse> getSummary() {
        return ResponseEntity.ok(profileService.getProfileSummary());
    }

    /**
     * Multipart: part "data" = JSON (CreateProfileRequest), part "image" = profile photo (optional).
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfileInfoDto> createProfile(
            @RequestPart("data") @Valid CreateProfileRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.status(HttpStatus.CREATED).body(profileService.createProfile(request, image));
    }

    /**
     * Multipart: part "data" = JSON (UpdateProfileRequest), part "image" = new profile photo (optional).
     */
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfileInfoDto> updateProfile(
            @RequestPart("data") @Valid UpdateProfileRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(profileService.updateProfile(request, image));
    }
}
