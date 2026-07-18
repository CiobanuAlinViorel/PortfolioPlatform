package com.example.portfolio.skills.application;

import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.profile.persistence.ProfileRepository;
import com.example.portfolio.projects.persistence.ProjectSkillRepository;
import com.example.portfolio.shared.base.BaseEntity;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final ProfileRepository profileRepository;
    private final SkillRepository skillRepository;
    private final SkillCategoryRepository skillCategoryRepository;
    private final SkillTagRepository skillTagRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final ProjectSkillRepository projectSkillRepository;
    private final SkillMapper skillMapper;

    // ─────────────────────────── READ ────────────────────────────

    @Transactional(readOnly = true)
    public List<SkillListItemDto> getSkillsList() {
        Profile profile = getProfile();
        return skillRepository.findAllByProfileOrderBySortOrderAscNameAsc(profile)
                .stream()
                .map(skillMapper::toSkillListItemDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SkillDetailDto getSkillById(Long id) {
        return buildDetailDto(findSkill(id));
    }

    // ─────────────────────────── CREATE ──────────────────────────

    @Transactional
    public SkillDetailDto createSkill(CreateSkillRequest request) {
        Profile profile = getProfile();
        SkillCategory category = resolveOrCreateCategory(request.getCategoryId(), request.getCategoryName());

        Skill skill = Skill.builder()
                .profile(profile)
                .name(request.getName())
                .category(category)
                .proficiency(request.getProficiency() != null ? request.getProficiency() : ProficiencyLevel.BEGINNER)
                .level(request.getLevel() != null ? request.getLevel() : 1)
                .yearsOfExperience(request.getYearsOfExperience() != null ? request.getYearsOfExperience() : BigDecimal.ZERO)
                .description(request.getDescription())
                .lastUsedDate(request.getLastUsedDate())
                .hasCertification(Boolean.TRUE.equals(request.getHasCertification()))
                .learning(Boolean.TRUE.equals(request.getLearning()))
                .sortOrder(request.getSortOrder())
                .build();

        skill = skillRepository.save(skill);

        if (request.getTags() != null) {
            for (SkillTagRequest tr : request.getTags()) {
                skill.addSkillTag(skillTagRepository.save(
                        SkillTag.builder().skill(skill).tagName(tr.getTagName()).build()));
            }
        }

        if (request.getLearningProgresses() != null) {
            for (LearningProgressRequest lpr : request.getLearningProgresses()) {
                skill.addLearningProgress(learningProgressRepository.save(buildLearningProgress(lpr, skill)));
            }
        }

        return buildDetailDto(skill);
    }

    // ─────────────────────────── UPDATE ──────────────────────────

    @Transactional
    public SkillDetailDto updateSkill(Long id, UpdateSkillRequest request) {
        Skill skill = findSkill(id);

        if (request.getName() != null) skill.setName(request.getName());
        if (request.getProficiency() != null) skill.setProficiency(request.getProficiency());
        if (request.getLevel() != null) skill.setLevel(request.getLevel());
        if (request.getYearsOfExperience() != null) skill.setYearsOfExperience(request.getYearsOfExperience());
        if (request.getDescription() != null) skill.setDescription(request.getDescription());
        if (request.getLastUsedDate() != null) skill.setLastUsedDate(request.getLastUsedDate());
        if (request.getHasCertification() != null) skill.setHasCertification(request.getHasCertification());
        if (request.getLearning() != null) skill.setLearning(request.getLearning());
        if (request.getSortOrder() != null) skill.setSortOrder(request.getSortOrder());
        if (request.getCategoryId() != null || request.getCategoryName() != null) {
            skill.setCategory(resolveOrCreateCategory(request.getCategoryId(), request.getCategoryName()));
        }

        skillRepository.save(skill);

        if (request.getTags() != null) syncTags(skill, request.getTags());
        if (request.getLearningProgresses() != null) syncLearningProgress(skill, request.getLearningProgresses());

        return buildDetailDto(skill);
    }

    // ─────────────────────────── DELETE ──────────────────────────

    @Transactional
    public void deleteSkill(Long id) {
        // cascade = ALL on Skill.tags/learningProgress removes them along with the skill
        skillRepository.delete(findSkill(id));
    }

    // ─────────────────── CATEGORY CRUD ───────────────────────────

    @Transactional
    public SkillCategoryDto createCategory(CreateSkillCategoryRequest request) {
        SkillCategory category = SkillCategory.builder()
                .name(request.getName())
                .description(request.getDescription())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();
        return skillMapper.toSkillCategoryDto(skillCategoryRepository.save(category));
    }

    @Transactional
    public SkillCategoryDto updateCategory(Long id, UpdateSkillCategoryRequest request) {
        SkillCategory category = findCategory(id);
        if (request.getName() != null) category.setName(request.getName());
        if (request.getDescription() != null) category.setDescription(request.getDescription());
        if (request.getSortOrder() != null) category.setSortOrder(request.getSortOrder());
        return skillMapper.toSkillCategoryDto(skillCategoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        SkillCategory category = findCategory(id);
        List<Skill> affectedSkills = skillRepository.findAllByCategory(category);
        affectedSkills.forEach(s -> s.setCategory(null));
        skillRepository.saveAll(affectedSkills);
        skillCategoryRepository.delete(category);
    }

    @Transactional(readOnly = true)
    public SkillCategoryDto getCategory(Long id) {
        return skillMapper.toSkillCategoryDto(findCategory(id));
    }

    @Transactional(readOnly = true)
    public List<SkillCategoryDto> getAllCategories() {
        return skillCategoryRepository.findAll()
                .stream()
                .map(skillMapper::toSkillCategoryDto)
                .toList();
    }

    // ─────────────────── SYNC HELPERS ────────────────────────────

    private void syncTags(Skill skill, List<SkillTagRequest> requests) {
        List<SkillTag> existing = skillTagRepository.findAllBySkill(skill);
        Map<Long, SkillTag> existingById = existing.stream()
                .collect(Collectors.toMap(BaseEntity::getId, t -> t));
        Set<Long> keepIds = new HashSet<>();

        for (SkillTagRequest tr : requests) {
            if (tr.getId() != null && existingById.containsKey(tr.getId())) {
                SkillTag tag = existingById.get(tr.getId());
                if (tr.getTagName() != null) tag.setTagName(tr.getTagName());
                skillTagRepository.save(tag);
                keepIds.add(tag.getId());
            } else {
                SkillTag tag = skillTagRepository.save(
                        SkillTag.builder().skill(skill).tagName(tr.getTagName()).build());
                skill.addSkillTag(tag);
                keepIds.add(tag.getId());
            }
        }

        existing.stream()
                .filter(t -> !keepIds.contains(t.getId()))
                .forEach(t -> { skill.removeSkillTag(t); skillTagRepository.delete(t); });
    }

    private void syncLearningProgress(Skill skill, List<LearningProgressRequest> requests) {
        List<LearningProgress> existing = learningProgressRepository.findAllBySkill(skill);
        Map<Long, LearningProgress> existingById = existing.stream()
                .collect(Collectors.toMap(BaseEntity::getId, lp -> lp));
        Set<Long> keepIds = new HashSet<>();

        for (LearningProgressRequest lpr : requests) {
            if (lpr.getId() != null && existingById.containsKey(lpr.getId())) {
                LearningProgress lp = existingById.get(lpr.getId());
                if (lpr.getName() != null) lp.setName(lpr.getName());
                if (lpr.getStatus() != null) lp.setStatus(lpr.getStatus());
                if (lpr.getStartDate() != null) lp.setStartDate(lpr.getStartDate());
                if (lpr.getCompletionDate() != null) lp.setCompletionDate(lpr.getCompletionDate());
                if (lpr.getProgressPercentage() != null) lp.setProgressPercentage(lpr.getProgressPercentage());
                if (lpr.getTimeSpentHours() != null) lp.setTimeSpentHours(lpr.getTimeSpentHours());
                if (lpr.getEstimatedCompletion() != null) lp.setEstimatedCompletion(lpr.getEstimatedCompletion());
                if (lpr.getDescription() != null) lp.setDescription(lpr.getDescription());
                learningProgressRepository.save(lp);
                keepIds.add(lp.getId());
            } else {
                LearningProgress lp = learningProgressRepository.save(buildLearningProgress(lpr, skill));
                skill.addLearningProgress(lp);
                keepIds.add(lp.getId());
            }
        }

        existing.stream()
                .filter(lp -> !keepIds.contains(lp.getId()))
                .forEach(lp -> { skill.removeLearningProgress(lp); learningProgressRepository.delete(lp); });
    }

    // ─────────────────── BUILDER / RESOLVE HELPERS ────────────────

    private LearningProgress buildLearningProgress(LearningProgressRequest lpr, Skill skill) {
        return LearningProgress.builder()
                .skill(skill)
                .name(lpr.getName())
                .status(lpr.getStatus() != null ? lpr.getStatus() : LearningStatus.NOT_STARTED)
                .startDate(lpr.getStartDate())
                .completionDate(lpr.getCompletionDate())
                .progressPercentage(lpr.getProgressPercentage() != null ? lpr.getProgressPercentage() : 0)
                .timeSpentHours(lpr.getTimeSpentHours() != null ? lpr.getTimeSpentHours() : BigDecimal.ZERO)
                .estimatedCompletion(lpr.getEstimatedCompletion())
                .description(lpr.getDescription())
                .build();
    }

    /**
     * Find-or-create by name; if an id is given and a differing name is supplied alongside it,
     * the shared category row is renamed in place (affects every skill referencing it).
     */
    private SkillCategory resolveOrCreateCategory(Long categoryId, String categoryName) {

        if(categoryId != null) {
            SkillCategory category = skillCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NoSuchElementException("Skill category not found"));
            if (categoryName != null && !categoryName.isBlank() && !categoryName.equals(category.getName())) {
                category.setName(categoryName);
                skillCategoryRepository.save(category);
            }
            return category;
        }
        else if (categoryName != null && !categoryName.isBlank()) {
            return skillCategoryRepository.findByName(categoryName)
                    .orElseGet(() -> skillCategoryRepository.save(
                            SkillCategory.builder().name(categoryName).build()));
        }
        return null;
    }

    private Profile getProfile() {
        return profileRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new NoSuchElementException("Profile not found"));
    }

    private Skill findSkill(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Skill not found"));
    }

    private SkillCategory findCategory(Long id) {
        return skillCategoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Skill category not found"));
    }

    private SkillDetailDto buildDetailDto(Skill skill) {
        SkillDetailDto dto = skillMapper.toSkillDetailDto(skill);

        dto.setTags(skill.getTags().stream()
                .map(SkillTag::getTagName)
                .sorted()
                .toList());

        List<ProjectInSkillDto> projects = projectSkillRepository.findAllBySkill(skill)
                .stream()
                .map(ps -> skillMapper.toProjectInSkillDto(ps.getProject()))
                .toList();
        dto.setProjects(projects);

        dto.setLearningProgress(skill.getLearningProgress().stream()
                .map(skillMapper::toLearningProgressDto)
                .toList());

        return dto;
    }
}
