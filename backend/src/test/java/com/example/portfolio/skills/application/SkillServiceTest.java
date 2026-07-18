package com.example.portfolio.skills.application;

import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.profile.persistence.ProfileRepository;
import com.example.portfolio.projects.domain.Project;
import com.example.portfolio.projects.domain.ProjectSkill;
import com.example.portfolio.projects.domain.ProjectStatus;
import com.example.portfolio.projects.persistence.ProjectSkillRepository;
import com.example.portfolio.skills.domain.LearningProgress;
import com.example.portfolio.skills.domain.LearningStatus;
import com.example.portfolio.skills.domain.ProficiencyLevel;
import com.example.portfolio.skills.domain.Skill;
import com.example.portfolio.skills.domain.SkillCategory;
import com.example.portfolio.skills.domain.SkillTag;
import com.example.portfolio.skills.dto.CreateSkillCategoryRequest;
import com.example.portfolio.skills.dto.CreateSkillRequest;
import com.example.portfolio.skills.dto.LearningProgressRequest;
import com.example.portfolio.skills.dto.ProjectInSkillDto;
import com.example.portfolio.skills.dto.SkillCategoryDto;
import com.example.portfolio.skills.dto.SkillDetailDto;
import com.example.portfolio.skills.dto.SkillListItemDto;
import com.example.portfolio.skills.dto.SkillTagRequest;
import com.example.portfolio.skills.dto.UpdateSkillCategoryRequest;
import com.example.portfolio.skills.dto.UpdateSkillRequest;
import com.example.portfolio.skills.mapper.SkillMapper;
import com.example.portfolio.skills.persistence.LearningProgressRepository;
import com.example.portfolio.skills.persistence.SkillCategoryRepository;
import com.example.portfolio.skills.persistence.SkillRepository;
import com.example.portfolio.skills.persistence.SkillTagRepository;
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
    @Mock private SkillCategoryRepository skillCategoryRepository;
    @Mock private SkillTagRepository skillTagRepository;
    @Mock private LearningProgressRepository learningProgressRepository;
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

    // ── createSkill ───────────────────────────────────────────────────────────

    @Test
    void createSkill_shouldDefaultProficiencyLevelAndExperience_whenNotProvided() {
        CreateSkillRequest request = new CreateSkillRequest();
        request.setName("Kotlin");

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(skillRepository.save(any(Skill.class))).thenAnswer(inv -> inv.getArgument(0));
        when(skillMapper.toSkillDetailDto(any(Skill.class))).thenReturn(SkillDetailDto.builder().build());
        when(projectSkillRepository.findAllBySkill(any(Skill.class))).thenReturn(List.of());

        skillService.createSkill(request);

        var captor = org.mockito.ArgumentCaptor.forClass(Skill.class);
        verify(skillRepository, atLeastOnce()).save(captor.capture());
        Skill saved = captor.getAllValues().get(0);
        assertThat(saved.getProficiency()).isEqualTo(ProficiencyLevel.BEGINNER);
        assertThat(saved.getLevel()).isEqualTo(1);
        assertThat(saved.getYearsOfExperience()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(saved.getHasCertification()).isFalse();
        assertThat(saved.getCategory()).isNull();
    }

    @Test
    void createSkill_shouldFindExistingCategoryByName_insteadOfCreatingDuplicate() {
        CreateSkillRequest request = new CreateSkillRequest();
        request.setName("Kotlin");
        request.setCategoryName("Backend");

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(skillCategoryRepository.findByName("Backend")).thenReturn(Optional.of(category));
        when(skillRepository.save(any(Skill.class))).thenAnswer(inv -> inv.getArgument(0));
        when(skillMapper.toSkillDetailDto(any(Skill.class))).thenReturn(SkillDetailDto.builder().build());
        when(projectSkillRepository.findAllBySkill(any(Skill.class))).thenReturn(List.of());

        skillService.createSkill(request);

        verify(skillCategoryRepository, never()).save(any());
    }

    @Test
    void createSkill_shouldCreateCategory_whenNameDoesNotExist() {
        CreateSkillRequest request = new CreateSkillRequest();
        request.setName("Kotlin");
        request.setCategoryName("Languages");

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(skillCategoryRepository.findByName("Languages")).thenReturn(Optional.empty());
        when(skillCategoryRepository.save(any(SkillCategory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(skillRepository.save(any(Skill.class))).thenAnswer(inv -> inv.getArgument(0));
        when(skillMapper.toSkillDetailDto(any(Skill.class))).thenReturn(SkillDetailDto.builder().build());
        when(projectSkillRepository.findAllBySkill(any(Skill.class))).thenReturn(List.of());

        skillService.createSkill(request);

        var captor = org.mockito.ArgumentCaptor.forClass(SkillCategory.class);
        verify(skillCategoryRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Languages");
    }

    @Test
    void createSkill_shouldThrowNoSuchElementException_whenCategoryIdDoesNotExist() {
        CreateSkillRequest request = new CreateSkillRequest();
        request.setName("Kotlin");
        request.setCategoryId(42L);

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(skillCategoryRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> skillService.createSkill(request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Skill category not found");

        verifyNoInteractions(skillTagRepository, learningProgressRepository);
        verify(skillRepository, never()).save(any());
    }

    @Test
    void createSkill_shouldPersistTagsAndLearningProgress_whenProvided() {
        CreateSkillRequest request = new CreateSkillRequest();
        request.setName("Kotlin");
        SkillTagRequest tagRequest = new SkillTagRequest();
        tagRequest.setTagName("jvm");
        request.setTags(List.of(tagRequest));
        LearningProgressRequest lpRequest = new LearningProgressRequest();
        lpRequest.setName("Coroutines course");
        request.setLearningProgresses(List.of(lpRequest));

        when(profileRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(profile));
        when(skillRepository.save(any(Skill.class))).thenAnswer(inv -> inv.getArgument(0));
        when(skillTagRepository.save(any(SkillTag.class))).thenAnswer(inv -> inv.getArgument(0));
        when(learningProgressRepository.save(any(LearningProgress.class))).thenAnswer(inv -> inv.getArgument(0));
        when(skillMapper.toSkillDetailDto(any(Skill.class))).thenReturn(SkillDetailDto.builder().build());
        when(projectSkillRepository.findAllBySkill(any(Skill.class))).thenReturn(List.of());

        SkillDetailDto result = skillService.createSkill(request);

        var tagCaptor = org.mockito.ArgumentCaptor.forClass(SkillTag.class);
        verify(skillTagRepository).save(tagCaptor.capture());
        assertThat(tagCaptor.getValue().getTagName()).isEqualTo("jvm");

        var lpCaptor = org.mockito.ArgumentCaptor.forClass(LearningProgress.class);
        verify(learningProgressRepository).save(lpCaptor.capture());
        assertThat(lpCaptor.getValue().getName()).isEqualTo("Coroutines course");
        assertThat(lpCaptor.getValue().getStatus()).isEqualTo(LearningStatus.NOT_STARTED);

        assertThat(result.getTags()).containsExactly("jvm");
    }

    // ── updateSkill ───────────────────────────────────────────────────────────

    @Test
    void updateSkill_shouldOnlyChangeProvidedFields_leavingOthersUntouched() {
        ReflectionTestUtils.setField(skill, "id", 1L);
        UpdateSkillRequest request = new UpdateSkillRequest();
        request.setLevel(50);

        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));
        when(skillRepository.save(any(Skill.class))).thenAnswer(inv -> inv.getArgument(0));
        when(skillMapper.toSkillDetailDto(any(Skill.class))).thenReturn(SkillDetailDto.builder().build());
        when(projectSkillRepository.findAllBySkill(any(Skill.class))).thenReturn(List.of());

        skillService.updateSkill(1L, request);

        assertThat(skill.getLevel()).isEqualTo(50);
        assertThat(skill.getName()).isEqualTo("Java");
        assertThat(skill.getProficiency()).isEqualTo(ProficiencyLevel.EXPERT);
        verify(skillCategoryRepository, never()).findById(any());
        verify(skillCategoryRepository, never()).findByName(any());
    }

    @Test
    void updateSkill_shouldRenameSharedCategory_whenIdAndDifferingNameProvided() {
        ReflectionTestUtils.setField(skill, "id", 1L);
        ReflectionTestUtils.setField(category, "id", 5L);
        UpdateSkillRequest request = new UpdateSkillRequest();
        request.setCategoryId(5L);
        request.setCategoryName("Backend Engineering");

        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));
        when(skillCategoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(skillCategoryRepository.save(any(SkillCategory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(skillRepository.save(any(Skill.class))).thenAnswer(inv -> inv.getArgument(0));
        when(skillMapper.toSkillDetailDto(any(Skill.class))).thenReturn(SkillDetailDto.builder().build());
        when(projectSkillRepository.findAllBySkill(any(Skill.class))).thenReturn(List.of());

        skillService.updateSkill(1L, request);

        assertThat(category.getName()).isEqualTo("Backend Engineering");
        verify(skillCategoryRepository).save(category);
    }

    @Test
    void updateSkill_shouldNotRenameCategory_whenNameMatchesCurrent() {
        ReflectionTestUtils.setField(skill, "id", 1L);
        ReflectionTestUtils.setField(category, "id", 5L);
        UpdateSkillRequest request = new UpdateSkillRequest();
        request.setCategoryId(5L);
        request.setCategoryName("Backend");

        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));
        when(skillCategoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(skillRepository.save(any(Skill.class))).thenAnswer(inv -> inv.getArgument(0));
        when(skillMapper.toSkillDetailDto(any(Skill.class))).thenReturn(SkillDetailDto.builder().build());
        when(projectSkillRepository.findAllBySkill(any(Skill.class))).thenReturn(List.of());

        skillService.updateSkill(1L, request);

        verify(skillCategoryRepository, never()).save(any());
    }

    @Test
    void updateSkill_shouldUpdateExistingTagAndCreateNewOne_whenSyncingTags() {
        ReflectionTestUtils.setField(skill, "id", 1L);
        SkillTag existingTag = SkillTag.builder().skill(skill).tagName("old-name").build();
        ReflectionTestUtils.setField(existingTag, "id", 10L);

        SkillTagRequest updateExisting = new SkillTagRequest();
        updateExisting.setId(10L);
        updateExisting.setTagName("renamed");
        SkillTagRequest createNew = new SkillTagRequest();
        createNew.setTagName("brand-new");

        UpdateSkillRequest request = new UpdateSkillRequest();
        request.setTags(List.of(updateExisting, createNew));

        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));
        when(skillRepository.save(any(Skill.class))).thenAnswer(inv -> inv.getArgument(0));
        when(skillTagRepository.findAllBySkill(skill)).thenReturn(List.of(existingTag));
        when(skillTagRepository.save(any(SkillTag.class))).thenAnswer(inv -> inv.getArgument(0));
        when(skillMapper.toSkillDetailDto(any(Skill.class))).thenReturn(SkillDetailDto.builder().build());
        when(projectSkillRepository.findAllBySkill(any(Skill.class))).thenReturn(List.of());

        skillService.updateSkill(1L, request);

        assertThat(existingTag.getTagName()).isEqualTo("renamed");
        verify(skillTagRepository, never()).delete(existingTag);
        var captor = org.mockito.ArgumentCaptor.forClass(SkillTag.class);
        verify(skillTagRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(SkillTag::getTagName)
                .containsExactlyInAnyOrder("renamed", "brand-new");
    }

    @Test
    void updateSkill_shouldDeleteTagsNotPresentInRequest() {
        ReflectionTestUtils.setField(skill, "id", 1L);
        SkillTag toRemove = SkillTag.builder().skill(skill).tagName("obsolete").build();
        ReflectionTestUtils.setField(toRemove, "id", 20L);
        skill.addSkillTag(toRemove);

        UpdateSkillRequest request = new UpdateSkillRequest();
        request.setTags(List.of());

        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));
        when(skillRepository.save(any(Skill.class))).thenAnswer(inv -> inv.getArgument(0));
        when(skillTagRepository.findAllBySkill(skill)).thenReturn(List.of(toRemove));
        when(skillMapper.toSkillDetailDto(any(Skill.class))).thenReturn(SkillDetailDto.builder().build());
        when(projectSkillRepository.findAllBySkill(any(Skill.class))).thenReturn(List.of());

        skillService.updateSkill(1L, request);

        verify(skillTagRepository).delete(toRemove);
        assertThat(skill.getTags()).doesNotContain(toRemove);
    }

    @Test
    void updateSkill_shouldMergeLearningProgressById_updatingExistingAndDeletingMissing() {
        ReflectionTestUtils.setField(skill, "id", 1L);
        LearningProgress keep = LearningProgress.builder().skill(skill).name("Course A").status(LearningStatus.IN_PROGRESS).build();
        ReflectionTestUtils.setField(keep, "id", 30L);
        LearningProgress remove = LearningProgress.builder().skill(skill).name("Course B").build();
        ReflectionTestUtils.setField(remove, "id", 31L);

        LearningProgressRequest updateKeep = new LearningProgressRequest();
        updateKeep.setId(30L);
        updateKeep.setName("Course A");
        updateKeep.setStatus(LearningStatus.COMPLETED);

        UpdateSkillRequest request = new UpdateSkillRequest();
        request.setLearningProgresses(List.of(updateKeep));

        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));
        when(skillRepository.save(any(Skill.class))).thenAnswer(inv -> inv.getArgument(0));
        when(learningProgressRepository.findAllBySkill(skill)).thenReturn(List.of(keep, remove));
        when(learningProgressRepository.save(any(LearningProgress.class))).thenAnswer(inv -> inv.getArgument(0));
        when(skillMapper.toSkillDetailDto(any(Skill.class))).thenReturn(SkillDetailDto.builder().build());
        when(projectSkillRepository.findAllBySkill(any(Skill.class))).thenReturn(List.of());

        skillService.updateSkill(1L, request);

        assertThat(keep.getStatus()).isEqualTo(LearningStatus.COMPLETED);
        verify(learningProgressRepository).delete(remove);
        verify(learningProgressRepository, never()).delete(keep);
    }

    @Test
    void updateSkill_shouldThrowNoSuchElementException_whenSkillNotFound() {
        UpdateSkillRequest request = new UpdateSkillRequest();

        when(skillRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> skillService.updateSkill(99L, request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Skill not found");
    }

    // ── deleteSkill ───────────────────────────────────────────────────────────

    @Test
    void deleteSkill_shouldDeleteSkill_whenFound() {
        ReflectionTestUtils.setField(skill, "id", 1L);
        when(skillRepository.findById(1L)).thenReturn(Optional.of(skill));

        skillService.deleteSkill(1L);

        verify(skillRepository).delete(skill);
    }

    @Test
    void deleteSkill_shouldThrowNoSuchElementException_whenNotFound() {
        when(skillRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> skillService.deleteSkill(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Skill not found");

        verify(skillRepository, never()).delete(any());
    }

    // ── category CRUD ─────────────────────────────────────────────────────────

    @Test
    void createCategory_shouldBuildCategoryWithDefaults_whenOptionalFieldsMissing() {
        CreateSkillCategoryRequest request = new CreateSkillCategoryRequest();
        request.setName("Frontend");

        when(skillCategoryRepository.save(any(SkillCategory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(skillMapper.toSkillCategoryDto(any(SkillCategory.class)))
                .thenReturn(SkillCategoryDto.builder().name("Frontend").sortOrder(0).build());

        SkillCategoryDto result = skillService.createCategory(request);

        var captor = org.mockito.ArgumentCaptor.forClass(SkillCategory.class);
        verify(skillCategoryRepository).save(captor.capture());
        assertThat(captor.getValue().getSortOrder()).isEqualTo(0);
        assertThat(captor.getValue().getParent()).isNull();
        assertThat(result.getName()).isEqualTo("Frontend");
    }

    @Test
    void updateCategory_shouldOnlyChangeProvidedFields() {
        ReflectionTestUtils.setField(category, "id", 5L);
        category.setSortOrder(3);
        UpdateSkillCategoryRequest request = new UpdateSkillCategoryRequest();
        request.setDescription("Updated description");

        when(skillCategoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(skillCategoryRepository.save(any(SkillCategory.class))).thenAnswer(inv -> inv.getArgument(0));
        when(skillMapper.toSkillCategoryDto(any(SkillCategory.class))).thenReturn(SkillCategoryDto.builder().build());

        skillService.updateCategory(5L, request);

        assertThat(category.getName()).isEqualTo("Backend");
        assertThat(category.getDescription()).isEqualTo("Updated description");
        assertThat(category.getSortOrder()).isEqualTo(3);
    }

    @Test
    void deleteCategory_shouldNullOutCategoryOnAllReferencingSkills_beforeDeletingCategory() {
        ReflectionTestUtils.setField(category, "id", 5L);
        Skill otherSkill = Skill.builder().name("Spring").profile(profile).category(category)
                .proficiency(ProficiencyLevel.ADVANCED).build();

        when(skillCategoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(skillRepository.findAllByCategory(category)).thenReturn(List.of(skill, otherSkill));

        skillService.deleteCategory(5L);

        assertThat(skill.getCategory()).isNull();
        assertThat(otherSkill.getCategory()).isNull();
        verify(skillRepository).saveAll(List.of(skill, otherSkill));
        verify(skillCategoryRepository).delete(category);
    }

    @Test
    void deleteCategory_shouldThrowNoSuchElementException_whenCategoryNotFound() {
        when(skillCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> skillService.deleteCategory(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Skill category not found");

        verify(skillCategoryRepository, never()).delete(any());
        verifyNoInteractions(skillRepository);
    }

    @Test
    void getAllCategories_shouldMapEveryCategory() {
        ReflectionTestUtils.setField(category, "id", 1L);
        SkillCategory other = SkillCategory.builder().name("Frontend").build();
        ReflectionTestUtils.setField(other, "id", 2L);
        when(skillCategoryRepository.findAll()).thenReturn(List.of(category, other));
        when(skillMapper.toSkillCategoryDto(category)).thenReturn(SkillCategoryDto.builder().name("Backend").build());
        when(skillMapper.toSkillCategoryDto(other)).thenReturn(SkillCategoryDto.builder().name("Frontend").build());

        List<SkillCategoryDto> result = skillService.getAllCategories();

        assertThat(result).extracting(SkillCategoryDto::getName).containsExactly("Backend", "Frontend");
    }
}
