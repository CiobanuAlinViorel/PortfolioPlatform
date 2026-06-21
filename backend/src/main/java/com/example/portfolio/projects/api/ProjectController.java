package com.example.portfolio.projects.api;

import com.example.portfolio.projects.application.ProjectService;
import com.example.portfolio.projects.dto.ProjectDetailDto;
import com.example.portfolio.projects.dto.ProjectListItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectListItemDto>> getProjects() {
        return ResponseEntity.ok(projectService.getProjectsList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDetailDto> getProject(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }
}
