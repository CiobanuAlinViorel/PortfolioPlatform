package com.example.portfolio.experience.dto;

import com.example.portfolio.experience.domain.VolunteerStatus;
import com.example.portfolio.experience.domain.VolunteerType;
import jakarta.validation.Valid;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateVolunteerExperienceRequest {
    private String organization;
    private String role;
    private VolunteerType type;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private VolunteerStatus status;
    private String description;
    private String impactDescription;
    private String website;
    private BigDecimal hoursPerWeek;
    private BigDecimal totalHours;

    // Diffed by id against existing rows: matched ids are updated, unmatched ids removed, id-less entries created
    @Valid
    private List<VolunteerResponsibilityRequest> responsibilities;
    @Valid
    private List<VolunteerProjectRequest> projects;
}
