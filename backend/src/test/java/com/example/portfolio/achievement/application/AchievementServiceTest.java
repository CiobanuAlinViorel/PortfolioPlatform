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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AchievementServiceTest {

    @Mock private AchievementRepository achievementRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private AchievementMapper achievementMapper;

    @InjectMocks
    private AchievementService achievementService;

    private Profile profile;
    private Achievement achievement;
    private AchievementDto achievementDto;

    @BeforeEach
    void setUp() {
        profile = Profile.builder().firstName("John").lastName("Doe").build();

        achievement = Achievement.builder()
                .profile(profile)
                .title("Best Developer Award")
                .description("Awarded for outstanding performance")
                .achievementType(AchievementType.AWARD)
                .achievementDate(LocalDate.of(2024, 3, 15))
                .recognitionLevel(RecognitionLevel.NATIONAL)
                .recognizedBy("Tech Corp")
                .isFeatured(true)
                .sortOrder(1)
                .build();

        achievementDto = AchievementDto.builder()
                .id(1L)
                .title("Best Developer Award")
                .achievementType(AchievementType.AWARD)
                .achievementDate(LocalDate.of(2024, 3, 15))
                .recognitionLevel(RecognitionLevel.NATIONAL)
                .isFeatured(true)
                .build();
    }

    // ── getAchievements ───────────────────────────────────────────────────────

    @Test
    void getAchievements_shouldReturnMappedList_whenNoFilters() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(achievementRepository.findAll(any(Specification.class))).thenReturn(List.of(achievement));
        when(achievementMapper.toAchievementDto(achievement)).thenReturn(achievementDto);

        List<AchievementDto> result = achievementService.getAchievements(null, null, null);

        assertThat(result).containsExactly(achievementDto);
    }

    @Test
    void getAchievements_shouldApplySpec_whenTypeFilterProvided() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(achievementRepository.findAll(any(Specification.class))).thenReturn(List.of(achievement));
        when(achievementMapper.toAchievementDto(achievement)).thenReturn(achievementDto);

        List<AchievementDto> result = achievementService.getAchievements(AchievementType.AWARD, null, null);

        assertThat(result).hasSize(1);
        verify(achievementRepository).findAll(any(Specification.class));
    }

    @Test
    void getAchievements_shouldApplySpec_whenRecognitionLevelFilterProvided() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(achievementRepository.findAll(any(Specification.class))).thenReturn(List.of(achievement));
        when(achievementMapper.toAchievementDto(achievement)).thenReturn(achievementDto);

        List<AchievementDto> result = achievementService.getAchievements(null, RecognitionLevel.NATIONAL, null);

        assertThat(result).hasSize(1);
    }

    @Test
    void getAchievements_shouldApplySpec_whenIsFeaturedFilterProvided() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(achievementRepository.findAll(any(Specification.class))).thenReturn(List.of(achievement));
        when(achievementMapper.toAchievementDto(achievement)).thenReturn(achievementDto);

        List<AchievementDto> result = achievementService.getAchievements(null, null, true);

        assertThat(result).hasSize(1);
    }

    @Test
    void getAchievements_shouldApplyAllSpecs_whenAllFiltersProvided() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(achievementRepository.findAll(any(Specification.class))).thenReturn(List.of(achievement));
        when(achievementMapper.toAchievementDto(achievement)).thenReturn(achievementDto);

        List<AchievementDto> result = achievementService.getAchievements(
                AchievementType.AWARD, RecognitionLevel.NATIONAL, true);

        assertThat(result).hasSize(1);
    }

    @Test
    void getAchievements_shouldReturnEmpty_whenNoneMatch() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(achievementRepository.findAll(any(Specification.class))).thenReturn(List.of());

        List<AchievementDto> result = achievementService.getAchievements(AchievementType.PUBLICATION, null, null);

        assertThat(result).isEmpty();
        verify(achievementMapper, never()).toAchievementDto(any());
    }

    @Test
    void getAchievements_shouldThrowNoSuchElementException_whenProfileNotFound() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> achievementService.getAchievements(null, null, null))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Profile not found");

        verifyNoInteractions(achievementRepository, achievementMapper);
    }

    // ── getAchievementById ────────────────────────────────────────────────────

    @Test
    void getAchievementById_shouldReturnDto_whenFound() {
        when(achievementRepository.findById(1L)).thenReturn(Optional.of(achievement));
        when(achievementMapper.toAchievementDto(achievement)).thenReturn(achievementDto);

        AchievementDto result = achievementService.getAchievementById(1L);

        assertThat(result).isEqualTo(achievementDto);
    }

    @Test
    void getAchievementById_shouldThrowNoSuchElementException_whenNotFound() {
        when(achievementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> achievementService.getAchievementById(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Achievement not found");
    }

    // ── createAchievement ─────────────────────────────────────────────────────

    @Test
    void createAchievement_shouldSaveAndReturnDto_whenProfileExists() {
        CreateAchievementRequest request = CreateAchievementRequest.builder()
                .title("Best Developer Award").description("Outstanding performance")
                .achievementType(AchievementType.AWARD).achievementDate(LocalDate.of(2024, 3, 15))
                .recognitionLevel(RecognitionLevel.NATIONAL).isFeatured(true).build();

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(achievementRepository.save(any(Achievement.class))).thenReturn(achievement);
        when(achievementMapper.toAchievementDto(achievement)).thenReturn(achievementDto);

        AchievementDto result = achievementService.createAchievement(request);

        assertThat(result).isEqualTo(achievementDto);
        verify(achievementRepository).save(any(Achievement.class));
    }

    @Test
    void createAchievement_shouldDefaultRecognitionLevelToLocal_whenNotProvided() {
        CreateAchievementRequest request = CreateAchievementRequest.builder()
                .title("Award").description("Desc")
                .achievementType(AchievementType.MILESTONE).achievementDate(LocalDate.now())
                .recognitionLevel(null).build();

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(achievementRepository.save(any(Achievement.class))).thenReturn(achievement);
        when(achievementMapper.toAchievementDto(any())).thenReturn(achievementDto);

        achievementService.createAchievement(request);

        verify(achievementRepository).save(argThat(a -> a.getRecognitionLevel() == RecognitionLevel.LOCAL));
    }

    @Test
    void createAchievement_shouldDefaultIsFeaturedToFalse_whenNotProvided() {
        CreateAchievementRequest request = CreateAchievementRequest.builder()
                .title("Award").description("Desc")
                .achievementType(AchievementType.AWARD).achievementDate(LocalDate.now())
                .isFeatured(null).build();

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(achievementRepository.save(any(Achievement.class))).thenReturn(achievement);
        when(achievementMapper.toAchievementDto(any())).thenReturn(achievementDto);

        achievementService.createAchievement(request);

        verify(achievementRepository).save(argThat(a -> !a.getIsFeatured()));
    }

    @Test
    void createAchievement_shouldThrowNoSuchElementException_whenProfileNotFound() {
        CreateAchievementRequest request = CreateAchievementRequest.builder()
                .title("Award").description("Desc")
                .achievementType(AchievementType.AWARD).achievementDate(LocalDate.now()).build();
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> achievementService.createAchievement(request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Profile not found");

        verify(achievementRepository, never()).save(any());
    }

    // ── updateAchievement ─────────────────────────────────────────────────────

    @Test
    void updateAchievement_shouldUpdateAndReturnDto_whenFound() {
        UpdateAchievementRequest request = UpdateAchievementRequest.builder()
                .title("Updated Title").isFeatured(false).build();

        when(achievementRepository.findById(1L)).thenReturn(Optional.of(achievement));
        when(achievementRepository.save(achievement)).thenReturn(achievement);
        when(achievementMapper.toAchievementDto(achievement)).thenReturn(achievementDto);

        AchievementDto result = achievementService.updateAchievement(1L, request);

        assertThat(result).isEqualTo(achievementDto);
        verify(achievementMapper).updateAchievement(request, achievement);
        verify(achievementRepository).save(achievement);
    }

    @Test
    void updateAchievement_shouldThrowNoSuchElementException_whenNotFound() {
        UpdateAchievementRequest request = UpdateAchievementRequest.builder().title("X").build();
        when(achievementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> achievementService.updateAchievement(99L, request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Achievement not found");

        verify(achievementRepository, never()).save(any());
    }

    // ── deleteAchievement ─────────────────────────────────────────────────────

    @Test
    void deleteAchievement_shouldDeleteById_whenFound() {
        when(achievementRepository.existsById(1L)).thenReturn(true);

        achievementService.deleteAchievement(1L);

        verify(achievementRepository).deleteById(1L);
    }

    @Test
    void deleteAchievement_shouldThrowNoSuchElementException_whenNotFound() {
        when(achievementRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> achievementService.deleteAchievement(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Achievement not found");

        verify(achievementRepository, never()).deleteById(any());
    }
}
