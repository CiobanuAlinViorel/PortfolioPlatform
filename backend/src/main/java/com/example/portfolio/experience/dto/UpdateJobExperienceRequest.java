package com.example.portfolio.experience.dto;

import jakarta.validation.Valid;
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
public class UpdateJobExperienceRequest {
    private String companyName;
    private String role;
    private LocalDate startDate;
    private LocalDate endDate;

    // Diffed by id against existing links: matched ids updated, unmatched ids removed, id-less entries created
    @Valid
    private List<JobProjectRequest> projects;
}
