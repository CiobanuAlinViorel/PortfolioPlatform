package com.example.portfolio.education.application;

import com.example.portfolio.achievement.domain.Achievement;
import com.example.portfolio.achievement.domain.AchievementType;
import com.example.portfolio.achievement.persistence.AchievementRepository;
import com.example.portfolio.education.domain.*;
import com.example.portfolio.education.dto.*;
import com.example.portfolio.education.mapper.EducationMapper;
import com.example.portfolio.education.persistence.CourseProjectRepository;
import com.example.portfolio.education.persistence.CourseRepository;
import com.example.portfolio.education.persistence.EducationAchievementRepository;
import com.example.portfolio.education.persistence.EducationRepository;
import com.example.portfolio.experience.domain.JobProjects;
import com.example.portfolio.experience.persistence.JobProjectsRepository;
import com.example.portfolio.hobby.domain.ComplexityLevel;
import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.profile.persistence.ProfileRepository;
import com.example.portfolio.projects.domain.Project;
import com.example.portfolio.projects.domain.ProjectStatus;
import com.example.portfolio.projects.persistence.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

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
    @Mock private CourseProjectRepository courseProjectRepository;
    @Mock private EducationAchievementRepository educationAchievementRepository;
    @Mock private AchievementRepository achievementRepository;
    @Mock private JobProjectsRepository jobProjectsRepository;
    @Mock private ProjectRepository projectRepository;
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

    private Project buildProject(long id, String title) {
        Project project = Project.builder()
                .profile(profile).title(title)
                .status(ProjectStatus.PRODUCTION).complexity(ComplexityLevel.ADVANCED)
                .build();
        ReflectionTestUtils.setField(project, "id", id);
        return project;
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
        when(courseProjectRepository.findAllByCourse(course)).thenReturn(List.of());
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

    // ── createEducation — courses & course projects ──────────────────────────

    @Test
    void createEducation_shouldPersistCoursesAndCourseProjects_whenProvided() {
        CourseProjectRequest pr = new CourseProjectRequest();
        pr.setProjectId(10L);
        pr.setGrade("A");
        CourseRequest cr = new CourseRequest();
        cr.setTitle("Algorithms");
        cr.setYear(2021);
        cr.setProjects(List.of(pr));

        CreateEducationRequest request = CreateEducationRequest.builder()
                .level(EducationLevel.BACHELOR).institution("University of Tech")
                .fieldOfStudy("Computer Science").startDate(LocalDate.of(2020, 9, 1))
                .status(EducationStatus.COMPLETED)
                .courses(List.of(cr)).build();

        Project project = buildProject(10L, "Sorting Visualizer");

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(educationRepository.save(any(Education.class))).thenReturn(education);
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(courseProjectRepository.findByProject(project)).thenReturn(Optional.empty());
        when(jobProjectsRepository.findByProject(project)).thenReturn(Optional.empty());
        when(courseProjectRepository.save(any(CourseProject.class))).thenAnswer(inv -> inv.getArgument(0));
        when(educationMapper.toEducationDto(education)).thenReturn(educationDto);

        educationService.createEducation(request);

        var courseCaptor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).save(courseCaptor.capture());
        assertThat(courseCaptor.getValue().getTitle()).isEqualTo("Algorithms");

        var projCaptor = ArgumentCaptor.forClass(CourseProject.class);
        verify(courseProjectRepository).save(projCaptor.capture());
        assertThat(projCaptor.getValue().getProject()).isEqualTo(project);
        assertThat(projCaptor.getValue().getGrade()).isEqualTo("A");
        assertThat(projCaptor.getValue().getContributionPercentage()).isEqualTo(100);
    }

    @Test
    void createEducation_shouldThrowIllegalArgumentException_whenProjectAlreadyLinkedToJobExperience() {
        CourseProjectRequest pr = new CourseProjectRequest();
        pr.setProjectId(10L);
        CourseRequest cr = new CourseRequest();
        cr.setTitle("Algorithms");
        cr.setProjects(List.of(pr));

        CreateEducationRequest request = CreateEducationRequest.builder()
                .level(EducationLevel.BACHELOR).institution("University of Tech")
                .fieldOfStudy("Computer Science").startDate(LocalDate.of(2020, 9, 1))
                .status(EducationStatus.COMPLETED)
                .courses(List.of(cr)).build();

        Project project = buildProject(10L, "Sorting Visualizer");
        JobProjects jobLink = JobProjects.builder().project(project).build();
        ReflectionTestUtils.setField(jobLink, "id", 77L);

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(educationRepository.save(any(Education.class))).thenReturn(education);
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(courseProjectRepository.findByProject(project)).thenReturn(Optional.empty());
        when(jobProjectsRepository.findByProject(project)).thenReturn(Optional.of(jobLink));

        assertThatThrownBy(() -> educationService.createEducation(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project is already linked to a job experience");

        verify(courseProjectRepository, never()).save(any());
    }

    @Test
    void createEducation_shouldThrowIllegalArgumentException_whenProjectAlreadyLinkedToCourse() {
        CourseProjectRequest pr = new CourseProjectRequest();
        pr.setProjectId(10L);
        CourseRequest cr = new CourseRequest();
        cr.setTitle("Algorithms");
        cr.setProjects(List.of(pr));

        CreateEducationRequest request = CreateEducationRequest.builder()
                .level(EducationLevel.BACHELOR).institution("University of Tech")
                .fieldOfStudy("Computer Science").startDate(LocalDate.of(2020, 9, 1))
                .status(EducationStatus.COMPLETED)
                .courses(List.of(cr)).build();

        Project project = buildProject(10L, "Sorting Visualizer");
        CourseProject existingLink = CourseProject.builder().project(project).build();
        ReflectionTestUtils.setField(existingLink, "id", 88L);

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(educationRepository.save(any(Education.class))).thenReturn(education);
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(courseProjectRepository.findByProject(project)).thenReturn(Optional.of(existingLink));

        assertThatThrownBy(() -> educationService.createEducation(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Project is already linked to a course");

        verify(courseProjectRepository, never()).save(any());
    }

    // ── updateEducation — courses & course projects sync ─────────────────────

    @Test
    void updateEducation_shouldUpdateExistingCourseAndCreateNewOne_whenSyncingCourses() {
        Course existingCourse = Course.builder().education(education).title("Old Title").build();
        ReflectionTestUtils.setField(existingCourse, "id", 10L);

        CourseRequest updateExisting = new CourseRequest();
        updateExisting.setId(10L);
        updateExisting.setTitle("Renamed");
        CourseRequest createNew = new CourseRequest();
        createNew.setTitle("Brand New Course");

        UpdateEducationRequest request = UpdateEducationRequest.builder()
                .courses(List.of(updateExisting, createNew)).build();

        when(educationRepository.findById(1L)).thenReturn(Optional.of(education));
        when(educationRepository.save(education)).thenReturn(education);
        when(courseRepository.findAllByEducationOrderByYearAscSemesterAsc(education)).thenReturn(List.of(existingCourse));
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));
        when(educationMapper.toEducationDto(education)).thenReturn(educationDto);

        educationService.updateEducation(1L, request);

        assertThat(existingCourse.getTitle()).isEqualTo("Renamed");
        verify(courseRepository, never()).delete(existingCourse);
        var captor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(Course::getTitle)
                .containsExactlyInAnyOrder("Renamed", "Brand New Course");
    }

    @Test
    void updateEducation_shouldDeleteCoursesNotInRequest() {
        Course toRemove = Course.builder().education(education).title("Obsolete").build();
        ReflectionTestUtils.setField(toRemove, "id", 20L);
        education.addCourse(toRemove);

        UpdateEducationRequest request = UpdateEducationRequest.builder().courses(List.of()).build();

        when(educationRepository.findById(1L)).thenReturn(Optional.of(education));
        when(educationRepository.save(education)).thenReturn(education);
        when(courseRepository.findAllByEducationOrderByYearAscSemesterAsc(education)).thenReturn(List.of(toRemove));
        when(educationMapper.toEducationDto(education)).thenReturn(educationDto);

        educationService.updateEducation(1L, request);

        verify(courseRepository).delete(toRemove);
        assertThat(education.getCourses()).doesNotContain(toRemove);
    }

    @Test
    void updateEducation_shouldSyncCourseProjects_updatingExistingAndDeletingMissing() {
        Course course = Course.builder().education(education).title("Algorithms").build();
        ReflectionTestUtils.setField(course, "id", 10L);

        Project project = buildProject(15L, "Sorting Visualizer");
        CourseProject keep = CourseProject.builder().course(course).project(project).grade("B").build();
        ReflectionTestUtils.setField(keep, "id", 30L);
        Project otherProject = buildProject(16L, "Other");
        CourseProject remove = CourseProject.builder().course(course).project(otherProject).build();
        ReflectionTestUtils.setField(remove, "id", 31L);

        CourseProjectRequest updateKeep = new CourseProjectRequest();
        updateKeep.setId(30L);
        updateKeep.setProjectId(15L);
        updateKeep.setGrade("A+");

        CourseRequest cr = new CourseRequest();
        cr.setId(10L);
        cr.setProjects(List.of(updateKeep));

        UpdateEducationRequest request = UpdateEducationRequest.builder().courses(List.of(cr)).build();

        when(educationRepository.findById(1L)).thenReturn(Optional.of(education));
        when(educationRepository.save(education)).thenReturn(education);
        when(courseRepository.findAllByEducationOrderByYearAscSemesterAsc(education)).thenReturn(List.of(course));
        when(courseRepository.save(course)).thenReturn(course);
        when(courseProjectRepository.findAllByCourse(course)).thenReturn(List.of(keep, remove));
        when(courseProjectRepository.save(any(CourseProject.class))).thenAnswer(inv -> inv.getArgument(0));
        when(educationMapper.toEducationDto(education)).thenReturn(educationDto);

        educationService.updateEducation(1L, request);

        assertThat(keep.getGrade()).isEqualTo("A+");
        verify(courseProjectRepository).delete(remove);
        verify(courseProjectRepository, never()).delete(keep);
        verify(projectRepository, never()).findById(any());
    }

    // ── getEducationById — course projects on read ────────────────────────────

    @Test
    void getEducationById_shouldReturnCourseWithLinkedProject() {
        Course course = Course.builder().education(education).title("Algorithms").build();
        ReflectionTestUtils.setField(course, "id", 10L);
        Project project = buildProject(15L, "Sorting Visualizer");
        CourseProject cp = CourseProject.builder().course(course).project(project).grade("A").contributionPercentage(80).build();
        ReflectionTestUtils.setField(cp, "id", 30L);

        CourseDto courseDto = CourseDto.builder().id(10L).title("Algorithms").build();
        ProjectInCourseDto projectDto = ProjectInCourseDto.builder().id(15L).title("Sorting Visualizer").build();

        when(educationRepository.findById(1L)).thenReturn(Optional.of(education));
        when(educationMapper.toEducationDetailDto(education)).thenReturn(educationDetailDto);
        when(courseRepository.findAllByEducationOrderByYearAscSemesterAsc(education)).thenReturn(List.of(course));
        when(educationMapper.toCourseDto(course)).thenReturn(courseDto);
        when(courseProjectRepository.findAllByCourse(course)).thenReturn(List.of(cp));
        when(educationMapper.toProjectInCourseDto(project)).thenReturn(projectDto);
        when(educationAchievementRepository.findAllByEducation(education)).thenReturn(List.of());

        EducationDetailDto result = educationService.getEducationById(1L);

        assertThat(result.getCourses()).hasSize(1);
        assertThat(result.getCourses().get(0).getProjects()).hasSize(1);
        assertThat(result.getCourses().get(0).getProjects().get(0).getGrade()).isEqualTo("A");
        assertThat(result.getCourses().get(0).getProjects().get(0).getProject().getTitle()).isEqualTo("Sorting Visualizer");
    }

    // ── createEducation — achievements ────────────────────────────────────────

    @Test
    void createEducation_shouldCreateNewAchievement_whenAchievementIdNotProvided() {
        EducationAchievementRequest ar = new EducationAchievementRequest();
        ar.setAchievementTitle("Dean's List");
        ar.setAchievementType(AchievementType.AWARD);
        ar.setAchievementDate(LocalDate.of(2021, 6, 1));
        ar.setTopic("Algorithms");
        ar.setGrade(new BigDecimal("10"));
        ar.setMaxGrade(new BigDecimal("10"));
        ar.setCredits(6);
        ar.setTeacherOrSupervisor("Dr. Smith");
        ar.setInstitutionName("University of Tech");
        ar.setSemester(1);
        ar.setAcademicYear(2021);

        CreateEducationRequest request = CreateEducationRequest.builder()
                .level(EducationLevel.BACHELOR).institution("University of Tech")
                .fieldOfStudy("Computer Science").startDate(LocalDate.of(2020, 9, 1))
                .status(EducationStatus.COMPLETED)
                .achievements(List.of(ar)).build();

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(educationRepository.save(any(Education.class))).thenReturn(education);
        when(achievementRepository.save(any(Achievement.class))).thenAnswer(inv -> inv.getArgument(0));
        when(educationAchievementRepository.save(any(EducationAchievement.class))).thenAnswer(inv -> inv.getArgument(0));
        when(educationMapper.toEducationDto(education)).thenReturn(educationDto);

        educationService.createEducation(request);

        var achievementCaptor = ArgumentCaptor.forClass(Achievement.class);
        verify(achievementRepository).save(achievementCaptor.capture());
        assertThat(achievementCaptor.getValue().getTitle()).isEqualTo("Dean's List");
        assertThat(achievementCaptor.getValue().getProfile()).isEqualTo(profile);

        var eaCaptor = ArgumentCaptor.forClass(EducationAchievement.class);
        verify(educationAchievementRepository).save(eaCaptor.capture());
        assertThat(eaCaptor.getValue().getTopic()).isEqualTo("Algorithms");
    }

    @Test
    void createEducation_shouldLinkExistingAchievement_whenAchievementIdProvided() {
        Achievement existingAchievement = Achievement.builder().profile(profile).title("Existing Award")
                .achievementType(AchievementType.AWARD).achievementDate(LocalDate.of(2020, 1, 1)).build();
        ReflectionTestUtils.setField(existingAchievement, "id", 5L);

        EducationAchievementRequest ar = new EducationAchievementRequest();
        ar.setAchievementId(5L);
        ar.setTopic("Algorithms");
        ar.setGrade(BigDecimal.TEN);
        ar.setMaxGrade(BigDecimal.TEN);
        ar.setCredits(6);
        ar.setTeacherOrSupervisor("Dr. Smith");
        ar.setInstitutionName("University of Tech");
        ar.setSemester(1);
        ar.setAcademicYear(2021);

        CreateEducationRequest request = CreateEducationRequest.builder()
                .level(EducationLevel.BACHELOR).institution("University of Tech")
                .fieldOfStudy("Computer Science").startDate(LocalDate.of(2020, 9, 1))
                .status(EducationStatus.COMPLETED)
                .achievements(List.of(ar)).build();

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(educationRepository.save(any(Education.class))).thenReturn(education);
        when(achievementRepository.findById(5L)).thenReturn(Optional.of(existingAchievement));
        when(educationAchievementRepository.save(any(EducationAchievement.class))).thenAnswer(inv -> inv.getArgument(0));
        when(educationMapper.toEducationDto(education)).thenReturn(educationDto);

        educationService.createEducation(request);

        verify(achievementRepository, never()).save(any());
        var eaCaptor = ArgumentCaptor.forClass(EducationAchievement.class);
        verify(educationAchievementRepository).save(eaCaptor.capture());
        assertThat(eaCaptor.getValue().getAchievement()).isEqualTo(existingAchievement);
    }

    // ── updateEducation — achievements sync ───────────────────────────────────

    @Test
    void updateEducation_shouldUpdateExistingAchievementAndCreateNewOne_whenSyncingAchievements() {
        Achievement achievement = Achievement.builder().profile(profile).title("Old Title")
                .achievementType(AchievementType.AWARD).achievementDate(LocalDate.of(2020, 1, 1)).build();
        EducationAchievement existingEa = EducationAchievement.builder()
                .education(education).achievement(achievement)
                .topic("Old topic").grade(BigDecimal.TEN).maxGrade(BigDecimal.TEN)
                .credits(6).teacherOrSupervisor("Dr. Smith").institutionName("University of Tech")
                .semester(1).academicYear(2021).build();
        ReflectionTestUtils.setField(existingEa, "id", 40L);

        EducationAchievementRequest updateExisting = new EducationAchievementRequest();
        updateExisting.setId(40L);
        updateExisting.setTopic("Updated topic");
        updateExisting.setAchievementTitle("New Title");

        EducationAchievementRequest createNew = new EducationAchievementRequest();
        createNew.setAchievementTitle("Second Award");
        createNew.setAchievementType(AchievementType.AWARD);
        createNew.setAchievementDate(LocalDate.of(2022, 1, 1));
        createNew.setTopic("Data Structures");
        createNew.setGrade(BigDecimal.TEN);
        createNew.setMaxGrade(BigDecimal.TEN);
        createNew.setCredits(5);
        createNew.setTeacherOrSupervisor("Dr. Doe");
        createNew.setInstitutionName("University of Tech");
        createNew.setSemester(2);
        createNew.setAcademicYear(2022);

        UpdateEducationRequest request = UpdateEducationRequest.builder()
                .achievements(List.of(updateExisting, createNew)).build();

        when(educationRepository.findById(1L)).thenReturn(Optional.of(education));
        when(educationRepository.save(education)).thenReturn(education);
        when(educationAchievementRepository.findAllByEducation(education)).thenReturn(List.of(existingEa));
        when(achievementRepository.save(any(Achievement.class))).thenAnswer(inv -> inv.getArgument(0));
        when(educationAchievementRepository.save(any(EducationAchievement.class))).thenAnswer(inv -> inv.getArgument(0));
        when(educationMapper.toEducationDto(education)).thenReturn(educationDto);

        educationService.updateEducation(1L, request);

        assertThat(existingEa.getTopic()).isEqualTo("Updated topic");
        assertThat(achievement.getTitle()).isEqualTo("New Title");
        verify(educationAchievementRepository, never()).delete(existingEa);

        var eaCaptor = ArgumentCaptor.forClass(EducationAchievement.class);
        verify(educationAchievementRepository, times(2)).save(eaCaptor.capture());
        assertThat(eaCaptor.getAllValues()).extracting(EducationAchievement::getTopic)
                .containsExactlyInAnyOrder("Updated topic", "Data Structures");
    }

    @Test
    void updateEducation_shouldDeleteAchievementsNotInRequest() {
        Achievement achievement = Achievement.builder().profile(profile).title("Old Title")
                .achievementType(AchievementType.AWARD).achievementDate(LocalDate.of(2020, 1, 1)).build();
        EducationAchievement toRemove = EducationAchievement.builder()
                .education(education).achievement(achievement)
                .topic("Obsolete").grade(BigDecimal.TEN).maxGrade(BigDecimal.TEN)
                .credits(6).teacherOrSupervisor("Dr. Smith").institutionName("University of Tech")
                .semester(1).academicYear(2021).build();
        ReflectionTestUtils.setField(toRemove, "id", 41L);
        education.addAchievement(toRemove);

        UpdateEducationRequest request = UpdateEducationRequest.builder().achievements(List.of()).build();

        when(educationRepository.findById(1L)).thenReturn(Optional.of(education));
        when(educationRepository.save(education)).thenReturn(education);
        when(educationAchievementRepository.findAllByEducation(education)).thenReturn(List.of(toRemove));
        when(educationMapper.toEducationDto(education)).thenReturn(educationDto);

        educationService.updateEducation(1L, request);

        verify(educationAchievementRepository).delete(toRemove);
        assertThat(education.getAchievements()).doesNotContain(toRemove);
    }
}
