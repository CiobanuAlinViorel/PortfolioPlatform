package com.example.portfolio.profile.persistence;

import com.example.portfolio.profile.domain.Profile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:portfolio_profile;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;NON_KEYWORDS=YEAR",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProfileRepositoryTest {

    @Autowired
    private ProfileRepository profileRepository;

    // ── findFirstByOrderByIdAsc ───────────────────────────────────────────────

    @Test
    void findFirstByOrderByIdAsc_shouldReturnEmpty_whenNoProfileExists() {
        Optional<Profile> result = profileRepository.findFirstByOrderByIdAsc();

        assertThat(result).isEmpty();
    }

    @Test
    void findFirstByOrderByIdAsc_shouldReturnProfile_whenOneExists() {
        Profile profile = Profile.builder()
                .firstName("John")
                .lastName("Doe")
                .build();
        profileRepository.save(profile);

        Optional<Profile> result = profileRepository.findFirstByOrderByIdAsc();

        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("John");
        assertThat(result.get().getLastName()).isEqualTo("Doe");
    }

    @Test
    void findFirstByOrderByIdAsc_shouldReturnTheOneWithLowestId_whenMultipleExist() {
        Profile first = Profile.builder().firstName("Alice").lastName("Smith").build();
        Profile second = Profile.builder().firstName("Bob").lastName("Jones").build();
        profileRepository.save(first);
        profileRepository.save(second);

        Optional<Profile> result = profileRepository.findFirstByOrderByIdAsc();

        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("Alice");
    }

    @Test
    void findFirstByOrderByIdAsc_shouldReturnProfileWithAllOptionalFields_whenSet() {
        Profile profile = Profile.builder()
                .firstName("John")
                .lastName("Doe")
                .age(30)
                .imageLink("http://example.com/photo.jpg")
                .description("Portfolio owner")
                .build();
        profileRepository.save(profile);

        Optional<Profile> result = profileRepository.findFirstByOrderByIdAsc();

        assertThat(result).isPresent();
        assertThat(result.get().getAge()).isEqualTo(30);
        assertThat(result.get().getImageLink()).isEqualTo("http://example.com/photo.jpg");
        assertThat(result.get().getDescription()).isEqualTo("Portfolio owner");
    }
}
