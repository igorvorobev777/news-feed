package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "subscriptions")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "follower_id", nullable = false)
    private Long followerId;

    @Column(name = "followed_id", nullable = false)
    private Long followedId;

    public Long getId() { return id; }
    
    public Long getFollowerId() { return followerId; }
    public void setFollowerId(Long followerId) { this.followerId = followerId; }
    
    public Long getFollowedId() { return followedId; }
    public void setFollowedId(Long followedId) { this.followedId = followedId; }
}