package com.group2.assignment.rag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChunkDto {
    private Long id;
    private Integer chunkIndex;
    private String content;
    private Integer pageNumber;
    private Integer wordCount;
    private Integer tokenCount;
    private Long documentId;
    private String documentFileName;
}
