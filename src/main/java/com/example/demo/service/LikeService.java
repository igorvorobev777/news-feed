package com.example.demo.service;

import com.example.demo.dto.LikeRequest;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Like;
import com.example.demo.model.Post;
import com.example.demo.repository.LikeRepository;
import com.example.demo.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService {
    @Autowired
    private LikeRepository likeRepository;
    @Autowired
    private PostRepository postRepository;

    @Transactional
    public Like likePost(Long postId, LikeRequest request) {
        postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
        
        if (likeRepository.existsByPostIdAndUserId(postId, request.userId())) {
            throw new ConflictException("User " + request.userId() + " already liked post " + postId);
        }
        
        Like like = new Like();
        like.setPostId(postId);
        like.setUserId(request.userId());
        return likeRepository.save(like);
    }

    @Transactional
    public void unlikePost(Long postId, Long userId) {
        Like like = likeRepository.findByPostIdAndUserId(postId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Like not found for post " + postId + " and user " + userId));
    
        likeRepository.delete(like);
    }
}