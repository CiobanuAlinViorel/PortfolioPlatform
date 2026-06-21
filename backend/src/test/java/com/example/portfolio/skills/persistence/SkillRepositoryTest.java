package com.example.portfolio.skills.persistence;

import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.profile.persistence.ProfileRepository;
import com.example.portfolio.skills.domain.ProficiencyLevel;
import com.example.portfolio.skills.domain.Skill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:portfolio_skill;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;NON_KEYWORDS=YEAR",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SkillRepositoryTest {

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private ProfileRepository profileRepository;

    private Profile profile;

    @BeforeEach
    void setUp() {
        profile = profileRepository.save(Profile.builder().firstName("John").lastName("Doe").build());
    }

    private Skill skill(String name, ProficiencyLevel proficiency, Integer sortOrder) {
        return Skill.builder()
                .profile(profile)
                .name(name)
                .proficiency(proficiency)
                .sortOrder(sortOrder)
                .build();
    }

    // ── findTopByProfile ──────────────────────────────────────────────────────

    @Test
    void findTopByProfile_shouldReturnEmpty_whenNoSkillsExist() {
        List<Skill> result = skillRepository.findTopByProfile(profile, PageRequest.of(0, 3));

        assertThat(result).isEmpty();
    }

    @Test
    void findTopByProfile_shouldExcludeSkillsWithNullSortOrder() {
        skillRepository.save(skill("Java", ProficiencyLevel.EXPERT, null));
        skillRepository.save(skill("Python", ProficiencyLevel.INTERMEDIATE, null));

        List<Skill> result = skillRepository.findTopByProfile(profile, PageRequest.of(0, 3));

        assertThat(result).isEmpty();
    }

    @Test
    void findTopByProfile_shouldReturnOnlySkillsWithSortOrder() {
        skillRepository.save(skill("Java", ProficiencyLevel.EXPERT, 1));
        skillRepository.save(skill("Hidden", ProficiencyLevel.BEGINNER, null));

        List<Skill> result = skillRepository.findTopByProfile(profile, PageRequest.of(0, 3));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Java");
    }

    @Test
    void findTopByProfile_shouldReturnSkillsOrderedBySortOrderAscending() {
        skillRepository.save(skill("Docker", ProficiencyLevel.INTERMEDIATE, 3));
        skillRepository.save(skill("Java", ProficiencyLevel.EXPERT, 1));
        skillRepository.save(skill("Spring", ProficiencyLevel.ADVANCED, 2));

        List<Skill> result = skillRepository.findTopByProfile(profile, PageRequest.of(0, 3));

        assertThat(result).extracting(Skill::getName)
                .containsExactly("Java", "Spring", "Docker");
    }

    @Test
    void findTopByProfile_shouldReturnAtMostThreeSkills_whenMoreExistWithSortOrder() {
        for (int i = 1; i <= 5; i++) {
            skillRepository.save(skill("Skill" + i, ProficiencyLevel.INTERMEDIATE, i));
        }

        List<Skill> result = skillRepository.findTopByProfile(profile, PageRequest.of(0, 3));

        assertThat(result).hasSize(3);
        assertThat(result).extracting(Skill::getName)
                .containsExactly("Skill1", "Skill2", "Skill3");
    }

    @Test
    void findTopByProfile_shouldOnlyReturnSkillsBelongingToGivenProfile() {
        Profile otherProfile = profileRepository.save(Profile.builder().firstName("Jane").lastName("Doe").build());
        skillRepository.save(skill("Java", ProficiencyLevel.EXPERT, 1));
        skillRepository.save(Skill.builder().profile(otherProfile).name("Python").proficiency(ProficiencyLevel.EXPERT).sortOrder(1).build());

        List<Skill> result = skillRepository.findTopByProfile(profile, PageRequest.of(0, 3));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Java");
    }
}
