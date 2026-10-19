package com.group2.assignment.rag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitationDto {
    private Integer chunkIndex;
    private Integer pageNumber;
    private String documentFileName;
    private String snippet;
}
