package com.example.portfolio.education.mapper;

import com.example.portfolio.education.domain.Course;
import com.example.portfolio.education.domain.Education;
import com.example.portfolio.education.domain.EducationAchievement;
import com.example.portfolio.education.dto.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface EducationMapper {

    EducationDto toEducationDto(Education education);

    @Mapping(target = "courses", ignore = true)
    @Mapping(target = "achievements", ignore = true)
    EducationDetailDto toEducationDetailDto(Education education);

    CourseDto toCourseDto(Course course);

    @Mapping(source = "achievement.id", target = "achievementId")
    @Mapping(source = "achievement.title", target = "achievementTitle")
    @Mapping(source = "course.id", target = "courseId")
    @Mapping(source = "course.title", target = "courseTitle")
    AchievementInEducationDto toAchievementInEducationDto(EducationAchievement educationAchievement);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "courses", ignore = true)
    @Mapping(target = "achievements", ignore = true)
    void updateEducation(UpdateEducationRequest request, @MappingTarget Education education);
}
