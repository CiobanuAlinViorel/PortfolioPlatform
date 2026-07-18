package com.example.portfolio.hobby.application;

import com.example.portfolio.hobby.domain.ActivityLevel;
import com.example.portfolio.hobby.domain.Hobby;
import com.example.portfolio.hobby.domain.HobbyCategory;
import com.example.portfolio.hobby.domain.HobbySkill;
import com.example.portfolio.hobby.dto.*;
import com.example.portfolio.hobby.mapper.HobbyMapper;
import com.example.portfolio.hobby.persistence.HobbyRepository;
import com.example.portfolio.hobby.persistence.HobbySkillRepository;
import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.profile.persistence.ProfileRepository;
import com.example.portfolio.shared.base.BaseEntity;
import com.example.portfolio.skills.domain.Skill;
import com.example.portfolio.skills.persistence.SkillRepository;
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
public class HobbyService {

    private final HobbyRepository hobbyRepository;
    private final HobbySkillRepository hobbySkillRepository;
    private final SkillRepository skillRepository;
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
        Hobby hobby = requireHobby(id);

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

        hobby = hobbyRepository.save(hobby);

        if (request.getSkills() != null) {
            for (HobbySkillRequest sr : request.getSkills()) {
                hobby.addHobbySkill(hobbySkillRepository.save(buildHobbySkill(sr, hobby, null)));
            }
        }

        return hobbyMapper.toHobbyDto(hobby);
    }

    @Transactional
    public HobbyDto updateHobby(Long id, UpdateHobbyRequest request) {
        Hobby hobby = requireHobby(id);

        hobbyMapper.updateHobby(request, hobby);
        hobbyRepository.save(hobby);

        if (request.getSkills() != null) syncSkills(hobby, request.getSkills());

        return hobbyMapper.toHobbyDto(hobby);
    }

    @Transactional
    public void deleteHobby(Long id) {
        // cascade = ALL on Hobby.hobbySkills removes them along with the hobby
        hobbyRepository.delete(requireHobby(id));
    }

    // ── Sync helpers ─────────────────────────────────────────────────────────

    private void syncSkills(Hobby hobby, List<HobbySkillRequest> requests) {
        List<HobbySkill> existing = hobbySkillRepository.findAllByHobby(hobby);
        Map<Long, HobbySkill> existingById = existing.stream()
                .collect(Collectors.toMap(BaseEntity::getId, hs -> hs));
        Set<Long> keepIds = new HashSet<>();

        for (HobbySkillRequest sr : requests) {
            if (sr.getId() != null && existingById.containsKey(sr.getId())) {
                HobbySkill hs = existingById.get(sr.getId());
                if (sr.getSkillId() != null && !sr.getSkillId().equals(hs.getSkill().getId())) {
                    hs.setSkill(resolveSkill(sr.getSkillId(), hs.getId()));
                }
                if (sr.getUsagePercentage() != null) hs.setUsagePercentage(sr.getUsagePercentage());
                hobbySkillRepository.save(hs);
                keepIds.add(hs.getId());
            } else {
                HobbySkill hs = hobbySkillRepository.save(buildHobbySkill(sr, hobby, null));
                hobby.addHobbySkill(hs);
                keepIds.add(hs.getId());
            }
        }

        existing.stream()
                .filter(hs -> !keepIds.contains(hs.getId()))
                .forEach(hs -> { hobby.removeHobbySkill(hs); hobbySkillRepository.delete(hs); });
    }

    // ── Builder / resolve helpers ────────────────────────────────────────────

    private HobbySkill buildHobbySkill(HobbySkillRequest sr, Hobby hobby, Long excludeLinkId) {
        Skill skill = resolveSkill(sr.getSkillId(), excludeLinkId);
        return HobbySkill.builder()
                .hobby(hobby)
                .skill(skill)
                .usagePercentage(sr.getUsagePercentage())
                .build();
    }

    /**
     * Resolves an existing Skill by id and enforces that it isn't already linked to another
     * hobby — a skill can only be linked to one hobby at a time. excludeLinkId lets an update
     * keep its own link.
     */
    private Skill resolveSkill(Long skillId, Long excludeLinkId) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new NoSuchElementException("Skill not found"));

        hobbySkillRepository.findBySkill(skill).ifPresent(existingLink -> {
            if (excludeLinkId == null || !existingLink.getId().equals(excludeLinkId)) {
                throw new IllegalArgumentException("Skill is already linked to a hobby");
            }
        });

        return skill;
    }

    private Hobby requireHobby(Long id) {
        return hobbyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Hobby not found"));
    }
}
