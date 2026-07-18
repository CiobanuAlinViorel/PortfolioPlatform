package com.example.portfolio.projects.dto;

import com.example.portfolio.projects.domain.DifficultyLevel;
import lombok.Data;

@Data
public class ProjectChallengeRequest {
    private Long id; // null = create new, present = update existing
    private String title;
    private String description;
    private String solution;
    private DifficultyLevel difficulty;
}
