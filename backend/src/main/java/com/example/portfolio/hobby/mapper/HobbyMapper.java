package com.example.portfolio.hobby.mapper;

import com.example.portfolio.hobby.domain.Hobby;
import com.example.portfolio.hobby.domain.HobbySkill;
import com.example.portfolio.hobby.dto.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface HobbyMapper {

    HobbyDto toHobbyDto(Hobby hobby);

    @Mapping(target = "skills", ignore = true)
    HobbyDetailDto toHobbyDetailDto(Hobby hobby);

    @Mapping(source = "skill.id", target = "skillId")
    @Mapping(source = "skill.name", target = "skillName")
    SkillInHobbyDto toSkillInHobbyDto(HobbySkill hobbySkill);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "hobbySkills", ignore = true)
    void updateHobby(UpdateHobbyRequest request, @MappingTarget Hobby hobby);
}
