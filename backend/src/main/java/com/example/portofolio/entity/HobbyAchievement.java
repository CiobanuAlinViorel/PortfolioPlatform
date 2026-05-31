package com.example.portofolio.entity;

import com.example.portofolio.entity.base.BaseEntity;
import com.example.portofolio.entity.enums.ActivityType;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "hobby_achievement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(callSuper = true,onlyExplicitlyIncluded = true)

public class HobbyAchievement extends BaseEntity {

    @OneToOne
    @JoinColumn(name="achievement_id", nullable = false)
    private Achievement achievement;

    @ManyToOne
    @JoinColumn(name="hobby_id", nullable = false)
    private Hobby hobby;

    @ManyToOne
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(name="activity_type")
    @Enumerated(EnumType.STRING)
    private ActivityType activityType;

    private String result;

    @Column(name="hours_spent" , nullable = false)
    private Integer hoursSpent;

    @Column(name = "start_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date startDate;


    @Column(name = "end_date" , nullable = false)
    @Temporal(TemporalType.DATE)
    private Date endDate;

    @Column(name = "event_name")
    @Builder.Default
    private String eventName=null;

    @Column(name = "is_portfolio_relevant", nullable = false)
    private Boolean isPortfolioRelevant;

    @Column(name = "notes", nullable = false)
    private String notes;
}
