package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feed_items")
public class FeedItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "score_rank", nullable = false)
    private Long scoreRank = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "view_status", nullable = false)
    private ViewStatus viewStatus = ViewStatus.UNSEEN;

    @Column(name = "generated_at", updatable = false)
    private LocalDateTime generatedAt;

    @PrePersist
    protected void onCreate() {
        generatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public Long getScoreRank() { return scoreRank; }
    public void setScoreRank(Long scoreRank) { this.scoreRank = scoreRank; }
    public ViewStatus getViewStatus() { return viewStatus; }
    public void setViewStatus(ViewStatus viewStatus) { this.viewStatus = viewStatus; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
}