package com.example.portfolio.projects.dto;

import com.example.portfolio.projects.domain.DifficultyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectChallengeDto {
    private Long id;
    private String title;
    private String description;
    private String solution;
    private DifficultyLevel difficulty;
}
