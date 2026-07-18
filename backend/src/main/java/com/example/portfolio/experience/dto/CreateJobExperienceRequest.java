package com.example.portfolio.experience.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateJobExperienceRequest {
    @NotBlank
    private String companyName;
    @NotBlank
    private String role;
    @NotNull
    private LocalDate startDate;
    private LocalDate endDate;

    @Valid
    private List<JobProjectRequest> projects;
}
