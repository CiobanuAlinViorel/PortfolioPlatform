package com.example.portfolio.experience.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateJobExperienceRequest {
    private String companyName;
    private String role;
    private LocalDate startDate;
    private LocalDate endDate;
}
