package com.example.portfolio.projects.domain;

import com.example.portfolio.shared.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "project_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProjectCategory extends BaseEntity {
    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(mappedBy = "projectCategory", fetch = FetchType.LAZY)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private Set<Project> projects =  new HashSet<>();

    // COLLECTION METHODS
    public void addProject(Project p){
        this.projects.add(p);
    }

    public void removeProject(Project p){
        this.projects.remove(p);
    }
}
