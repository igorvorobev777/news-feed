package com.example.demo.repository;

import com.example.demo.model.Report;
import com.example.demo.model.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report, Long> {
    
    @Query("""
        SELECT r FROM Report r 
        WHERE (:status IS NULL OR r.status = :status)
          AND (:postId IS NULL OR r.postId = :postId)
          AND (:authorId IS NULL OR r.authorId = :authorId)
    """)
    Page<Report> findFiltered(
            @Param("status") ReportStatus status,
            @Param("postId") Long postId,
            @Param("authorId") Long authorId,
            Pageable pageable);
}