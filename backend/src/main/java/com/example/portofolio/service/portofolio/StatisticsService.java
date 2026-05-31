package com.example.portofolio.service.portofolio;

import com.example.portofolio.dto.*;
import com.example.portofolio.entity.*;
import com.example.portofolio.entity.enums.EntityType;
import com.example.portofolio.entity.enums.EducationLevel;
import com.example.portofolio.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StatisticsService {

    private final ProjectRepository projectRepository;
    private final EducationRepository educationRepository;
    private final EntityMetadataRepository entityMetadataRepository;

    // ===== PROJECT STATISTICS =====

    public ProjectStatisticsDto getProjectStatistics(Long personalId) {
        log.debug("Getting project statistics for personal: {}", personalId);

        List<Project> projects = projectRepository.findByPersonalId(personalId);

        // Category distribution
        Map<String, Long> categoryDistribution = new HashMap<>();

        // Status distribution
        Map<String, Long> statusDistribution = projects.stream()
                .collect(Collectors.groupingBy(p -> p.getStatus().toString(), Collectors.counting()));

        // Featured count
        Long featuredCount = projects.stream()
                .filter(project -> entityMetadataRepository
                        .findByEntityTypeAndEntityId(EntityType.PROJECT, project.getId())
                        .map(EntityMetadata::getFeatured)
                        .orElse(false))
                .count();

        return ProjectStatisticsDto.builder()
                .totalProjects((long) projects.size())
                .categoryDistribution(categoryDistribution)
                .statusDistribution(statusDistribution)
                .featuredCount(featuredCount)
                .currentYear(LocalDate.now().getYear())
                .build();
    }

    // ===== EDUCATION STATISTICS =====

    public EducationStatisticsDto getEducationStatistics(Long personalId) {
        log.debug("Getting education statistics for personal: {}", personalId);

        List<Education> educationList = educationRepository.findByPersonalId(personalId);

        // Basic counts
        Long totalEducation = (long) educationList.size();
        Long ongoingCount = educationList.stream()
                .filter(e -> e.getEndDate() == null)
                .count();
        Long completedCount = educationList.stream()
                .filter(e -> e.getEndDate() != null)
                .count();

        // Institution distribution
        Map<String, Long> institutionDistribution = educationList.stream()
                .collect(Collectors.groupingBy(Education::getInstitution, Collectors.counting()));

        // Field distribution
        Map<String, Long> fieldDistribution = educationList.stream()
                .collect(Collectors.groupingBy(Education::getFieldOfStudy, Collectors.counting()));

        // Level distribution
        Map<EducationLevel, Long> levelDistribution = educationList.stream()
                .collect(Collectors.groupingBy(Education::getLevel, Collectors.counting()));

        // Featured count
        Long featuredCount = educationList.stream()
                .filter(education -> entityMetadataRepository
                        .findByEntityTypeAndEntityId(EntityType.EDUCATION, education.getId())
                        .map(EntityMetadata::getFeatured)
                        .orElse(false))
                .count();

        return EducationStatisticsDto.builder()
                .totalEducation(totalEducation)
                .ongoingCount(ongoingCount)
                .completedCount(completedCount)
                .institutionDistribution(institutionDistribution)
                .fieldDistribution(fieldDistribution)
                .levelDistribution(levelDistribution)
                .featuredCount(featuredCount)
                .build();
    }


}