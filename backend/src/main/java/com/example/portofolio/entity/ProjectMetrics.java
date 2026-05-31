package com.example.portofolio.entity;

import com.example.portofolio.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Project metrics
 */
@Entity
@Table(name = "project_metrics")
@Getter @Setter 
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = true)
public class ProjectMetrics extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Builder.Default
    @Column(name = "users_count")
    private Long usersCount = 0L;

    @Column(name = "performance_score", length = 50)
    private String performanceScore;

    @Column(name = "code_quality_score", length = 50)
    private String codeQualityScore;

    @Builder.Default
    @Column(name = "lines_of_code")
    private Long linesOfCode = 0L;

    @Builder.Default
    @Column(name = "commits_count")
    private Integer commitsCount = 0;

    @Column(name = "test_coverage_percentage", precision = 5, scale = 2)
    private BigDecimal testCoveragePercentage;

    @Builder.Default
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated = LocalDateTime.now();
}