package com.example.portfolio.experience.dto;

import com.example.portfolio.experience.domain.VolunteerStatus;
import com.example.portfolio.experience.domain.VolunteerType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerExperienceDetailDto {
    private Long id;
    private String organization;
    private String role;
    private VolunteerType type;
    private String location;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    private VolunteerStatus status;
    private String description;
    private String impactDescription;
    private String website;
    private BigDecimal hoursPerWeek;
    private BigDecimal totalHours;
    private List<VolunteerResponsibilityDto> responsibilities;
    private List<VolunteerProjectDto> projects;
}
