package com.group2.assignment.rag.entity;

import com.group2.assignment.core.entity.Course;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

/**
 * Tóm tắt nhanh các thuộc tính của Entity {@link ChatSession} (bảng {@code chat_sessions}):
 * <ul>
 *   <li>{@code id}: Khóa chính tự tăng (IDENTITY).</li>
 *   <li>{@code course}: Môn học của phiên hỏi đáp RAG (ManyToOne LAZY, CASCADE delete).</li>
 *   <li>{@code title}: Tiêu đề phiên hội thoại (tối đa 255 ký tự, tự sinh từ câu hỏi đầu hoặc mặc định).</li>
 *   <li>{@code createdAt}: Thời điểm tạo phiên chat (tự gán khi {@code @PrePersist}).</li>
 *   <li>{@code updatedAt}: Thời điểm cập nhật gần nhất ({@code @PrePersist} / {@code @PreUpdate}).</li>
 * </ul>
 */
@Entity
@Table(name = "chat_sessions", indexes = {
        @Index(name = "idx_chat_sessions_course", columnList = "course_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ToString.Exclude
    private Course course;

    @Column(nullable = false, length = 255)
    private String title; // Tiêu đề cuộc hội thoại

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
