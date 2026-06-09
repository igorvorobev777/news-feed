package com.example.demo.repository;

import com.example.demo.model.FeedItem;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedItemRepository extends JpaRepository<FeedItem, Long> {
    Page<FeedItem> findByUserIdOrderByScoreRankDesc(Long userId, Pageable pageable);
    void deleteByPostId(Long postId);

    Optional<FeedItem> findByUserIdAndPostId(Long userId, Long postId);
}