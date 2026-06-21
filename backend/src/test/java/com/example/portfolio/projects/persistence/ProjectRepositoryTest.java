package com.example.portfolio.projects.persistence;

import com.example.portfolio.hobby.domain.ComplexityLevel;
import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.profile.persistence.ProfileRepository;
import com.example.portfolio.projects.domain.Project;
import com.example.portfolio.projects.domain.ProjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:portfolio_project;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;NON_KEYWORDS=YEAR",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProfileRepository profileRepository;

    private Profile profile;

    @BeforeEach
    void setUp() {
        profile = profileRepository.save(Profile.builder().firstName("John").lastName("Doe").build());
    }

    private Project project(String title, Integer sortOrder) {
        return Project.builder()
                .profile(profile)
                .title(title)
                .status(ProjectStatus.PRODUCTION)
                .complexity(ComplexityLevel.INTERMEDIATE)
                .sortOrder(sortOrder)
                .build();
    }

    // ── findTopByProfile ──────────────────────────────────────────────────────

    @Test
    void findTopByProfile_shouldReturnEmpty_whenNoProjectsExist() {
        List<Project> result = projectRepository.findTopByProfile(profile, PageRequest.of(0, 3));

        assertThat(result).isEmpty();
    }

    @Test
    void findTopByProfile_shouldExcludeProjectsWithNullSortOrder() {
        projectRepository.save(project("Hidden Project", null));
        projectRepository.save(project("Another Hidden", null));

        List<Project> result = projectRepository.findTopByProfile(profile, PageRequest.of(0, 3));

        assertThat(result).isEmpty();
    }

    @Test
    void findTopByProfile_shouldReturnOnlyProjectsWithSortOrder() {
        projectRepository.save(project("Featured", 1));
        projectRepository.save(project("Hidden", null));

        List<Project> result = projectRepository.findTopByProfile(profile, PageRequest.of(0, 3));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Featured");
    }

    @Test
    void findTopByProfile_shouldReturnProjectsOrderedBySortOrderAscending() {
        projectRepository.save(project("Third", 3));
        projectRepository.save(project("First", 1));
        projectRepository.save(project("Second", 2));

        List<Project> result = projectRepository.findTopByProfile(profile, PageRequest.of(0, 3));

        assertThat(result).extracting(Project::getTitle)
                .containsExactly("First", "Second", "Third");
    }

    @Test
    void findTopByProfile_shouldReturnAtMostThreeProjects_whenMoreExistWithSortOrder() {
        for (int i = 1; i <= 5; i++) {
            projectRepository.save(project("Project" + i, i));
        }

        List<Project> result = projectRepository.findTopByProfile(profile, PageRequest.of(0, 3));

        assertThat(result).hasSize(3);
        assertThat(result).extracting(Project::getTitle)
                .containsExactly("Project1", "Project2", "Project3");
    }

    @Test
    void findTopByProfile_shouldOnlyReturnProjectsBelongingToGivenProfile() {
        Profile otherProfile = profileRepository.save(Profile.builder().firstName("Jane").lastName("Doe").build());
        projectRepository.save(project("Mine", 1));
        projectRepository.save(Project.builder().profile(otherProfile).title("Theirs").status(ProjectStatus.PRODUCTION).complexity(ComplexityLevel.BEGINNER).sortOrder(1).build());

        List<Project> result = projectRepository.findTopByProfile(profile, PageRequest.of(0, 3));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Mine");
    }
}
