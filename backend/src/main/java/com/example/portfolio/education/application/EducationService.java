package com.example.portfolio.education.application;

import com.example.portfolio.achievement.domain.Achievement;
import com.example.portfolio.achievement.domain.RecognitionLevel;
import com.example.portfolio.achievement.persistence.AchievementRepository;
import com.example.portfolio.education.domain.Course;
import com.example.portfolio.education.domain.CourseProject;
import com.example.portfolio.education.domain.Education;
import com.example.portfolio.education.domain.EducationAchievement;
import com.example.portfolio.education.domain.EducationLevel;
import com.example.portfolio.education.domain.EducationStatus;
import com.example.portfolio.education.dto.*;
import com.example.portfolio.education.mapper.EducationMapper;
import com.example.portfolio.education.persistence.CourseProjectRepository;
import com.example.portfolio.education.persistence.CourseRepository;
import com.example.portfolio.education.persistence.EducationAchievementRepository;
import com.example.portfolio.education.persistence.EducationRepository;
import com.example.portfolio.experience.persistence.JobProjectsRepository;
import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.profile.persistence.ProfileRepository;
import com.example.portfolio.projects.domain.Project;
import com.example.portfolio.projects.persistence.ProjectRepository;
import com.example.portfolio.shared.base.BaseEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EducationService {

    private final EducationRepository educationRepository;
    private final CourseRepository courseRepository;
    private final CourseProjectRepository courseProjectRepository;
    private final EducationAchievementRepository educationAchievementRepository;
    private final AchievementRepository achievementRepository;
    private final JobProjectsRepository jobProjectsRepository;
    private final ProjectRepository projectRepository;
    private final ProfileRepository profileRepository;
    private final EducationMapper educationMapper;

    @Transactional
    public List<EducationDto> getEducations(EducationLevel level, EducationStatus status) {
        Profile profile = profileRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new NoSuchElementException("Profile not found"));

        List<Specification<Education>> specs = new ArrayList<>();
        specs.add((root, query, cb) -> cb.equal(root.get("profile"), profile));

        if (level != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("level"), level));
        }
        if (status != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        return educationRepository.findAll(Specification.allOf(specs))
                .stream()
                .map(educationMapper::toEducationDto)
                .toList();
    }

    @Transactional
    public EducationDetailDto getEducationById(Long id) {
        Education education = requireEducation(id);

        EducationDetailDto dto = educationMapper.toEducationDetailDto(education);

        List<CourseDto> courses = courseRepository
                .findAllByEducationOrderByYearAscSemesterAsc(education)
                .stream()
                .map(this::toCourseDtoWithProjects)
                .toList();

        List<AchievementInEducationDto> achievements = educationAchievementRepository
                .findAllByEducation(education)
                .stream()
                .map(educationMapper::toAchievementInEducationDto)
                .toList();

        dto.setCourses(courses);
        dto.setAchievements(achievements);
        return dto;
    }

    @Transactional
    public EducationDto createEducation(CreateEducationRequest request) {
        Profile profile = profileRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new NoSuchElementException("Profile not found"));

        Education education = Education.builder()
                .profile(profile)
                .level(request.getLevel())
                .institution(request.getInstitution())
                .degree(request.getDegree())
                .fieldOfStudy(request.getFieldOfStudy())
                .location(request.getLocation())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus())
                .gpa(request.getGpa())
                .description(request.getDescription())
                .build();

        education = educationRepository.save(education);

        if (request.getCourses() != null) {
            for (CourseRequest cr : request.getCourses()) {
                Course course = courseRepository.save(buildCourse(cr, education));
                education.addCourse(course);

                if (cr.getProjects() != null) {
                    for (CourseProjectRequest pr : cr.getProjects()) {
                        course.addCourseProject(courseProjectRepository.save(buildCourseProject(pr, course, null)));
                    }
                }
            }
        }

        if (request.getAchievements() != null) {
            for (EducationAchievementRequest ar : request.getAchievements()) {
                Achievement achievement = resolveOrCreateAchievement(ar, profile);
                education.addAchievement(
                        educationAchievementRepository.save(buildEducationAchievement(ar, education, achievement)));
            }
        }

        return educationMapper.toEducationDto(education);
    }

    @Transactional
    public EducationDto updateEducation(Long id, UpdateEducationRequest request) {
        Education education = requireEducation(id);
        Profile profile = education.getProfile();

        educationMapper.updateEducation(request, education);
        educationRepository.save(education);

        if (request.getCourses() != null) syncCourses(education, request.getCourses());
        if (request.getAchievements() != null) syncAchievements(education, request.getAchievements(), profile);

        return educationMapper.toEducationDto(education);
    }

    @Transactional
    public void deleteEducation(Long id) {
        if (!educationRepository.existsById(id)) {
            throw new NoSuchElementException("Education not found");
        }
        educationRepository.deleteById(id);
    }

    // ── Sync helpers ─────────────────────────────────────────────────────────

    private void syncCourses(Education education, List<CourseRequest> requests) {
        List<Course> existing = courseRepository.findAllByEducationOrderByYearAscSemesterAsc(education);
        Map<Long, Course> existingById = existing.stream()
                .collect(Collectors.toMap(BaseEntity::getId, c -> c));
        Set<Long> keepIds = new HashSet<>();

        for (CourseRequest cr : requests) {
            Course course;
            if (cr.getId() != null && existingById.containsKey(cr.getId())) {
                course = existingById.get(cr.getId());
                if (cr.getTitle() != null) course.setTitle(cr.getTitle());
                if (cr.getDescription() != null) course.setDescription(cr.getDescription());
                if (cr.getGrade() != null) course.setGrade(cr.getGrade());
                if (cr.getCredits() != null) course.setCredits(cr.getCredits());
                if (cr.getSemester() != null) course.setSemester(cr.getSemester());
                if (cr.getYear() != null) course.setYear(cr.getYear());
                if (cr.getRelevant() != null) course.setRelevant(cr.getRelevant());
                courseRepository.save(course);
            } else {
                course = courseRepository.save(buildCourse(cr, education));
                education.addCourse(course);
            }
            keepIds.add(course.getId());

            if (cr.getProjects() != null) syncCourseProjects(course, cr.getProjects());
        }

        existing.stream()
                .filter(c -> !keepIds.contains(c.getId()))
                .forEach(c -> { education.removeCourse(c); courseRepository.delete(c); });
    }

    private void syncCourseProjects(Course course, List<CourseProjectRequest> requests) {
        List<CourseProject> existing = courseProjectRepository.findAllByCourse(course);
        Map<Long, CourseProject> existingById = existing.stream()
                .collect(Collectors.toMap(BaseEntity::getId, cp -> cp));
        Set<Long> keepIds = new HashSet<>();

        for (CourseProjectRequest pr : requests) {
            if (pr.getId() != null && existingById.containsKey(pr.getId())) {
                CourseProject cp = existingById.get(pr.getId());
                if (pr.getProjectId() != null && !pr.getProjectId().equals(cp.getProject().getId())) {
                    cp.setProject(resolveProject(pr.getProjectId(), cp.getId()));
                }
                if (pr.getGrade() != null) cp.setGrade(pr.getGrade());
                if (pr.getContributionPercentage() != null) cp.setContributionPercentage(pr.getContributionPercentage());
                courseProjectRepository.save(cp);
                keepIds.add(cp.getId());
            } else {
                CourseProject cp = courseProjectRepository.save(buildCourseProject(pr, course, null));
                course.addCourseProject(cp);
                keepIds.add(cp.getId());
            }
        }

        existing.stream()
                .filter(cp -> !keepIds.contains(cp.getId()))
                .forEach(cp -> { course.removeCourseProject(cp); courseProjectRepository.delete(cp); });
    }

    private void syncAchievements(Education education, List<EducationAchievementRequest> requests, Profile profile) {
        List<EducationAchievement> existing = educationAchievementRepository.findAllByEducation(education);
        Map<Long, EducationAchievement> existingById = existing.stream()
                .collect(Collectors.toMap(BaseEntity::getId, ea -> ea));
        Set<Long> keepIds = new HashSet<>();

        for (EducationAchievementRequest ar : requests) {
            if (ar.getId() != null && existingById.containsKey(ar.getId())) {
                EducationAchievement ea = existingById.get(ar.getId());
                if (ar.getCourseId() != null) ea.setCourse(resolveCourse(ar.getCourseId()));
                if (ar.getTopic() != null) ea.setTopic(ar.getTopic());
                if (ar.getGrade() != null) ea.setGrade(ar.getGrade());
                if (ar.getMaxGrade() != null) ea.setMaxGrade(ar.getMaxGrade());
                if (ar.getCredits() != null) ea.setCredits(ar.getCredits());
                if (ar.getTeacherOrSupervisor() != null) ea.setTeacherOrSupervisor(ar.getTeacherOrSupervisor());
                if (ar.getInstitutionName() != null) ea.setInstitutionName(ar.getInstitutionName());
                if (ar.getSemester() != null) ea.setSemester(ar.getSemester());
                if (ar.getAcademicYear() != null) ea.setAcademicYear(ar.getAcademicYear());

                Achievement achievement = ea.getAchievement();
                if (ar.getAchievementTitle() != null) achievement.setTitle(ar.getAchievementTitle());
                if (ar.getAchievementDescription() != null) achievement.setDescription(ar.getAchievementDescription());
                if (ar.getAchievementType() != null) achievement.setAchievementType(ar.getAchievementType());
                if (ar.getAchievementDate() != null) achievement.setAchievementDate(ar.getAchievementDate());
                if (ar.getRecognitionLevel() != null) achievement.setRecognitionLevel(ar.getRecognitionLevel());
                if (ar.getRecognizedBy() != null) achievement.setRecognizedBy(ar.getRecognizedBy());
                if (ar.getProofUrl() != null) achievement.setProofUrl(ar.getProofUrl());
                if (ar.getIsFeatured() != null) achievement.setIsFeatured(ar.getIsFeatured());
                achievementRepository.save(achievement);

                educationAchievementRepository.save(ea);
                keepIds.add(ea.getId());
            } else {
                Achievement achievement = resolveOrCreateAchievement(ar, profile);
                EducationAchievement ea = educationAchievementRepository.save(
                        buildEducationAchievement(ar, education, achievement));
                education.addAchievement(ea);
                keepIds.add(ea.getId());
            }
        }

        existing.stream()
                .filter(ea -> !keepIds.contains(ea.getId()))
                .forEach(ea -> { education.removeAchievement(ea); educationAchievementRepository.delete(ea); });
    }

    // ── Builder / resolve helpers ────────────────────────────────────────────

    private Course buildCourse(CourseRequest cr, Education education) {
        return Course.builder()
                .education(education)
                .title(cr.getTitle())
                .description(cr.getDescription())
                .grade(cr.getGrade())
                .credits(cr.getCredits())
                .semester(cr.getSemester())
                .year(cr.getYear())
                .relevant(Boolean.TRUE.equals(cr.getRelevant()))
                .build();
    }

    private CourseProject buildCourseProject(CourseProjectRequest pr, Course course, Long excludeLinkId) {
        Project project = resolveProject(pr.getProjectId(), excludeLinkId);
        return CourseProject.builder()
                .course(course)
                .project(project)
                .grade(pr.getGrade())
                .contributionPercentage(pr.getContributionPercentage() != null ? pr.getContributionPercentage() : 100)
                .build();
    }

    /**
     * Resolves an existing Project by id and enforces that it isn't already linked to another
     * course, nor to any job experience — a project can only be linked to one of a job or a
     * course at a time. Volunteer experience is exempt: a project may be both a volunteer
     * project and a course project. excludeLinkId lets an update keep its own link.
     */
    private Project resolveProject(Long projectId, Long excludeLinkId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NoSuchElementException("Project not found"));

        courseProjectRepository.findByProject(project).ifPresent(existingLink -> {
            if (excludeLinkId == null || !existingLink.getId().equals(excludeLinkId)) {
                throw new IllegalArgumentException("Project is already linked to a course");
            }
        });

        jobProjectsRepository.findByProject(project).ifPresent(existingLink -> {
            throw new IllegalArgumentException("Project is already linked to a job experience");
        });

        return project;
    }

    private EducationAchievement buildEducationAchievement(EducationAchievementRequest ar, Education education, Achievement achievement) {
        return EducationAchievement.builder()
                .education(education)
                .achievement(achievement)
                .course(resolveCourse(ar.getCourseId()))
                .topic(ar.getTopic())
                .grade(ar.getGrade())
                .maxGrade(ar.getMaxGrade())
                .credits(ar.getCredits())
                .teacherOrSupervisor(ar.getTeacherOrSupervisor())
                .institutionName(ar.getInstitutionName())
                .semester(ar.getSemester())
                .academicYear(ar.getAcademicYear())
                .build();
    }

    private Achievement resolveOrCreateAchievement(EducationAchievementRequest ar, Profile profile) {
        if (ar.getAchievementId() != null) {
            return achievementRepository.findById(ar.getAchievementId())
                    .orElseThrow(() -> new NoSuchElementException("Achievement not found: " + ar.getAchievementId()));
        }
        return achievementRepository.save(Achievement.builder()
                .profile(profile)
                .title(ar.getAchievementTitle())
                .description(ar.getAchievementDescription())
                .achievementType(ar.getAchievementType())
                .achievementDate(ar.getAchievementDate())
                .recognitionLevel(ar.getRecognitionLevel() != null ? ar.getRecognitionLevel() : RecognitionLevel.LOCAL)
                .recognizedBy(ar.getRecognizedBy())
                .proofUrl(ar.getProofUrl())
                .isFeatured(Boolean.TRUE.equals(ar.getIsFeatured()))
                .sortOrder(ar.getSortOrder())
                .build());
    }

    private Course resolveCourse(Long courseId) {
        if (courseId == null) return null;
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));
    }

    private Education requireEducation(Long id) {
        return educationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Education not found"));
    }

    private CourseDto toCourseDtoWithProjects(Course course) {
        CourseDto dto = educationMapper.toCourseDto(course);
        dto.setProjects(courseProjectRepository.findAllByCourse(course).stream()
                .map(cp -> CourseProjectDto.builder()
                        .id(cp.getId())
                        .grade(cp.getGrade())
                        .contributionPercentage(cp.getContributionPercentage())
                        .project(educationMapper.toProjectInCourseDto(cp.getProject()))
                        .build())
                .toList());
        return dto;
    }
}
