package com.example.portfolio.skills.api;

import com.example.portfolio.auth.application.JwtService;
import com.example.portfolio.auth.persistence.UserRepository;
import com.example.portfolio.hobby.domain.ComplexityLevel;
import com.example.portfolio.projects.domain.ProjectStatus;
import com.example.portfolio.shared.config.SecurityConfig;
import com.example.portfolio.skills.application.SkillService;
import com.example.portfolio.skills.domain.ProficiencyLevel;
import com.example.portfolio.skills.dto.ProjectInSkillDto;
import com.example.portfolio.skills.dto.SkillDetailDto;
import com.example.portfolio.skills.dto.SkillListItemDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SkillController.class)
@Import(SecurityConfig.class)
@MockBean(JpaMetamodelMappingContext.class)
class SkillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SkillService skillService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    private List<SkillListItemDto> buildSkillList() {
        return List.of(
                SkillListItemDto.builder()
                        .id(1L).name("Java").categoryName("Backend")
                        .proficiency(ProficiencyLevel.EXPERT).level(90)
                        .yearsOfExperience(new BigDecimal("5.0"))
                        .description("Primary language").hasCertification(true).learning(false)
                        .build(),
                SkillListItemDto.builder()
                        .id(2L).name("Spring").categoryName("Backend")
                        .proficiency(ProficiencyLevel.ADVANCED).level(80)
                        .yearsOfExperience(new BigDecimal("3.0"))
                        .hasCertification(false).learning(false)
                        .build()
        );
    }

    private SkillDetailDto buildSkillDetail() {
        return SkillDetailDto.builder()
                .id(1L).name("Java").categoryName("Backend")
                .proficiency(ProficiencyLevel.EXPERT).level(90)
                .yearsOfExperience(new BigDecimal("5.0"))
                .description("Primary language")
                .lastUsedDate(LocalDate.of(2024, 1, 1))
                .hasCertification(true).learning(false)
                .tags(List.of("jvm", "oop"))
                .projects(List.of(
                        ProjectInSkillDto.builder()
                                .id(10L).title("Portfolio API")
                                .status(ProjectStatus.PRODUCTION).complexity(ComplexityLevel.ADVANCED)
                                .year(2024).githubUrl("https://github.com/portfolio")
                                .build()
                ))
                .build();
    }

    // ── GET /skills ───────────────────────────────────────────────────────────

    @Test
    void getSkills_shouldReturn200WithSkillList() throws Exception {
        when(skillService.getSkillsList()).thenReturn(buildSkillList());

        mockMvc.perform(get("/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getSkills_shouldReturn200WithCorrectSkillFields() throws Exception {
        when(skillService.getSkillsList()).thenReturn(buildSkillList());

        mockMvc.perform(get("/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Java"))
                .andExpect(jsonPath("$[0].categoryName").value("Backend"))
                .andExpect(jsonPath("$[0].proficiency").value("EXPERT"))
                .andExpect(jsonPath("$[0].level").value(90))
                .andExpect(jsonPath("$[0].hasCertification").value(true));
    }

    @Test
    void getSkills_shouldReturn200WithEmptyList_whenNoSkillsExist() throws Exception {
        when(skillService.getSkillsList()).thenReturn(List.of());

        mockMvc.perform(get("/skills"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getSkills_shouldReturn404_whenProfileNotFound() throws Exception {
        when(skillService.getSkillsList()).thenThrow(new NoSuchElementException("Profile not found"));

        mockMvc.perform(get("/skills"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Profile not found"));
    }

    @Test
    void getSkills_shouldBeAccessibleWithoutAuthentication() throws Exception {
        when(skillService.getSkillsList()).thenReturn(List.of());

        mockMvc.perform(get("/skills"))
                .andExpect(status().isOk());

        verify(jwtService, never()).extractEmail(any());
    }

    // ── GET /skills/{id} ──────────────────────────────────────────────────────

    @Test
    void getSkill_shouldReturn200WithFullDetail() throws Exception {
        when(skillService.getSkillById(1L)).thenReturn(buildSkillDetail());

        mockMvc.perform(get("/skills/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Java"))
                .andExpect(jsonPath("$.categoryName").value("Backend"))
                .andExpect(jsonPath("$.proficiency").value("EXPERT"))
                .andExpect(jsonPath("$.level").value(90))
                .andExpect(jsonPath("$.description").value("Primary language"))
                .andExpect(jsonPath("$.hasCertification").value(true))
                .andExpect(jsonPath("$.learning").value(false));
    }

    @Test
    void getSkill_shouldReturn200WithTagsAndProjects() throws Exception {
        when(skillService.getSkillById(1L)).thenReturn(buildSkillDetail());

        mockMvc.perform(get("/skills/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags.length()").value(2))
                .andExpect(jsonPath("$.tags[0]").value("jvm"))
                .andExpect(jsonPath("$.tags[1]").value("oop"))
                .andExpect(jsonPath("$.projects").isArray())
                .andExpect(jsonPath("$.projects.length()").value(1))
                .andExpect(jsonPath("$.projects[0].title").value("Portfolio API"))
                .andExpect(jsonPath("$.projects[0].status").value("PRODUCTION"))
                .andExpect(jsonPath("$.projects[0].year").value(2024));
    }

    @Test
    void getSkill_shouldReturn200WithEmptyTagsAndProjects_whenNoneExist() throws Exception {
        SkillDetailDto emptyDetail = SkillDetailDto.builder()
                .id(1L).name("Java").proficiency(ProficiencyLevel.EXPERT)
                .tags(List.of()).projects(List.of())
                .build();
        when(skillService.getSkillById(1L)).thenReturn(emptyDetail);

        mockMvc.perform(get("/skills/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags").isEmpty())
                .andExpect(jsonPath("$.projects").isArray())
                .andExpect(jsonPath("$.projects").isEmpty());
    }

    @Test
    void getSkill_shouldReturn404_whenSkillNotFound() throws Exception {
        when(skillService.getSkillById(99L)).thenThrow(new NoSuchElementException("Skill not found"));

        mockMvc.perform(get("/skills/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Skill not found"));
    }

    @Test
    void getSkill_shouldBeAccessibleWithoutAuthentication() throws Exception {
        when(skillService.getSkillById(1L)).thenReturn(buildSkillDetail());

        mockMvc.perform(get("/skills/1"))
                .andExpect(status().isOk());

        verify(jwtService, never()).extractEmail(any());
    }
}
