package com.group2.assignment.rag.entity;

import com.group2.assignment.auth.entity.User;
import com.group2.assignment.core.entity.Course;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

/**
 * Tóm tắt nhanh các thuộc tính của Entity {@link Document} (bảng {@code documents}):
 * <ul>
 *   <li>{@code id}: Khóa chính tự tăng (IDENTITY).</li>
 *   <li>{@code course}: Môn học chứa tài liệu (ManyToOne LAZY, CASCADE delete).</li>
 *   <li>{@code uploadedBy}: Người tải tài liệu lên (ManyToOne LAZY, {@code null} nếu do seeder/hệ thống tạo).</li>
 *   <li>{@code fileName}: Tên file PDF gốc (tối đa 255 ký tự).</li>
 *   <li>{@code filePath}: Đường dẫn vật lý lưu file trên server ({@code ./uploads/courses/{courseId}/...}).</li>
 *   <li>{@code fileType}: Định dạng MIME của file (mặc định {@code application/pdf}).</li>
 *   <li>{@code fileSize}: Dung lượng file tính theo bytes.</li>
 *   <li>{@code totalPages}: Tổng số trang PDF đọc được (mặc định 0).</li>
 *   <li>{@code totalChunks}: Tổng số đoạn {@code Chunk} đã cắt từ tài liệu (mặc định 0).</li>
 *   <li>{@code status}: Trạng thái xử lý ({@code UPLOADED}, {@code CHUNKING}, {@code INDEXED}, {@code ERROR}).</li>
 *   <li>{@code errorMessage}: Mô tả lỗi nếu trích xuất/cắt chunk thất bại.</li>
 *   <li>{@code uploadedAt}: Thời điểm tải lên (tự gán khi {@code @PrePersist}).</li>
 *   <li>{@code updatedAt}: Thời điểm cập nhật gần nhất ({@code @PrePersist} / {@code @PreUpdate}).</li>
 * </ul>
 */
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

    /** Người tải lên tài liệu này (null = seeder/hệ thống) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id")
    @ToString.Exclude
    private User uploadedBy;

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
