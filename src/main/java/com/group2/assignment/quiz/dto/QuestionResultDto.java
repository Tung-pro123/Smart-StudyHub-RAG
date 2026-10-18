package com.group2.assignment.quiz.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResultDto {
    private Long questionId;
    private Integer sortOrder;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String selectedAnswer;
    private String correctAnswer;
    private boolean isCorrect;
    private String explanation;
}
