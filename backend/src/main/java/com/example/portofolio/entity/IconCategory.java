package com.example.portofolio.entity;

import com.example.portofolio.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "icon_categories")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = true,onlyExplicitlyIncluded = true)
public class IconCategory extends BaseEntity {
    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(mappedBy = "iconCategory", fetch = FetchType.LAZY)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private Set<Icon> icons = new HashSet<>();

    // COLLECTION METHODS
    public void addIcon (Icon i) {
        this.icons.add(i);
    }

    public void removeIcon (Icon i) {
        this.icons.remove(i);
    }
}
