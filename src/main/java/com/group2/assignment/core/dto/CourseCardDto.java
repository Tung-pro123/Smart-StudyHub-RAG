package com.group2.assignment.core.dto;

import com.group2.assignment.core.entity.Course;
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

    // Thông tin người sở hữu
    private Long ownerId;
    private String ownerUsername;
    private String ownerFullName;

    public boolean isOwnedBy(Long userId) {
        return ownerId != null && ownerId.equals(userId);
    }

    public boolean isSystemSample() {
        return ownerId == null;
    }

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
                .ownerId(course.getUser() != null ? course.getUser().getId() : null)
                .ownerUsername(course.getUser() != null ? course.getUser().getUsername() : null)
                .ownerFullName(course.getUser() != null ? course.getUser().getFullName() : null)
                .build();
    }
}
