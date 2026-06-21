package com.example.portfolio.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LastExperienceDto {
    private String companyName;
    private String role;
    private LocalDate startDate;
    private LocalDate endDate;
    private int projectsCount;
}
