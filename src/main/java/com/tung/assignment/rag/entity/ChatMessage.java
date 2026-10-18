package com.tung.assignment.rag.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chat_messages_session", columnList = "session_id"),
        @Index(name = "idx_chat_messages_sent_at", columnList = "sent_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ToString.Exclude
    private ChatSession session;

    @Column(nullable = false, length = 20)
    private String sender; // 'USER' hoặc 'AI'

    @Lob
    @Column(nullable = false, columnDefinition = "CLOB")
    private String content; // Nội dung Markdown

    @Lob
    @Column(columnDefinition = "CLOB")
    private String ragContext; // Các trích đoạn Chunks dùng làm dẫn chứng

    @Builder.Default
    private Integer tokenCount = 0;

    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        if (this.sentAt == null) {
            this.sentAt = LocalDateTime.now();
        }
        if (this.tokenCount == null) {
            this.tokenCount = 0;
        }
    }
}
