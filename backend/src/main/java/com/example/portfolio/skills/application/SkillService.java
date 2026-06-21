package com.example.portfolio.skills.application;

import com.example.portfolio.profile.domain.Profile;
import com.example.portfolio.profile.persistence.ProfileRepository;
import com.example.portfolio.projects.persistence.ProjectSkillRepository;
import com.example.portfolio.skills.domain.Skill;
import com.example.portfolio.skills.domain.SkillTag;
import com.example.portfolio.skills.dto.ProjectInSkillDto;
import com.example.portfolio.skills.dto.SkillDetailDto;
import com.example.portfolio.skills.dto.SkillListItemDto;
import com.example.portfolio.skills.mapper.SkillMapper;
import com.example.portfolio.skills.persistence.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final ProfileRepository profileRepository;
    private final SkillRepository skillRepository;
    private final ProjectSkillRepository projectSkillRepository;
    private final SkillMapper skillMapper;

    @Transactional(readOnly = true)
    public List<SkillListItemDto> getSkillsList() {
        Profile profile = profileRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new NoSuchElementException("Profile not found"));
        return skillRepository.findAllByProfileOrderBySortOrderAscNameAsc(profile)
                .stream()
                .map(skillMapper::toSkillListItemDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SkillDetailDto getSkillById(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Skill not found"));

        SkillDetailDto dto = skillMapper.toSkillDetailDto(skill);

        List<String> tags = skill.getTags().stream()
                .map(SkillTag::getTagName)
                .sorted()
                .toList();

        List<ProjectInSkillDto> projects = projectSkillRepository.findAllBySkill(skill)
                .stream()
                .map(ps -> skillMapper.toProjectInSkillDto(ps.getProject()))
                .toList();

        dto.setTags(tags);
        dto.setProjects(projects);
        return dto;
    }
}
