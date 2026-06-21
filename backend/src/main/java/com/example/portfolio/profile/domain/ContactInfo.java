package com.example.portfolio.profile.domain;

import com.example.portfolio.shared.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contact_info")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ContactInfo extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "personal_id", nullable = false)
    private Profile profile;

    @Column(unique = true, length = 150)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String github;

    @Column(length = 100)
    private String linkedin;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String country;
}
