package com.example.portofolio.repository;

import com.example.portofolio.entity.*;
import com.example.portofolio.entity.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Personal Repository Tests - Fixed")
class PersonalRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PersonalRepository personalRepository;

    private Personal testPersonal;
    private ContactInfo testContactInfo;
    private ContactLocation testContactLocation;
    private SkillCategory testSkillCategory;
    private Skill testSkill;

    @BeforeEach
    void setUp() {
        setupTestData();
        persistTestData();
    }

    private void setupTestData() {
        // Personal entity
        testPersonal = Personal.builder()
                .firstName("John")
                .lastName("Doe")
                .age(30)
                .imageLink("https://example.com/image.jpg")
                .description("Software Developer with 5 years experience")
                .build();

        // ContactLocation setup
        testContactLocation = ContactLocation.builder()
                .name("Home Office")
                .address("123 Main Street")
                .city("New York")
                .country("USA")
                .latitude(new BigDecimal("40.7128"))
                .longitude(new BigDecimal("-74.0060"))
                .timezone("EST")
                .workingHours("9AM - 5PM")
                .build();

        // ContactInfo setup
        testContactInfo = ContactInfo.builder()
                .personal(testPersonal)
                .email("john.doe@example.com")
                .phone("+1234567890")
                .github("johndoe")
                .linkedin("john-doe")
                .contactLocation(testContactLocation)
                .build();

        // SkillCategory setup
        testSkillCategory = SkillCategory.builder()
                .name("Programming Languages")
                .description("Various programming languages")
                .sortOrder(1)
                .build();

        // Skill setup
        testSkill = Skill.builder()
                .personal(testPersonal)
                .name("Java")
                .category(testSkillCategory)
                .proficiency(ProficiencyLevel.EXPERT)
                .level(85)
                .yearsOfExperience(new BigDecimal("5.0"))
                .description("Experienced Java developer")
                .lastUsedDate(LocalDate.now())
                .trending(true)
                .hasCertification(true)
                .learning(false)
                .build();

        // Set up relationships
        testContactLocation.setContactInfo(testContactInfo);
        testPersonal.getSkills().add(testSkill);
    }

    private void persistTestData() {
        // Persist in correct order
        entityManager.persist(testSkillCategory);
        entityManager.persist(testPersonal);
        entityManager.flush();
    }

    @Test
    @DisplayName("Should save and find Personal by ID")
    void shouldSaveAndFindPersonalById() {
        // Given - data is already persisted in setUp()

        // When
        Optional<Personal> found = personalRepository.findById(testPersonal.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("John");
        assertThat(found.get().getLastName()).isEqualTo("Doe");
        assertThat(found.get().getAge()).isEqualTo(30);
    }

    @Test
    @DisplayName("Should find Personal by first name and last name")
    void shouldFindByFirstNameAndLastName() {
        // When
        Optional<Personal> found = personalRepository.findByFirstNameAndLastName("John", "Doe");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("John");
        assertThat(found.get().getLastName()).isEqualTo("Doe");
    }

    @Test
    @DisplayName("Should return empty when finding by non-existent names")
    void shouldReturnEmptyForNonExistentNames() {
        // When
        Optional<Personal> found = personalRepository.findByFirstNameAndLastName("Jane", "Smith");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find Personal with ContactInfo using fetch join")
    void shouldFindByIdWithContactInfo() {
        // When
        Optional<Personal> found = personalRepository.findByIdWithContactInfo(testPersonal.getId());

        // Then
        assertThat(found).isPresent();

        Personal personal = found.get();

        assertThat(personal.getContactInfo()).isNotNull();
        assertThat(personal.getContactInfo().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(personal.getContactInfo().getContactLocation()).isNotNull();
        assertThat(personal.getContactInfo().getContactLocation().getCity()).isEqualTo("New York");
    }

    @Test
    @DisplayName("Should find Personal with Skills using fetch join")
    void shouldFindByIdWithSkills() {
        // When
        Optional<Personal> found = personalRepository.findByIdWithSkills(testPersonal.getId());

        // Then
        assertThat(found).isPresent();

        Personal personal = found.get();
        assertThat(personal.getSkills()).isNotEmpty();
        assertThat(personal.getSkills()).hasSize(1);

        Skill skill = personal.getSkills().iterator().next();
        assertThat(skill.getName()).isEqualTo("Java");
        assertThat(skill.getProficiency()).isEqualTo(ProficiencyLevel.EXPERT);
        assertThat(skill.getCategory().getName()).isEqualTo("Programming Languages");
    }

    @Test
    @DisplayName("Should count total Personals")
    void shouldCountTotalPersonals() {
        // When
        Long count = personalRepository.countTotalPersonals();

        // Then
        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should check existence by first name and last name")
    void shouldCheckExistenceByNames() {
        // When & Then
        assertThat(personalRepository.existsByFirstNameAndLastName("John", "Doe")).isTrue();
        assertThat(personalRepository.existsByFirstNameAndLastName("Jane", "Smith")).isFalse();
    }
}