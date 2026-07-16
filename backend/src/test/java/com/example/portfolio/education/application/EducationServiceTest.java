package com.example.portfolio.education.application;

import com.example.portfolio.achievement.domain.Achievement;
import com.example.portfolio.education.domain.*;
import com.example.portfolio.education.dto.*;
import com.example.portfolio.education.mapper.EducationMapper;
import com.example.portfolio.education.persistence.CourseRepository;
import com.example.portfolio.education.persistence.EducationAchievementRepository;
import com.example.portfolio.education.persistence.EducationRepository;
import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.profile.persistence.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EducationServiceTest {

    @Mock private EducationRepository educationRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private EducationAchievementRepository educationAchievementRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private EducationMapper educationMapper;

    @InjectMocks
    private EducationService educationService;

    private Profile profile;
    private Education education;
    private EducationDto educationDto;
    private EducationDetailDto educationDetailDto;

    @BeforeEach
    void setUp() {
        profile = Profile.builder().firstName("John").lastName("Doe").build();

        education = Education.builder()
                .profile(profile)
                .level(EducationLevel.BACHELOR)
                .institution("University of Tech")
                .fieldOfStudy("Computer Science")
                .startDate(LocalDate.of(2020, 9, 1))
                .endDate(LocalDate.of(2024, 6, 30))
                .status(EducationStatus.COMPLETED)
                .gpa("9.5")
                .build();

        educationDto = EducationDto.builder()
                .id(1L)
                .level(EducationLevel.BACHELOR)
                .institution("University of Tech")
                .fieldOfStudy("Computer Science")
                .status(EducationStatus.COMPLETED)
                .build();

        educationDetailDto = EducationDetailDto.builder()
                .id(1L)
                .level(EducationLevel.BACHELOR)
                .institution("University of Tech")
                .build();
    }

    // ── getEducations ─────────────────────────────────────────────────────────

    @Test
    void getEducations_shouldReturnMappedList_whenNoFilters() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(educationRepository.findAll(any(Specification.class))).thenReturn(List.of(education));
        when(educationMapper.toEducationDto(education)).thenReturn(educationDto);

        List<EducationDto> result = educationService.getEducations(null, null);

        assertThat(result).containsExactly(educationDto);
    }

    @Test
    void getEducations_shouldApplySpec_whenLevelFilterProvided() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(educationRepository.findAll(any(Specification.class))).thenReturn(List.of(education));
        when(educationMapper.toEducationDto(education)).thenReturn(educationDto);

        List<EducationDto> result = educationService.getEducations(EducationLevel.BACHELOR, null);

        assertThat(result).hasSize(1);
        verify(educationRepository).findAll(any(Specification.class));
    }

    @Test
    void getEducations_shouldApplySpec_whenStatusFilterProvided() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(educationRepository.findAll(any(Specification.class))).thenReturn(List.of());

        List<EducationDto> result = educationService.getEducations(null, EducationStatus.ONGOING);

        assertThat(result).isEmpty();
        verify(educationMapper, never()).toEducationDto(any());
    }

    @Test
    void getEducations_shouldApplyBothSpecs_whenBothFiltersProvided() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(educationRepository.findAll(any(Specification.class))).thenReturn(List.of(education));
        when(educationMapper.toEducationDto(education)).thenReturn(educationDto);

        List<EducationDto> result = educationService.getEducations(EducationLevel.BACHELOR, EducationStatus.COMPLETED);

        assertThat(result).hasSize(1);
    }

    @Test
    void getEducations_shouldThrowNoSuchElementException_whenProfileNotFound() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> educationService.getEducations(null, null))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Profile not found");

        verifyNoInteractions(educationRepository, educationMapper);
    }

    // ── getEducationById ──────────────────────────────────────────────────────

    @Test
    void getEducationById_shouldReturnDetailWithCoursesAndAchievements_whenFound() {
        Course course = Course.builder()
                .education(education).title("Algorithms").grade("A").year(2021).build();
        CourseDto courseDto = CourseDto.builder().id(1L).title("Algorithms").grade("A").year(2021).build();

        Achievement achievement = Achievement.builder().title("Best Student Award").build();
        EducationAchievement ea = EducationAchievement.builder()
                .education(education).achievement(achievement)
                .topic("Algorithms").grade(new BigDecimal("10")).maxGrade(new BigDecimal("10"))
                .credits(6).teacherOrSupervisor("Dr. Smith").institutionName("University of Tech")
                .semester(1).academicYear(2021).build();
        AchievementInEducationDto achievementDto = AchievementInEducationDto.builder()
                .achievementId(1L).achievementTitle("Best Student Award").topic("Algorithms").build();

        when(educationRepository.findById(1L)).thenReturn(Optional.of(education));
        when(educationMapper.toEducationDetailDto(education)).thenReturn(educationDetailDto);
        when(courseRepository.findAllByEducationOrderByYearAscSemesterAsc(education)).thenReturn(List.of(course));
        when(educationMapper.toCourseDto(course)).thenReturn(courseDto);
        when(educationAchievementRepository.findAllByEducation(education)).thenReturn(List.of(ea));
        when(educationMapper.toAchievementInEducationDto(ea)).thenReturn(achievementDto);

        EducationDetailDto result = educationService.getEducationById(1L);

        assertThat(result.getCourses()).containsExactly(courseDto);
        assertThat(result.getAchievements()).containsExactly(achievementDto);
    }

    @Test
    void getEducationById_shouldReturnEmptyCoursesAndAchievements_whenNoneExist() {
        when(educationRepository.findById(1L)).thenReturn(Optional.of(education));
        when(educationMapper.toEducationDetailDto(education)).thenReturn(educationDetailDto);
        when(courseRepository.findAllByEducationOrderByYearAscSemesterAsc(education)).thenReturn(List.of());
        when(educationAchievementRepository.findAllByEducation(education)).thenReturn(List.of());

        EducationDetailDto result = educationService.getEducationById(1L);

        assertThat(result.getCourses()).isEmpty();
        assertThat(result.getAchievements()).isEmpty();
        verify(educationMapper, never()).toCourseDto(any());
        verify(educationMapper, never()).toAchievementInEducationDto(any());
    }

    @Test
    void getEducationById_shouldThrowNoSuchElementException_whenNotFound() {
        when(educationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> educationService.getEducationById(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Education not found");

        verifyNoInteractions(courseRepository, educationAchievementRepository, educationMapper);
    }

    // ── createEducation ───────────────────────────────────────────────────────

    @Test
    void createEducation_shouldSaveAndReturnDto_whenProfileExists() {
        CreateEducationRequest request = CreateEducationRequest.builder()
                .level(EducationLevel.BACHELOR).institution("University of Tech")
                .fieldOfStudy("Computer Science").startDate(LocalDate.of(2020, 9, 1))
                .status(EducationStatus.COMPLETED).build();

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(educationRepository.save(any(Education.class))).thenReturn(education);
        when(educationMapper.toEducationDto(education)).thenReturn(educationDto);

        EducationDto result = educationService.createEducation(request);

        assertThat(result).isEqualTo(educationDto);
        verify(educationRepository).save(any(Education.class));
    }

    @Test
    void createEducation_shouldSetProfileOnEducation() {
        CreateEducationRequest request = CreateEducationRequest.builder()
                .level(EducationLevel.MASTER).institution("MIT")
                .fieldOfStudy("AI").startDate(LocalDate.of(2024, 9, 1))
                .status(EducationStatus.ONGOING).build();

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(educationRepository.save(any(Education.class))).thenReturn(education);
        when(educationMapper.toEducationDto(any())).thenReturn(educationDto);

        educationService.createEducation(request);

        verify(educationRepository).save(argThat(e -> e.getProfile() == profile));
    }

    @Test
    void createEducation_shouldThrowNoSuchElementException_whenProfileNotFound() {
        CreateEducationRequest request = CreateEducationRequest.builder()
                .level(EducationLevel.BACHELOR).institution("University")
                .fieldOfStudy("CS").startDate(LocalDate.now())
                .status(EducationStatus.ONGOING).build();
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> educationService.createEducation(request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Profile not found");

        verify(educationRepository, never()).save(any());
    }

    // ── updateEducation ───────────────────────────────────────────────────────

    @Test
    void updateEducation_shouldUpdateAndReturnDto_whenFound() {
        UpdateEducationRequest request = UpdateEducationRequest.builder()
                .gpa("10.0").status(EducationStatus.COMPLETED).build();

        when(educationRepository.findById(1L)).thenReturn(Optional.of(education));
        when(educationRepository.save(education)).thenReturn(education);
        when(educationMapper.toEducationDto(education)).thenReturn(educationDto);

        EducationDto result = educationService.updateEducation(1L, request);

        assertThat(result).isEqualTo(educationDto);
        verify(educationMapper).updateEducation(request, education);
        verify(educationRepository).save(education);
    }

    @Test
    void updateEducation_shouldThrowNoSuchElementException_whenNotFound() {
        UpdateEducationRequest request = UpdateEducationRequest.builder().gpa("9").build();
        when(educationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> educationService.updateEducation(99L, request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Education not found");

        verify(educationRepository, never()).save(any());
    }

    // ── deleteEducation ───────────────────────────────────────────────────────

    @Test
    void deleteEducation_shouldDeleteById_whenFound() {
        when(educationRepository.existsById(1L)).thenReturn(true);

        educationService.deleteEducation(1L);

        verify(educationRepository).deleteById(1L);
    }

    @Test
    void deleteEducation_shouldThrowNoSuchElementException_whenNotFound() {
        when(educationRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> educationService.deleteEducation(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Education not found");

        verify(educationRepository, never()).deleteById(any());
    }
}
