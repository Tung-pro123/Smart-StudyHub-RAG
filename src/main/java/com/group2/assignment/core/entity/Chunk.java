package com.group2.assignment.core.entity;

import com.group2.assignment.rag.entity.Document;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

/**
 * Tóm tắt nhanh các thuộc tính của Entity {@link Chunk} (bảng {@code chunks}):
 * <ul>
 *   <li>{@code id}: Khóa chính tự tăng (IDENTITY).</li>
 *   <li>{@code document}: Tài liệu PDF cha chứa chunk (ManyToOne LAZY, CASCADE delete).</li>
 *   <li>{@code course}: Môn học sở hữu chunk, dùng truy vấn chunk theo môn (ManyToOne LAZY, CASCADE delete).</li>
 *   <li>{@code chunkIndex}: Thứ tự đoạn chunk trong tài liệu (0, 1, 2...).</li>
 *   <li>{@code content}: Nội dung text đã tách (300–500 từ, lưu dạng CLOB).</li>
 *   <li>{@code pageNumber}: Số trang PDF gốc (mặc định 1) để hiển thị trích dẫn (Citation).</li>
 *   <li>{@code wordCount}: Số từ trong đoạn {@code content} (mặc định 0).</li>
 *   <li>{@code tokenCount}: Số token ước lượng (~1.3 × số từ, mặc định 0).</li>
 *   <li>{@code embeddingVector}: Chuỗi JSON vector 768 chiều (CLOB, dự phòng cho Vector Search).</li>
 *   <li>{@code hasEmbedding}: Cờ đánh dấu đã sinh vector nhúng chưa (mặc định {@code false}, hiện truy xuất bằng Lexical).</li>
 *   <li>{@code createdAt}: Thời điểm tạo bản ghi (tự gán khi {@code @PrePersist}).</li>
 *   <li>{@code getCharCount()}: Thuộc tính {@code @Transient} (không lưu DB), trả về số ký tự của {@code content}.</li>
 * </ul>
 */
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

    @Transient
    public int getCharCount() {
        return content != null ? content.length() : 0;
    }
}
