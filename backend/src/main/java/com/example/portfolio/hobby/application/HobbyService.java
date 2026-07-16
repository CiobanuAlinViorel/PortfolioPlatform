package com.example.portfolio.hobby.application;

import com.example.portfolio.hobby.domain.ActivityLevel;
import com.example.portfolio.hobby.domain.Hobby;
import com.example.portfolio.hobby.domain.HobbyCategory;
import com.example.portfolio.hobby.dto.*;
import com.example.portfolio.hobby.mapper.HobbyMapper;
import com.example.portfolio.hobby.persistence.HobbyRepository;
import com.example.portfolio.hobby.persistence.HobbySkillRepository;
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
public class HobbyService {

    private final HobbyRepository hobbyRepository;
    private final HobbySkillRepository hobbySkillRepository;
    private final ProfileRepository profileRepository;
    private final HobbyMapper hobbyMapper;

    @Transactional
    public List<HobbyDto> getHobbies(HobbyCategory category, ActivityLevel activityLevel) {
        Profile profile = profileRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new NoSuchElementException("Profile not found"));

        List<Specification<Hobby>> specs = new ArrayList<>();
        specs.add((root, query, cb) -> cb.equal(root.get("profile"), profile));

        if (category != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("category"), category));
        }
        if (activityLevel != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("activityLevel"), activityLevel));
        }

        return hobbyRepository.findAll(Specification.allOf(specs))
                .stream()
                .map(hobbyMapper::toHobbyDto)
                .toList();
    }

    @Transactional
    public HobbyDetailDto getHobbyById(Long id) {
        Hobby hobby = hobbyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Hobby not found"));

        HobbyDetailDto dto = hobbyMapper.toHobbyDetailDto(hobby);

        List<SkillInHobbyDto> skills = hobbySkillRepository.findAllByHobby(hobby)
                .stream()
                .map(hobbyMapper::toSkillInHobbyDto)
                .toList();

        dto.setSkills(skills);
        return dto;
    }

    @Transactional
    public HobbyDto createHobby(CreateHobbyRequest request) {
        Profile profile = profileRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new NoSuchElementException("Profile not found"));

        Hobby hobby = Hobby.builder()
                .profile(profile)
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .activityLevel(request.getActivityLevel())
                .complexityLevel(request.getComplexityLevel())
                .impactOnWork(request.getImpactOnWork())
                .yearsActive(request.getYearsActive())
                .whyInterested(request.getWhyInterested())
                .favoriteAspect(request.getFavoriteAspect())
                .build();

        return hobbyMapper.toHobbyDto(hobbyRepository.save(hobby));
    }

    @Transactional
    public HobbyDto updateHobby(Long id, UpdateHobbyRequest request) {
        Hobby hobby = hobbyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Hobby not found"));

        hobbyMapper.updateHobby(request, hobby);
        return hobbyMapper.toHobbyDto(hobbyRepository.save(hobby));
    }

    @Transactional
    public void deleteHobby(Long id) {
        if (!hobbyRepository.existsById(id)) {
            throw new NoSuchElementException("Hobby not found");
        }
        hobbyRepository.deleteById(id);
    }
}
