package com.example.demo.controller;

import com.example.demo.model.AppUser;
import com.example.demo.model.FeedItem;
import com.example.demo.model.Post;
import com.example.demo.repository.FeedItemRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feed")
public class FeedController {

    @Autowired
    private FeedItemRepository feedItemRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    //Получить ленту текущего пользователя
    @GetMapping
    public ResponseEntity<Page<Post>> getFeed(@AuthenticationPrincipal UserDetails userDetails, 
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {

        String login = userDetails.getUsername();
    
        AppUser user = userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("User not found: " + login));
    
        Long userId = user.getId();

        Pageable pageable = PageRequest.of(page, size, 
            Sort.by(Sort.Direction.DESC, "scoreRank"));

        Page<FeedItem> feedItems = feedItemRepository
            .findByUserIdOrderByScoreRankDesc(userId, pageable);

        List<Long> postIds = feedItems.getContent().stream()
            .map(FeedItem::getPostId)
            .toList();

        List<Post> posts = postRepository.findAllById(postIds);

        Page<Post> postPage = new org.springframework.data.domain.PageImpl<>(
            posts, pageable, feedItems.getTotalElements()
        );

        return ResponseEntity.ok(postPage);
    }

    @PatchMapping("/{postId}/view")
    public ResponseEntity<Void> markAsViewed(@AuthenticationPrincipal Long userId, @PathVariable Long postId) {

        feedItemRepository.findByUserIdAndPostId(userId, postId)
            .ifPresent(feedItem -> {
                feedItem.setViewStatus(com.example.demo.model.ViewStatus.SEEN);
                feedItemRepository.save(feedItem);
            });

        return ResponseEntity.ok().build();
    }
}