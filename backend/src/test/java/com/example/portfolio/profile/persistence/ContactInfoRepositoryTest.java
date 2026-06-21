package com.example.portfolio.profile.persistence;

import com.example.portfolio.profile.domain.ContactInfo;
import com.example.portfolio.profile.domain.Profile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:portfolio_contact;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;NON_KEYWORDS=YEAR",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ContactInfoRepositoryTest {

    @Autowired
    private ContactInfoRepository contactInfoRepository;

    @Autowired
    private ProfileRepository profileRepository;

    private Profile profile;

    @BeforeEach
    void setUp() {
        profile = profileRepository.save(Profile.builder().firstName("John").lastName("Doe").build());
    }

    // ── findByProfile ─────────────────────────────────────────────────────────

    @Test
    void findByProfile_shouldReturnEmpty_whenNoContactInfoExistsForProfile() {
        Optional<ContactInfo> result = contactInfoRepository.findByProfile(profile);

        assertThat(result).isEmpty();
    }

    @Test
    void findByProfile_shouldReturnContactInfo_whenExistsForProfile() {
        contactInfoRepository.save(
                ContactInfo.builder().profile(profile).email("john@test.com").github("john-gh").build()
        );

        Optional<ContactInfo> result = contactInfoRepository.findByProfile(profile);

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john@test.com");
        assertThat(result.get().getGithub()).isEqualTo("john-gh");
    }

    @Test
    void findByProfile_shouldOnlyReturnContactInfoBelongingToGivenProfile() {
        Profile otherProfile = profileRepository.save(Profile.builder().firstName("Jane").lastName("Doe").build());
        contactInfoRepository.save(ContactInfo.builder().profile(profile).email("john@test.com").build());
        contactInfoRepository.save(ContactInfo.builder().profile(otherProfile).email("jane@test.com").build());

        Optional<ContactInfo> result = contactInfoRepository.findByProfile(profile);

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john@test.com");
    }

    @Test
    void findByProfile_shouldPersistAllContactFields_whenSaved() {
        ContactInfo saved = contactInfoRepository.save(ContactInfo.builder()
                .profile(profile)
                .email("john@test.com")
                .phone("+40123456789")
                .github("john-gh")
                .linkedin("john-li")
                .city("Bucharest")
                .country("Romania")
                .build());

        Optional<ContactInfo> result = contactInfoRepository.findByProfile(profile);

        assertThat(result).isPresent();
        ContactInfo ci = result.get();
        assertThat(ci.getEmail()).isEqualTo("john@test.com");
        assertThat(ci.getPhone()).isEqualTo("+40123456789");
        assertThat(ci.getGithub()).isEqualTo("john-gh");
        assertThat(ci.getLinkedin()).isEqualTo("john-li");
        assertThat(ci.getCity()).isEqualTo("Bucharest");
        assertThat(ci.getCountry()).isEqualTo("Romania");
    }
}
