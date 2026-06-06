package com.example.portfolio.profile.domain;

import com.example.portfolio.certificate.domain.Certificate;
import com.example.portfolio.projects.domain.Project;
import com.example.portfolio.skills.domain.Skill;
import com.example.portfolio.shared.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * The main entity - the person
 */
@Entity
@Table(name = "personal", indexes = {
        @Index(name = "idx_personal_name", columnList = "first_name, last_name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Personal extends BaseEntity {

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    private Integer age;

    @Column(name = "image_link", length = 500)
    private String imageLink;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Relationships
    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "personal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Skill> skills = new HashSet<>();

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "personal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Project> projects = new HashSet<>();

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "personal", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Certificate> certificates = new HashSet<>();

    @OneToOne(mappedBy = "personal")
    private ContactInfo contactInfo;

    // FUNCTIONS TO POPULATE THE COLLECTIONS

    public void addSkill(Skill skill){
        skills.add(skill);
    }

    public void removeSkill(Skill skill){
        skills.remove(skill);
    }

    public void addProject(Project project){
        projects.add(project);
    }

    public void removeProject(Project project){
        projects.remove(project);
    }

    public void addCertificate(Certificate certificate){
        certificates.add(certificate);
    }

    public void removeCertificate(Certificate certificate){
        certificates.remove(certificate);
    }
}
