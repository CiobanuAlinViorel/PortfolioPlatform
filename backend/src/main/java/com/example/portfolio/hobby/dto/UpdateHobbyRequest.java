package com.example.portfolio.hobby.dto;

import com.example.portfolio.experience.domain.ImpactLevel;
import com.example.portfolio.hobby.domain.ActivityLevel;
import com.example.portfolio.hobby.domain.ComplexityLevel;
import com.example.portfolio.hobby.domain.HobbyCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateHobbyRequest {
    private String name;
    private String description;
    private HobbyCategory category;
    private ActivityLevel activityLevel;
    private ComplexityLevel complexityLevel;
    private ImpactLevel impactOnWork;
    private Long yearsActive;
    private String whyInterested;
    private String favoriteAspect;
}
