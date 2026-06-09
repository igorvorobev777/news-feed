package com.example.demo.repository;

import com.example.demo.model.Post;
import com.example.demo.model.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    
    @Query("""
        SELECT p FROM Post p 
        WHERE (:status IS NULL OR p.status = :status)
          AND (:authorId IS NULL OR p.authorId = :authorId)
          AND (COALESCE(:textSearch, '') = '' OR LOWER(p.text) LIKE LOWER(CONCAT('%', :textSearch, '%')))
    """)
    Page<Post> findFiltered(
            @Param("status") PostStatus status,
            @Param("authorId") Long authorId,
            @Param("textSearch") String textSearch,
            Pageable pageable);

    List<Post> findByStatusAndCreatedAtBefore(PostStatus status, LocalDateTime createdAt);
}