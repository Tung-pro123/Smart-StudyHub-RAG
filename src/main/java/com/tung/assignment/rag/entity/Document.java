package com.tung.assignment.rag.entity;

import com.tung.assignment.core.entity.Course;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents", indexes = {
        @Index(name = "idx_documents_course", columnList = "course_id"),
        @Index(name = "idx_documents_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ToString.Exclude
    private Course course;

    @Column(nullable = false, length = 255)
    private String fileName; // Tên file gốc: e.g. Slide_Chapter1.pdf

    @Column(length = 500)
    private String filePath; // Đường dẫn file vật lý trên server

    @Column(length = 50)
    @Builder.Default
    private String fileType = "application/pdf";

    private Long fileSize; // Dung lượng bytes

    @Builder.Default
    private Integer totalPages = 0; // Số trang PDF

    @Builder.Default
    private Integer totalChunks = 0; // Số đoạn chunk đã cắt

    @Column(length = 30)
    @Builder.Default
    private String status = "UPLOADED"; // UPLOADED, CHUNKING, INDEXED, ERROR

    @Column(length = 1000)
    private String errorMessage;

    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.uploadedAt == null) {
            this.uploadedAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
        if (this.totalPages == null) this.totalPages = 0;
        if (this.totalChunks == null) this.totalChunks = 0;
        if (this.fileType == null) this.fileType = "application/pdf";
        if (this.status == null) this.status = "UPLOADED";
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
