package com.example.portfolio.profile.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProfileRequest {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    private Integer age;
    private String imageLink;
    private String description;
}
