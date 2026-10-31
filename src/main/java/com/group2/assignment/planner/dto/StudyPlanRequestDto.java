package com.group2.assignment.planner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyPlanRequestDto {

    private Long courseId;

    @Builder.Default
    private Integer hoursPerWeek = 6;

    private List<String> selectedDays;

    private LocalDate examDate;

    private String goal;

    private List<String> squadMembers;
}
