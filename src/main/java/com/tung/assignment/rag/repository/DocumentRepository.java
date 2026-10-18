package com.tung.assignment.rag.repository;

import com.tung.assignment.rag.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findAllByCourseIdOrderByUploadedAtDesc(Long courseId);
    List<Document> findAllByCourseIdAndStatus(Long courseId, String status);
    long countByCourseId(Long courseId);
    void deleteAllByCourseId(Long courseId);
}
