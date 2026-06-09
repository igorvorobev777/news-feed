package com.example.demo.service;

import com.example.demo.dto.CommentRequest;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Comment;
import com.example.demo.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    public List<Comment> findAll() {
        return commentRepository.findAll();
    }

    public List<Comment> findByPostId(Long postId) {
        return commentRepository.findAllByPostId(postId);
    }

    public Comment findById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));
    }

    @Transactional
    public Comment create(CommentRequest request) {
        Comment comment = new Comment();
        comment.setPostId(request.postId());
        comment.setAuthorId(request.authorId());
        comment.setText(request.text());
        return commentRepository.save(comment);
    }

    @Transactional
    public Comment update(Long id, CommentRequest request) {
        Comment comment = findById(id);
        comment.setText(request.text());
        return commentRepository.save(comment);
    }

    @Transactional
    public void delete(Long id) {
        Comment comment = findById(id);
        commentRepository.delete(comment);
    }
}