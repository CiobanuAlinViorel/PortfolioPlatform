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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:portfolio_job;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;NON_KEYWORDS=YEAR",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JobExperienceRepositoryTest {

    @Autowired
    private JobExperienceRepository jobExperienceRepository;

    @Autowired
    private ProfileRepository profileRepository;

    private Profile profile;

    @BeforeEach
    void setUp() {
        profile = profileRepository.save(Profile.builder().firstName("John").lastName("Doe").build());
    }

    private JobExperience job(String company, String role, LocalDate startDate) {
        return JobExperience.builder()
                .profile(profile)
                .companyName(company)
                .role(role)
                .startDate(startDate)
                .build();
    }

    // ── findFirstByProfileOrderByStartDateDesc ────────────────────────────────

    @Test
    void findFirstByProfileOrderByStartDateDesc_shouldReturnEmpty_whenNoJobsExist() {
        Optional<JobExperience> result = jobExperienceRepository.findFirstByProfileOrderByStartDateDesc(profile);

        assertThat(result).isEmpty();
    }

    @Test
    void findFirstByProfileOrderByStartDateDesc_shouldReturnTheOnlyJob_whenOneExists() {
        jobExperienceRepository.save(job("Acme", "Developer", LocalDate.of(2022, 1, 1)));

        Optional<JobExperience> result = jobExperienceRepository.findFirstByProfileOrderByStartDateDesc(profile);

        assertThat(result).isPresent();
        assertThat(result.get().getCompanyName()).isEqualTo("Acme");
        assertThat(result.get().getRole()).isEqualTo("Developer");
    }

    @Test
    void findFirstByProfileOrderByStartDateDesc_shouldReturnMostRecentByStartDate_whenMultipleJobsExist() {
        jobExperienceRepository.save(job("Old Corp", "Junior Dev", LocalDate.of(2018, 6, 1)));
        jobExperienceRepository.save(job("Recent Corp", "Senior Dev", LocalDate.of(2023, 3, 1)));
        jobExperienceRepository.save(job("Middle Corp", "Dev", LocalDate.of(2020, 9, 1)));

        Optional<JobExperience> result = jobExperienceRepository.findFirstByProfileOrderByStartDateDesc(profile);

        assertThat(result).isPresent();
        assertThat(result.get().getCompanyName()).isEqualTo("Recent Corp");
        assertThat(result.get().getStartDate()).isEqualTo(LocalDate.of(2023, 3, 1));
    }

    @Test
    void findFirstByProfileOrderByStartDateDesc_shouldReturnJobWithEndDate_whenSet() {
        jobExperienceRepository.save(
                JobExperience.builder()
                        .profile(profile)
                        .companyName("Closed Corp")
                        .role("Dev")
                        .startDate(LocalDate.of(2021, 1, 1))
                        .endDate(LocalDate.of(2022, 12, 31))
                        .build()
        );

        Optional<JobExperience> result = jobExperienceRepository.findFirstByProfileOrderByStartDateDesc(profile);

        assertThat(result).isPresent();
        assertThat(result.get().getEndDate()).isEqualTo(LocalDate.of(2022, 12, 31));
    }

    @Test
    void findFirstByProfileOrderByStartDateDesc_shouldOnlyReturnJobsBelongingToGivenProfile() {
        Profile otherProfile = profileRepository.save(Profile.builder().firstName("Jane").lastName("Doe").build());
        jobExperienceRepository.save(job("My Corp", "Dev", LocalDate.of(2022, 1, 1)));
        jobExperienceRepository.save(
                JobExperience.builder().profile(otherProfile).companyName("Other Corp").role("Manager")
                        .startDate(LocalDate.of(2023, 1, 1)).build()
        );

        Optional<JobExperience> result = jobExperienceRepository.findFirstByProfileOrderByStartDateDesc(profile);

        assertThat(result).isPresent();
        assertThat(result.get().getCompanyName()).isEqualTo("My Corp");
    }

    @Test
    void findFirstByProfileOrderByStartDateDesc_shouldReturnCurrentJob_whenNullEndDate() {
        jobExperienceRepository.save(job("Current Corp", "Lead Dev", LocalDate.of(2023, 6, 1)));

        Optional<JobExperience> result = jobExperienceRepository.findFirstByProfileOrderByStartDateDesc(profile);

        assertThat(result).isPresent();
        assertThat(result.get().getEndDate()).isNull();
        assertThat(result.get().getCompanyName()).isEqualTo("Current Corp");
    }
}
