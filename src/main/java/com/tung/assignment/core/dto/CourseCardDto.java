package com.tung.assignment.core.dto;

import com.tung.assignment.core.entity.Course;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseCardDto {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String color;
    private String icon;
    private String semester;
    private Integer credits;
    private LocalDateTime createdAt;
    private long chunkCount;

    public static CourseCardDto fromEntity(Course course, long chunkCount) {
        return CourseCardDto.builder()
                .id(course.getId())
                .code(course.getCode())
                .name(course.getName())
                .description(course.getDescription())
                .color(course.getColor())
                .icon(course.getIcon())
                .semester(course.getSemester())
                .credits(course.getCredits())
                .createdAt(course.getCreatedAt())
                .chunkCount(chunkCount)
                .build();
    }
}
