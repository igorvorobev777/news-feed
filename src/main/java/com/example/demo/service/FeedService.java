package com.example.demo.service;

import com.example.demo.model.FeedItem;
import com.example.demo.model.Subscription;
import com.example.demo.repository.FeedItemRepository;
import com.example.demo.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FeedService {

    @Autowired
    private FeedItemRepository feedItemRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;


    //Асинхронная генерация FeedItem для всех подписчиков автора поста
    @Async
    @Transactional
    public void generateFeedItemsForPost(Long postId, Long authorId) {
        
        List<Subscription> subscribers = subscriptionRepository.findByFollowedId(authorId);
        
        // Создаем FeedItem для каждого подписчика
        for (Subscription subscription : subscribers) {
            FeedItem feedItem = new FeedItem();
            feedItem.setUserId(subscription.getFollowerId());
            feedItem.setPostId(postId);
            feedItem.setScoreRank(System.currentTimeMillis());
            
            try {
                feedItemRepository.save(feedItem);
            } catch (Exception e) {
                System.err.println("Failed to create FeedItem for user " + subscription.getFollowerId() + ": " + e.getMessage());
            }
        }
    }
}