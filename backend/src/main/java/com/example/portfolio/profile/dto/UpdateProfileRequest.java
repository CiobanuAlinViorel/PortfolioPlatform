package com.example.portfolio.profile.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
    private Integer age;
    private String imageLink;
    private String description;
    @NotNull
    @Valid
    private ContactInfoDto contactInfo;
}
