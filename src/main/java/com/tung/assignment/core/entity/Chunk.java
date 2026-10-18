package com.tung.assignment.core.entity;

import com.tung.assignment.rag.entity.Document;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "chunks", indexes = {
        @Index(name = "idx_chunks_course", columnList = "course_id"),
        @Index(name = "idx_chunks_doc", columnList = "document_id"),
        @Index(name = "idx_chunks_embedding", columnList = "has_embedding")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ToString.Exclude
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ToString.Exclude
    private Course course;

    @Column(nullable = false)
    private Integer chunkIndex; // Số thứ tự đoạn: 0, 1, 2...

    @Lob
    @Column(nullable = false, columnDefinition = "CLOB")
    private String content; // Nội dung văn bản (300-500 từ)

    @Builder.Default
    private Integer pageNumber = 1; // Vị trí trang PDF gốc để Citation

    @Builder.Default
    private Integer wordCount = 0;

    @Builder.Default
    private Integer tokenCount = 0;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String embeddingVector; // Chuỗi JSON vector 768 chiều

    @Builder.Default
    private Boolean hasEmbedding = false;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.hasEmbedding == null) {
            this.hasEmbedding = false;
        }
        if (this.pageNumber == null) {
            this.pageNumber = 1;
        }
        if (this.wordCount == null) {
            this.wordCount = 0;
        }
        if (this.tokenCount == null) {
            this.tokenCount = 0;
        }
    }
}
