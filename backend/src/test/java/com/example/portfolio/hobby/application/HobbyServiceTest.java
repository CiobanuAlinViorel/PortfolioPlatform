package com.example.portfolio.hobby.application;

import com.example.portfolio.experience.domain.ImpactLevel;
import com.example.portfolio.hobby.domain.*;
import com.example.portfolio.hobby.dto.*;
import com.example.portfolio.hobby.mapper.HobbyMapper;
import com.example.portfolio.hobby.persistence.HobbyRepository;
import com.example.portfolio.hobby.persistence.HobbySkillRepository;
import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.profile.persistence.ProfileRepository;
import com.example.portfolio.skills.domain.Skill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HobbyServiceTest {

    @Mock private HobbyRepository hobbyRepository;
    @Mock private HobbySkillRepository hobbySkillRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private HobbyMapper hobbyMapper;

    @InjectMocks
    private HobbyService hobbyService;

    private Profile profile;
    private Hobby hobby;
    private HobbyDto hobbyDto;
    private HobbyDetailDto hobbyDetailDto;

    @BeforeEach
    void setUp() {
        profile = Profile.builder().firstName("John").lastName("Doe").build();

        hobby = Hobby.builder()
                .profile(profile)
                .name("Chess")
                .description("Strategic board game")
                .category(HobbyCategory.GAMING)
                .activityLevel(ActivityLevel.REGULAR)
                .complexityLevel(ComplexityLevel.ADVANCED)
                .impactOnWork(ImpactLevel.HIGH)
                .yearsActive(5L)
                .build();

        hobbyDto = HobbyDto.builder()
                .id(1L)
                .name("Chess")
                .category(HobbyCategory.GAMING)
                .activityLevel(ActivityLevel.REGULAR)
                .build();

        hobbyDetailDto = HobbyDetailDto.builder()
                .id(1L)
                .name("Chess")
                .category(HobbyCategory.GAMING)
                .build();
    }

    // ── getHobbies ────────────────────────────────────────────────────────────

    @Test
    void getHobbies_shouldReturnMappedList_whenNoFilters() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(hobbyRepository.findAll(any(Specification.class))).thenReturn(List.of(hobby));
        when(hobbyMapper.toHobbyDto(hobby)).thenReturn(hobbyDto);

        List<HobbyDto> result = hobbyService.getHobbies(null, null);

        assertThat(result).containsExactly(hobbyDto);
    }

    @Test
    void getHobbies_shouldApplySpec_whenCategoryFilterProvided() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(hobbyRepository.findAll(any(Specification.class))).thenReturn(List.of(hobby));
        when(hobbyMapper.toHobbyDto(hobby)).thenReturn(hobbyDto);

        List<HobbyDto> result = hobbyService.getHobbies(HobbyCategory.GAMING, null);

        assertThat(result).hasSize(1);
        verify(hobbyRepository).findAll(any(Specification.class));
    }

    @Test
    void getHobbies_shouldApplySpec_whenActivityLevelFilterProvided() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(hobbyRepository.findAll(any(Specification.class))).thenReturn(List.of());

        List<HobbyDto> result = hobbyService.getHobbies(null, ActivityLevel.DAILY);

        assertThat(result).isEmpty();
        verify(hobbyMapper, never()).toHobbyDto(any());
    }

    @Test
    void getHobbies_shouldReturnEmpty_whenNoHobbiesExist() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(hobbyRepository.findAll(any(Specification.class))).thenReturn(List.of());

        List<HobbyDto> result = hobbyService.getHobbies(null, null);

        assertThat(result).isEmpty();
        verify(hobbyMapper, never()).toHobbyDto(any());
    }

    @Test
    void getHobbies_shouldThrowNoSuchElementException_whenProfileNotFound() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hobbyService.getHobbies(null, null))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Profile not found");

        verifyNoInteractions(hobbyRepository, hobbyMapper);
    }

    // ── getHobbyById ──────────────────────────────────────────────────────────

    @Test
    void getHobbyById_shouldReturnDetailWithSkills_whenHobbyExists() {
        Skill skill = Skill.builder().name("Problem Solving").build();
        HobbySkill hobbySkill = HobbySkill.builder()
                .hobby(hobby).skill(skill).usagePercentage(new BigDecimal("0.8")).build();
        SkillInHobbyDto skillDto = SkillInHobbyDto.builder()
                .skillName("Problem Solving").usagePercentage(new BigDecimal("0.8")).build();

        when(hobbyRepository.findById(1L)).thenReturn(Optional.of(hobby));
        when(hobbyMapper.toHobbyDetailDto(hobby)).thenReturn(hobbyDetailDto);
        when(hobbySkillRepository.findAllByHobby(hobby)).thenReturn(List.of(hobbySkill));
        when(hobbyMapper.toSkillInHobbyDto(hobbySkill)).thenReturn(skillDto);

        HobbyDetailDto result = hobbyService.getHobbyById(1L);

        assertThat(result.getName()).isEqualTo("Chess");
        assertThat(result.getSkills()).containsExactly(skillDto);
    }

    @Test
    void getHobbyById_shouldReturnEmptySkills_whenNoHobbySkillsExist() {
        when(hobbyRepository.findById(1L)).thenReturn(Optional.of(hobby));
        when(hobbyMapper.toHobbyDetailDto(hobby)).thenReturn(hobbyDetailDto);
        when(hobbySkillRepository.findAllByHobby(hobby)).thenReturn(List.of());

        HobbyDetailDto result = hobbyService.getHobbyById(1L);

        assertThat(result.getSkills()).isEmpty();
        verify(hobbyMapper, never()).toSkillInHobbyDto(any());
    }

    @Test
    void getHobbyById_shouldThrowNoSuchElementException_whenNotFound() {
        when(hobbyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hobbyService.getHobbyById(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Hobby not found");

        verifyNoInteractions(hobbySkillRepository, hobbyMapper);
    }

    // ── createHobby ───────────────────────────────────────────────────────────

    @Test
    void createHobby_shouldSaveAndReturnDto_whenProfileExists() {
        CreateHobbyRequest request = CreateHobbyRequest.builder()
                .name("Chess").category(HobbyCategory.GAMING)
                .activityLevel(ActivityLevel.REGULAR).build();

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(hobbyRepository.save(any(Hobby.class))).thenReturn(hobby);
        when(hobbyMapper.toHobbyDto(hobby)).thenReturn(hobbyDto);

        HobbyDto result = hobbyService.createHobby(request);

        assertThat(result).isEqualTo(hobbyDto);
        verify(hobbyRepository).save(any(Hobby.class));
    }

    @Test
    void createHobby_shouldSetProfileOnHobby() {
        CreateHobbyRequest request = CreateHobbyRequest.builder()
                .name("Chess").category(HobbyCategory.GAMING).build();

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(hobbyRepository.save(any(Hobby.class))).thenReturn(hobby);
        when(hobbyMapper.toHobbyDto(any())).thenReturn(hobbyDto);

        hobbyService.createHobby(request);

        verify(hobbyRepository).save(argThat(h -> h.getProfile() == profile));
    }

    @Test
    void createHobby_shouldThrowNoSuchElementException_whenProfileNotFound() {
        CreateHobbyRequest request = CreateHobbyRequest.builder()
                .name("Chess").category(HobbyCategory.GAMING).build();
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hobbyService.createHobby(request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Profile not found");

        verify(hobbyRepository, never()).save(any());
    }

    // ── updateHobby ───────────────────────────────────────────────────────────

    @Test
    void updateHobby_shouldUpdateAndReturnDto_whenHobbyExists() {
        UpdateHobbyRequest request = UpdateHobbyRequest.builder()
                .name("Advanced Chess").activityLevel(ActivityLevel.DAILY).build();

        when(hobbyRepository.findById(1L)).thenReturn(Optional.of(hobby));
        when(hobbyRepository.save(hobby)).thenReturn(hobby);
        when(hobbyMapper.toHobbyDto(hobby)).thenReturn(hobbyDto);

        HobbyDto result = hobbyService.updateHobby(1L, request);

        assertThat(result).isEqualTo(hobbyDto);
        verify(hobbyMapper).updateHobby(request, hobby);
        verify(hobbyRepository).save(hobby);
    }

    @Test
    void updateHobby_shouldThrowNoSuchElementException_whenNotFound() {
        UpdateHobbyRequest request = UpdateHobbyRequest.builder().name("X").build();
        when(hobbyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hobbyService.updateHobby(99L, request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Hobby not found");

        verify(hobbyRepository, never()).save(any());
    }

    // ── deleteHobby ───────────────────────────────────────────────────────────

    @Test
    void deleteHobby_shouldDeleteById_whenHobbyExists() {
        when(hobbyRepository.existsById(1L)).thenReturn(true);

        hobbyService.deleteHobby(1L);

        verify(hobbyRepository).deleteById(1L);
    }

    @Test
    void deleteHobby_shouldThrowNoSuchElementException_whenNotFound() {
        when(hobbyRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> hobbyService.deleteHobby(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Hobby not found");

        verify(hobbyRepository, never()).deleteById(any());
    }
}
