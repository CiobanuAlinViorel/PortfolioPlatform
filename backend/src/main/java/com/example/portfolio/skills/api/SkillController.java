package com.example.portfolio.skills.api;

import com.example.portfolio.skills.application.SkillService;
import com.example.portfolio.skills.dto.SkillDetailDto;
import com.example.portfolio.skills.dto.SkillListItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    @GetMapping
    public ResponseEntity<List<SkillListItemDto>> getSkills() {
        return ResponseEntity.ok(skillService.getSkillsList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SkillDetailDto> getSkill(@PathVariable Long id) {
        return ResponseEntity.ok(skillService.getSkillById(id));
    }
}
