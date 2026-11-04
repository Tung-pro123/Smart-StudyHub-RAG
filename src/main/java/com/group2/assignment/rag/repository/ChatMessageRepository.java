package com.group2.assignment.rag.repository;

import com.group2.assignment.rag.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findAllBySessionIdOrderBySentAtAsc(Long sessionId);
    long countBySessionId(Long sessionId);

    @Transactional
    @Modifying
    @Query("DELETE FROM ChatMessage m WHERE m.session.id = :sessionId")
    void deleteAllBySessionId(@Param("sessionId") Long sessionId);
}
