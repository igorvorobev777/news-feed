package com.example.demo.repository;

import com.example.demo.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByFollowedId(Long followedId);
    boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);
}