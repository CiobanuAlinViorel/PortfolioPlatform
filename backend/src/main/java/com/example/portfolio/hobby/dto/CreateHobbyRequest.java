package com.example.portfolio.hobby.dto;

import com.example.portfolio.experience.domain.ImpactLevel;
import com.example.portfolio.hobby.domain.ActivityLevel;
import com.example.portfolio.hobby.domain.ComplexityLevel;
import com.example.portfolio.hobby.domain.HobbyCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateHobbyRequest {
    @NotBlank
    private String name;
    private String description;
    @NotNull
    private HobbyCategory category;
    private ActivityLevel activityLevel;
    private ComplexityLevel complexityLevel;
    private ImpactLevel impactOnWork;
    private Long yearsActive;
    private String whyInterested;
    private String favoriteAspect;
}
