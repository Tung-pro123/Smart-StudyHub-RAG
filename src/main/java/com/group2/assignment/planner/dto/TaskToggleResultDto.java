package com.group2.assignment.planner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskToggleResultDto {

    private Long taskId;
    private boolean completed;
    private int progressPercent;
    private String planStatus;
}
