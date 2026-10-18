package com.group2.assignment.core.repository;

import com.group2.assignment.core.entity.Chunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChunkRepository extends JpaRepository<Chunk, Long> {
    List<Chunk> findAllByDocumentIdOrderByChunkIndexAsc(Long documentId);
    List<Chunk> findAllByCourseId(Long courseId);
    List<Chunk> findAllByCourseIdAndHasEmbeddingTrue(Long courseId);
    long countByCourseId(Long courseId);
    long countByDocumentId(Long documentId);
    void deleteAllByDocumentId(Long documentId);
    void deleteAllByCourseId(Long courseId);
}
