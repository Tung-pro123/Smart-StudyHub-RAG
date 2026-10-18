package com.group2.assignment.rag.repository;

import com.group2.assignment.rag.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    List<ChatSession> findAllByCourseIdOrderByUpdatedAtDesc(Long courseId);
    void deleteAllByCourseId(Long courseId);
}
