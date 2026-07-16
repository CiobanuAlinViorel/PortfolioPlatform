package com.example.portfolio.education.application;

import com.example.portfolio.education.domain.Education;
import com.example.portfolio.education.domain.EducationLevel;
import com.example.portfolio.education.domain.EducationStatus;
import com.example.portfolio.education.dto.*;
import com.example.portfolio.education.mapper.EducationMapper;
import com.example.portfolio.education.persistence.CourseRepository;
import com.example.portfolio.education.persistence.EducationAchievementRepository;
import com.example.portfolio.education.persistence.EducationRepository;
import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.profile.persistence.ProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class EducationService {

    private final EducationRepository educationRepository;
    private final CourseRepository courseRepository;
    private final EducationAchievementRepository educationAchievementRepository;
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
        Education education = educationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Education not found"));

        EducationDetailDto dto = educationMapper.toEducationDetailDto(education);

        List<CourseDto> courses = courseRepository
                .findAllByEducationOrderByYearAscSemesterAsc(education)
                .stream()
                .map(educationMapper::toCourseDto)
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

        return educationMapper.toEducationDto(educationRepository.save(education));
    }

    @Transactional
    public EducationDto updateEducation(Long id, UpdateEducationRequest request) {
        Education education = educationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Education not found"));

        educationMapper.updateEducation(request, education);
        return educationMapper.toEducationDto(educationRepository.save(education));
    }

    @Transactional
    public void deleteEducation(Long id) {
        if (!educationRepository.existsById(id)) {
            throw new NoSuchElementException("Education not found");
        }
        educationRepository.deleteById(id);
    }
}
