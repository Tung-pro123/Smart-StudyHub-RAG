package com.group2.assignment.quiz.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResultDto {
    private Long attemptId;
    private Long quizId;
    private String quizTitle;
    private String courseName;
    private String studentName;
    private Double totalScore;
    private Integer correctCount;
    private Integer totalCount;
    private Integer timeSpentSeconds;
    private LocalDateTime completedAt;
    private List<QuestionResultDto> details;
}
