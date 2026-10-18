package com.group2.assignment.quiz.dto;

import lombok.*;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizSubmissionDto {
    private String studentName;
    private Integer timeSpentSeconds;
    @Builder.Default
    private Map<Long, String> answers = new HashMap<>();
}
