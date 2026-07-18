package com.example.portfolio.experience.dto;

import com.example.portfolio.experience.domain.VolunteerStatus;
import com.example.portfolio.experience.domain.VolunteerType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreateVolunteerExperienceRequest {
    @NotBlank
    private String organization;
    @NotBlank
    private String role;
    @NotNull
    private VolunteerType type;
    private String location;
    @NotNull
    private LocalDate startDate;
    private LocalDate endDate;
    @NotNull
    private VolunteerStatus status;
    private String description;
    private String impactDescription;
    private String website;
    private BigDecimal hoursPerWeek;
    private BigDecimal totalHours;

    @Valid
    private List<VolunteerResponsibilityRequest> responsibilities;
    @Valid
    private List<VolunteerProjectRequest> projects;
}
