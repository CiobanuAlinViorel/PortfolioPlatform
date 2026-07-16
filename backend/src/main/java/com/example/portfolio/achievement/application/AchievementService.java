package com.example.portfolio.achievement.application;

import com.example.portfolio.achievement.domain.Achievement;
import com.example.portfolio.achievement.domain.AchievementType;
import com.example.portfolio.achievement.domain.RecognitionLevel;
import com.example.portfolio.achievement.dto.AchievementDto;
import com.example.portfolio.achievement.dto.CreateAchievementRequest;
import com.example.portfolio.achievement.dto.UpdateAchievementRequest;
import com.example.portfolio.achievement.mapper.AchievementMapper;
import com.example.portfolio.achievement.persistence.AchievementRepository;
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
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final ProfileRepository profileRepository;
    private final AchievementMapper achievementMapper;

    @Transactional
    public List<AchievementDto> getAchievements(AchievementType type, RecognitionLevel recognitionLevel, Boolean isFeatured) {
        Profile profile = profileRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new NoSuchElementException("Profile not found"));

        List<Specification<Achievement>> specs = new ArrayList<>();
        specs.add((root, query, cb) -> cb.equal(root.get("profile"), profile));

        if (type != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("achievementType"), type));
        }
        if (recognitionLevel != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("recognitionLevel"), recognitionLevel));
        }
        if (isFeatured != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("isFeatured"), isFeatured));
        }

        return achievementRepository.findAll(Specification.allOf(specs))
                .stream()
                .map(achievementMapper::toAchievementDto)
                .toList();
    }

    @Transactional
    public AchievementDto getAchievementById(Long id) {
        return achievementMapper.toAchievementDto(
                achievementRepository.findById(id)
                        .orElseThrow(() -> new NoSuchElementException("Achievement not found"))
        );
    }

    @Transactional
    public AchievementDto createAchievement(CreateAchievementRequest request) {
        Profile profile = profileRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new NoSuchElementException("Profile not found"));

        Achievement achievement = Achievement.builder()
                .profile(profile)
                .title(request.getTitle())
                .description(request.getDescription())
                .achievementType(request.getAchievementType())
                .achievementDate(request.getAchievementDate())
                .recognitionLevel(request.getRecognitionLevel() != null
                        ? request.getRecognitionLevel() : RecognitionLevel.LOCAL)
                .recognizedBy(request.getRecognizedBy())
                .proofUrl(request.getProofUrl())
                .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
                .sortOrder(request.getSortOrder())
                .build();

        return achievementMapper.toAchievementDto(achievementRepository.save(achievement));
    }

    @Transactional
    public AchievementDto updateAchievement(Long id, UpdateAchievementRequest request) {
        Achievement achievement = achievementRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Achievement not found"));

        achievementMapper.updateAchievement(request, achievement);
        return achievementMapper.toAchievementDto(achievementRepository.save(achievement));
    }

    @Transactional
    public void deleteAchievement(Long id) {
        if (!achievementRepository.existsById(id)) {
            throw new NoSuchElementException("Achievement not found");
        }
        achievementRepository.deleteById(id);
    }
}
