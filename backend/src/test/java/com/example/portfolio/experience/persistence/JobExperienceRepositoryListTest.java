package com.example.portfolio.experience.persistence;

import com.example.portfolio.experience.domain.JobExperience;
import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.profile.persistence.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:portfolio_job_list;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;NON_KEYWORDS=YEAR",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JobExperienceRepositoryListTest {

    @Autowired
    private JobExperienceRepository jobExperienceRepository;

    @Autowired
    private ProfileRepository profileRepository;

    private Profile profile;

    @BeforeEach
    void setUp() {
        profile = profileRepository.save(Profile.builder().firstName("John").lastName("Doe").build());
    }

    private JobExperience job(String company, String role, LocalDate startDate, LocalDate endDate) {
        return JobExperience.builder()
                .profile(profile)
                .companyName(company)
                .role(role)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    // ── findAllByProfileOrderByStartDateDesc ──────────────────────────────────

    @Test
    void findAllByProfileOrderByStartDateDesc_shouldReturnEmpty_whenNoJobsExist() {
        List<JobExperience> result = jobExperienceRepository.findAllByProfileOrderByStartDateDesc(profile);

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByProfileOrderByStartDateDesc_shouldReturnSingleJob_whenOneExists() {
        jobExperienceRepository.save(job("Acme", "Dev", LocalDate.of(2022, 1, 1), null));

        List<JobExperience> result = jobExperienceRepository.findAllByProfileOrderByStartDateDesc(profile);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCompanyName()).isEqualTo("Acme");
    }

    @Test
    void findAllByProfileOrderByStartDateDesc_shouldReturnInDescendingOrder_whenMultipleExist() {
        jobExperienceRepository.save(job("Old Corp", "Junior Dev", LocalDate.of(2018, 1, 1), LocalDate.of(2020, 12, 31)));
        jobExperienceRepository.save(job("Recent Corp", "Senior Dev", LocalDate.of(2023, 3, 1), null));
        jobExperienceRepository.save(job("Middle Corp", "Dev", LocalDate.of(2021, 6, 1), LocalDate.of(2022, 12, 31)));

        List<JobExperience> result = jobExperienceRepository.findAllByProfileOrderByStartDateDesc(profile);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getCompanyName()).isEqualTo("Recent Corp");
        assertThat(result.get(1).getCompanyName()).isEqualTo("Middle Corp");
        assertThat(result.get(2).getCompanyName()).isEqualTo("Old Corp");
    }

    @Test
    void findAllByProfileOrderByStartDateDesc_shouldOnlyReturnJobsBelongingToGivenProfile() {
        Profile otherProfile = profileRepository.save(Profile.builder().firstName("Jane").lastName("Smith").build());
        jobExperienceRepository.save(job("My Corp", "Dev", LocalDate.of(2022, 1, 1), null));
        jobExperienceRepository.save(
                JobExperience.builder().profile(otherProfile).companyName("Other Corp").role("Manager")
                        .startDate(LocalDate.of(2023, 1, 1)).build()
        );

        List<JobExperience> result = jobExperienceRepository.findAllByProfileOrderByStartDateDesc(profile);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCompanyName()).isEqualTo("My Corp");
    }

    @Test
    void findAllByProfileOrderByStartDateDesc_shouldIncludeCurrentAndPastJobs() {
        jobExperienceRepository.save(job("Current Corp", "Lead Dev", LocalDate.of(2023, 6, 1), null));
        jobExperienceRepository.save(job("Past Corp", "Dev", LocalDate.of(2019, 1, 1), LocalDate.of(2022, 12, 31)));

        List<JobExperience> result = jobExperienceRepository.findAllByProfileOrderByStartDateDesc(profile);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEndDate()).isNull();
        assertThat(result.get(1).getEndDate()).isNotNull();
    }
}
