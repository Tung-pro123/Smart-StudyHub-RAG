package com.group2.assignment.rag.repository;

import com.group2.assignment.rag.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findAllBySessionIdOrderBySentAtAsc(Long sessionId);
    long countBySessionId(Long sessionId);
    void deleteAllBySessionId(Long sessionId);
}
