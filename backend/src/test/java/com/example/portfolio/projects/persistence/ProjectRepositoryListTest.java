package com.example.portfolio.projects.persistence;

import com.example.portfolio.hobby.domain.ComplexityLevel;
import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.profile.persistence.ProfileRepository;
import com.example.portfolio.projects.domain.Project;
import com.example.portfolio.projects.domain.ProjectCategory;
import com.example.portfolio.projects.domain.ProjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:portfolio_project_list;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;NON_KEYWORDS=YEAR",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProjectRepositoryListTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ProjectCategoryRepository projectCategoryRepository;

    private Profile profile;

    @BeforeEach
    void setUp() {
        profile = profileRepository.save(Profile.builder().firstName("John").lastName("Doe").build());
    }

    private Project project(String title, Integer sortOrder) {
        return Project.builder()
                .profile(profile).title(title)
                .status(ProjectStatus.PRODUCTION).complexity(ComplexityLevel.ADVANCED)
                .sortOrder(sortOrder)
                .build();
    }

    // ── findAllByProfile ──────────────────────────────────────────────────────

    @Test
    void findAllByProfile_shouldReturnEmpty_whenNoProjectsExist() {
        List<Project> result = projectRepository.findAllByProfile(profile);

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByProfile_shouldReturnAllProjects_includingThoseWithNullSortOrder() {
        projectRepository.save(project("Featured", 1));
        projectRepository.save(project("Hidden", null));

        List<Project> result = projectRepository.findAllByProfile(profile);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Project::getTitle).containsExactlyInAnyOrder("Featured", "Hidden");
    }

    @Test
    void findAllByProfile_shouldReturnProjectsOrderedBySortOrderThenTitle() {
        projectRepository.save(project("Charlie", 3));
        projectRepository.save(project("Alpha", 1));
        projectRepository.save(project("Bravo", 2));

        List<Project> result = projectRepository.findAllByProfile(profile);

        assertThat(result).extracting(Project::getTitle)
                .containsExactly("Alpha", "Bravo", "Charlie");
    }

    @Test
    void findAllByProfile_shouldIncludeCategoryName_whenCategoryAssigned() {
        ProjectCategory cat = projectCategoryRepository.save(ProjectCategory.builder().name("Personal").build());
        projectRepository.save(Project.builder()
                .profile(profile).title("My Project").status(ProjectStatus.PRODUCTION)
                .complexity(ComplexityLevel.ADVANCED).projectCategory(cat).sortOrder(1).build());

        List<Project> result = projectRepository.findAllByProfile(profile);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProjectCategory().getName()).isEqualTo("Personal");
    }

    @Test
    void findAllByProfile_shouldOnlyReturnProjectsForGivenProfile() {
        Profile other = profileRepository.save(Profile.builder().firstName("Jane").lastName("Doe").build());
        projectRepository.save(project("Mine", 1));
        projectRepository.save(Project.builder().profile(other).title("Theirs")
                .status(ProjectStatus.PRODUCTION).complexity(ComplexityLevel.BEGINNER).build());

        List<Project> result = projectRepository.findAllByProfile(profile);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Mine");
    }
}
