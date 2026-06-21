package com.example.portfolio.experience.domain;

import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.shared.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Volunteer exp
 */
@Entity
@Table(name = "volunteer_experience")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = true,onlyExplicitlyIncluded = true)
public class VolunteerExperience extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    private Profile profile;

    @Column(nullable = false, length = 200)
    private String organization;

    @Column(nullable = false, length = 150)
    private String role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VolunteerType type;

    @Column(length = 150)
    private String location;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VolunteerStatus status;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "impact_description", columnDefinition = "TEXT")
    private String impactDescription;

    @Column(length = 300)
    private String website;

    @Column(name = "hours_per_week", precision = 4, scale = 1)
    private BigDecimal hoursPerWeek;

    @Column(name = "total_hours", precision = 8, scale = 1)
    private BigDecimal totalHours;

    // Relationships
    @OneToMany(mappedBy = "volunteerExperience", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private Set<VolunteerResponsibility> responsibilities = new HashSet<>();

    @OneToMany(mappedBy = "volunteerExperience", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private Set<VolunteerProject>  volunteerProjects = new HashSet<>();

    // COLLECTIONS METHODS
    public void addResponsibility(VolunteerResponsibility responsibility) {
        responsibilities.add(responsibility);
    }
    public void removeResponsibility(VolunteerResponsibility responsibility) {
        responsibilities.remove(responsibility);
    }

    public void addProject(VolunteerProject p){
        this.volunteerProjects.add(p);
    }

    public void removeProject(VolunteerProject p){
        this.volunteerProjects.remove(p);
    }
}