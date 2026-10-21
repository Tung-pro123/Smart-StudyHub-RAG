package com.group2.assignment.rag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDto {
    private String answer;
    private Long sessionId;
    private String sessionTitle;
    @Builder.Default
    private List<CitationDto> citations = new ArrayList<>();
    private LocalDateTime createdAt;
    @Builder.Default
    private boolean success = true;
    private String errorMessage;
}
