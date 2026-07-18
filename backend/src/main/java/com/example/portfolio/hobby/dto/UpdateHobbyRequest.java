package com.example.portfolio.hobby.dto;

import com.example.portfolio.experience.domain.ImpactLevel;
import com.example.portfolio.hobby.domain.ActivityLevel;
import com.example.portfolio.hobby.domain.ComplexityLevel;
import com.example.portfolio.hobby.domain.HobbyCategory;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    // Diffed by id against existing links: matched ids updated, unmatched ids removed, id-less entries created
    @Valid
    private List<HobbySkillRequest> skills;
}
