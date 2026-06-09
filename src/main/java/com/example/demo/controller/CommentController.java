package com.example.demo.controller;

import com.example.demo.dto.CommentRequest;
import com.example.demo.model.Comment;
import com.example.demo.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/{postId}/comments")
    public List<Comment> getCommentsByPost(@PathVariable Long postId) {
        return commentService.findByPostId(postId);
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<Comment> createComment(@PathVariable Long postId, @Valid @RequestBody CommentRequest request) {
        
        CommentRequest fixedRequest = new CommentRequest(
            postId,
            request.authorId(),
            request.text()
        );
        
        Comment created = commentService.create(fixedRequest);
        return ResponseEntity.created(
            URI.create("/api/v1/posts/" + postId + "/comments/" + created.getId())
        ).body(created);
    }

    @GetMapping("/comments/{id}")
    public ResponseEntity<Comment> getCommentById(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.findById(id));
    }

    @PutMapping("/comments/{id}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.ok(commentService.update(id, request));
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}