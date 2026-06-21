package com.example.portfolio.skills.application;

import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.profile.persistence.ProfileRepository;
import com.example.portfolio.projects.domain.Project;
import com.example.portfolio.projects.domain.ProjectSkill;
import com.example.portfolio.projects.domain.ProjectStatus;
import com.example.portfolio.projects.persistence.ProjectSkillRepository;
import com.example.portfolio.skills.domain.ProficiencyLevel;
import com.example.portfolio.skills.domain.Skill;
import com.example.portfolio.skills.domain.SkillCategory;
import com.example.portfolio.skills.domain.SkillTag;
import com.example.portfolio.skills.dto.ProjectInSkillDto;
import com.example.portfolio.skills.dto.SkillDetailDto;
import com.example.portfolio.skills.dto.SkillListItemDto;
import com.example.portfolio.skills.mapper.SkillMapper;
import com.example.portfolio.skills.persistence.SkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class SkillServiceTest {

    @Mock private ProfileRepository profileRepository;
    @Mock private SkillRepository skillRepository;
    @Mock private ProjectSkillRepository projectSkillRepository;
    @Mock private SkillMapper skillMapper;

    @InjectMocks
    private SkillService skillService;

    private Profile profile;
    private SkillCategory category;
    private Skill skill;

    @BeforeEach
    void setUp() {
        profile = Profile.builder().firstName("John").lastName("Doe").build();
        category = SkillCategory.builder().name("Backend").build();
        skill = Skill.builder()
                .name("Java")
                .profile(profile)
                .category(category)
                .proficiency(ProficiencyLevel.EXPERT)
                .level(90)
                .yearsOfExperience(new BigDecimal("5.0"))
                .description("Primary language")
                .lastUsedDate(LocalDate.of(2024, 1, 1))
                .hasCertification(true)
                .build();
    }

    // ── getSkillsList ─────────────────────────────────────────────────────────

    @Test
    void getSkillsList_shouldReturnMappedList_whenProfileAndSkillsExist() {
        Skill skill2 = Skill.builder().name("Spring").profile(profile).proficiency(ProficiencyLevel.ADVANCED).build();
        SkillListItemDto dto1 = SkillListItemDto.builder().name("Java").categoryName("Backend").proficiency(ProficiencyLevel.EXPERT).build();
        SkillListItemDto dto2 = SkillListItemDto.builder().name("Spring").proficiency(ProficiencyLevel.ADVANCED).build();

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(skillRepository.findAllByProfileOrderBySortOrderAscNameAsc(profile)).thenReturn(List.of(skill, skill2));
        when(skillMapper.toSkillListItemDto(any(Skill.class))).thenAnswer(inv -> {
            Skill s = inv.getArgument(0);
            return s.getName().equals("Java") ? dto1 : dto2;
        });

        List<SkillListItemDto> result = skillService.getSkillsList();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Java");
        assertThat(result.get(1).getName()).isEqualTo("Spring");
    }

    @Test
    void getSkillsList_shouldReturnEmptyList_whenNoSkillsExist() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(skillRepository.findAllByProfileOrderBySortOrderAscNameAsc(profile)).thenReturn(List.of());

        List<SkillListItemDto> result = skillService.getSkillsList();

        assertThat(result).isEmpty();
        verify(skillMapper, never()).toSkillListItemDto(any());
    }

    @Test
    void getSkillsList_shouldCallRepositoryWithProfile() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(skillRepository.findAllByProfileOrderBySortOrderAscNameAsc(profile)).thenReturn(List.of());

        skillService.getSkillsList();

        verify(skillRepository).findAllByProfileOrderBySortOrderAscNameAsc(profile);
    }

    @Test
    void getSkillsList_shouldThrowNoSuchElementException_whenNoProfileExists() {
        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> skillService.getSkillsList())
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Profile not found");

        verifyNoInteractions(skillRepository, skillMapper);
    }

    // ── getSkillById ──────────────────────────────────────────────────────────

    @Test
    void getSkillById_shouldReturnDetailWithTagsAndProjects_whenSkillExists() {
        SkillTag tag1 = SkillTag.builder().tagName("oop").skill(skill).build();
        SkillTag tag2 = SkillTag.builder().tagName("jvm").skill(skill).build();
        ReflectionTestUtils.setField(tag1, "id", 1L);
        ReflectionTestUtils.setField(tag2, "id", 2L);
        skill.addSkillTag(tag1);
        skill.addSkillTag(tag2);

        Project project = Project.builder().title("Portfolio API").status(ProjectStatus.PRODUCTION).profile(profile).build();
        ProjectSkill projectSkill = ProjectSkill.builder().skill(skill).project(project).usage(new BigDecimal("0.8")).build();

        SkillDetailDto baseDto = SkillDetailDto.builder()
                .name("Java").categoryName("Backend").proficiency(ProficiencyLevel.EXPERT).level(90).build();
        ProjectInSkillDto projectDto = ProjectInSkillDto.builder().title("Portfolio API").status(ProjectStatus.PRODUCTION).build();

        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));
        when(skillMapper.toSkillDetailDto(skill)).thenReturn(baseDto);
        when(projectSkillRepository.findAllBySkill(skill)).thenReturn(List.of(projectSkill));
        when(skillMapper.toProjectInSkillDto(project)).thenReturn(projectDto);

        SkillDetailDto result = skillService.getSkillById(1L);

        assertThat(result.getName()).isEqualTo("Java");
        assertThat(result.getTags()).containsExactlyInAnyOrder("oop", "jvm");
        assertThat(result.getProjects()).hasSize(1);
        assertThat(result.getProjects().get(0).getTitle()).isEqualTo("Portfolio API");
    }

    @Test
    void getSkillById_shouldReturnEmptyTagsAndProjects_whenNoneExist() {
        SkillDetailDto baseDto = SkillDetailDto.builder().name("Java").build();

        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));
        when(skillMapper.toSkillDetailDto(skill)).thenReturn(baseDto);
        when(projectSkillRepository.findAllBySkill(skill)).thenReturn(List.of());

        SkillDetailDto result = skillService.getSkillById(1L);

        assertThat(result.getTags()).isEmpty();
        assertThat(result.getProjects()).isEmpty();
    }

    @Test
    void getSkillById_shouldReturnTagsSorted_whenMultipleTagsExist() {
        SkillTag t1 = SkillTag.builder().tagName("streams").skill(skill).build();
        SkillTag t2 = SkillTag.builder().tagName("concurrency").skill(skill).build();
        SkillTag t3 = SkillTag.builder().tagName("generics").skill(skill).build();
        ReflectionTestUtils.setField(t1, "id", 1L);
        ReflectionTestUtils.setField(t2, "id", 2L);
        ReflectionTestUtils.setField(t3, "id", 3L);
        skill.addSkillTag(t1);
        skill.addSkillTag(t2);
        skill.addSkillTag(t3);

        SkillDetailDto baseDto = SkillDetailDto.builder().name("Java").build();
        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));
        when(skillMapper.toSkillDetailDto(skill)).thenReturn(baseDto);
        when(projectSkillRepository.findAllBySkill(skill)).thenReturn(List.of());

        SkillDetailDto result = skillService.getSkillById(1L);

        assertThat(result.getTags()).containsExactly("concurrency", "generics", "streams");
    }

    @Test
    void getSkillById_shouldThrowNoSuchElementException_whenSkillNotFound() {
        when(skillRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> skillService.getSkillById(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Skill not found");

        verifyNoInteractions(projectSkillRepository, skillMapper);
    }
}
