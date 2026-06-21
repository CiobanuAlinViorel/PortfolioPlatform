package com.example.portfolio.projects.persistence;

import com.example.portfolio.hobby.domain.ComplexityLevel;
import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.profile.persistence.ProfileRepository;
import com.example.portfolio.projects.domain.Project;
import com.example.portfolio.projects.domain.ProjectSkill;
import com.example.portfolio.projects.domain.ProjectStatus;
import com.example.portfolio.skills.domain.ProficiencyLevel;
import com.example.portfolio.skills.domain.Skill;
import com.example.portfolio.skills.persistence.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:portfolio_project_skill;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;NON_KEYWORDS=YEAR",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProjectSkillRepositoryTest {

    @Autowired
    private ProjectSkillRepository projectSkillRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private ProfileRepository profileRepository;

    private Profile profile;
    private Skill skill;
    private Project project;

    @BeforeEach
    void setUp() {
        profile = profileRepository.save(Profile.builder().firstName("John").lastName("Doe").build());
        skill = skillRepository.save(Skill.builder()
                .profile(profile).name("Java").proficiency(ProficiencyLevel.EXPERT).build());
        project = projectRepository.save(Project.builder()
                .profile(profile).title("Portfolio API")
                .status(ProjectStatus.PRODUCTION).complexity(ComplexityLevel.ADVANCED).build());
    }

    // ── findAllBySkill ────────────────────────────────────────────────────────

    @Test
    void findAllBySkill_shouldReturnEmpty_whenNoProjectsLinkedToSkill() {
        List<ProjectSkill> result = projectSkillRepository.findAllBySkill(skill);

        assertThat(result).isEmpty();
    }

    @Test
    void findAllBySkill_shouldReturnProjectSkill_whenOneLinked() {
        projectSkillRepository.save(
                ProjectSkill.builder().skill(skill).project(project).usage(new BigDecimal("0.8")).build()
        );

        List<ProjectSkill> result = projectSkillRepository.findAllBySkill(skill);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProject().getTitle()).isEqualTo("Portfolio API");
    }

    @Test
    void findAllBySkill_shouldReturnCorrectUsageValue_whenProjectSkillSaved() {
        projectSkillRepository.save(
                ProjectSkill.builder().skill(skill).project(project).usage(new BigDecimal("0.75")).build()
        );

        List<ProjectSkill> result = projectSkillRepository.findAllBySkill(skill);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsage()).isEqualByComparingTo(new BigDecimal("0.75"));
    }

    @Test
    void findAllBySkill_shouldReturnMultipleProjects_whenSameSkillUsedInSeveralProjects() {
        Project project2 = projectRepository.save(Project.builder()
                .profile(profile).title("Blog API")
                .status(ProjectStatus.DEVELOPMENT).complexity(ComplexityLevel.INTERMEDIATE).build());

        projectSkillRepository.save(ProjectSkill.builder().skill(skill).project(project).usage(new BigDecimal("0.9")).build());
        projectSkillRepository.save(ProjectSkill.builder().skill(skill).project(project2).usage(new BigDecimal("0.5")).build());

        List<ProjectSkill> result = projectSkillRepository.findAllBySkill(skill);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ps -> ps.getProject().getTitle())
                .containsExactlyInAnyOrder("Portfolio API", "Blog API");
    }

    @Test
    void findAllBySkill_shouldOnlyReturnProjectsLinkedToGivenSkill() {
        Skill otherSkill = skillRepository.save(Skill.builder()
                .profile(profile).name("Spring").proficiency(ProficiencyLevel.ADVANCED).build());
        projectSkillRepository.save(ProjectSkill.builder().skill(skill).project(project).usage(new BigDecimal("0.8")).build());
        projectSkillRepository.save(ProjectSkill.builder().skill(otherSkill).project(project).usage(new BigDecimal("0.3")).build());

        List<ProjectSkill> result = projectSkillRepository.findAllBySkill(skill);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSkill().getName()).isEqualTo("Java");
    }
}
