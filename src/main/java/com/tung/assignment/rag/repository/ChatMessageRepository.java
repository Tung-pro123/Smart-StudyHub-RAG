package com.tung.assignment.rag.repository;

import com.tung.assignment.rag.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findAllBySessionIdOrderBySentAtAsc(Long sessionId);
    long countBySessionId(Long sessionId);
    void deleteAllBySessionId(Long sessionId);
}
