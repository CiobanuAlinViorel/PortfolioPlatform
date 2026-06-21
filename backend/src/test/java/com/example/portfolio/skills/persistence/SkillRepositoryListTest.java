package com.example.portfolio.skills.persistence;

import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.profile.persistence.ProfileRepository;
import com.example.portfolio.skills.domain.ProficiencyLevel;
import com.example.portfolio.skills.domain.Skill;
import com.example.portfolio.skills.domain.SkillCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:portfolio_skill_list;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;NON_KEYWORDS=YEAR",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SkillRepositoryListTest {

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private SkillCategoryRepository skillCategoryRepository;

    private Profile profile;

    @BeforeEach
    void setUp() {
        profile = profileRepository.save(Profile.builder().firstName("John").lastName("Doe").build());
    }

    private Skill skill(String name, Integer sortOrder) {
        return Skill.builder()
                .profile(profile).name(name)
                .proficiency(ProficiencyLevel.INTERMEDIATE)
                .sortOrder(sortOrder)
                .build();
    }

    // ── findAllByProfileOrderBySortOrderAscNameAsc ────────────────────────────

    @Test
    void findAllByProfile_shouldReturnEmpty_whenNoSkillsExist() {
        List<Skill> result = skillRepository.findAllByProfileOrderBySortOrderAscNameAsc(profile);

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByProfile_shouldReturnAllSkills_includingThoseWithNullSortOrder() {
        skillRepository.save(skill("Java", 1));
        skillRepository.save(skill("Hidden", null));

        List<Skill> result = skillRepository.findAllByProfileOrderBySortOrderAscNameAsc(profile);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Skill::getName).containsExactlyInAnyOrder("Java", "Hidden");
    }

    @Test
    void findAllByProfile_shouldReturnSkillsOrderedBySortOrderThenName() {
        skillRepository.save(skill("Bravo", 2));
        skillRepository.save(skill("Alpha", 1));
        skillRepository.save(skill("Charlie", 3));

        List<Skill> result = skillRepository.findAllByProfileOrderBySortOrderAscNameAsc(profile);

        assertThat(result).extracting(Skill::getName)
                .containsExactly("Alpha", "Bravo", "Charlie");
    }

    @Test
    void findAllByProfile_shouldIncludeCategoryName_whenCategoryAssigned() {
        SkillCategory cat = skillCategoryRepository.save(SkillCategory.builder().name("Backend").build());
        skillRepository.save(Skill.builder()
                .profile(profile).name("Java").proficiency(ProficiencyLevel.EXPERT).category(cat).sortOrder(1).build());

        List<Skill> result = skillRepository.findAllByProfileOrderBySortOrderAscNameAsc(profile);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory().getName()).isEqualTo("Backend");
    }

    @Test
    void findAllByProfile_shouldOnlyReturnSkillsForGivenProfile() {
        Profile other = profileRepository.save(Profile.builder().firstName("Jane").lastName("Doe").build());
        skillRepository.save(skill("Mine", 1));
        skillRepository.save(Skill.builder().profile(other).name("Theirs").proficiency(ProficiencyLevel.BEGINNER).build());

        List<Skill> result = skillRepository.findAllByProfileOrderBySortOrderAscNameAsc(profile);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Mine");
    }
}
