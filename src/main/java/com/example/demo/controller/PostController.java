package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.model.*;
import com.example.demo.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    @Autowired 
    private PostService postService;
    
    @Autowired 
    private LikeService likeService;

    @GetMapping
    public ResponseEntity<Page<Post>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort,
            @RequestParam(required = false) PostStatus status,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) String textSearch) {
        
        Sort.Direction direction = sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        
        Page<Post> posts = postService.findFilteredPosts(status, authorId, textSearch, pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Post> createPost(@Valid @RequestBody PostRequest request) {
        Post created = postService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/posts/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable Long id, @RequestParam Long userId, @Valid @RequestBody PostRequest request) {
        return ResponseEntity.ok(postService.update(id, userId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id, @RequestParam Long userId) {
        postService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<Post> publishPost(@PathVariable Long id, @RequestParam Long userId) {
        return ResponseEntity.ok(postService.publishPost(id, userId));
    }

    @PostMapping("/{id}/likes")
    public ResponseEntity<Like> likePost(@PathVariable Long id, @Valid @RequestBody LikeRequest request) {
        Like like = likeService.likePost(id, request);
        return ResponseEntity.created(URI.create("/api/v1/likes/" + like.getId())).body(like);
    }

    @DeleteMapping("/{id}/likes")
    public ResponseEntity<Void> unlikePost(@PathVariable Long id, @RequestParam Long userId) {
        likeService.unlikePost(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<Post> blockPost(@PathVariable Long id) {
        return ResponseEntity.ok(postService.blockPost(id));
    }
}