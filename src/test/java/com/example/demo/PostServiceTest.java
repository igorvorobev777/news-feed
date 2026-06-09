package com.example.demo.service;

import com.example.demo.dto.PostRequest;
import com.example.demo.exception.AccessException;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Post;
import com.example.demo.model.PostStatus;
import com.example.demo.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PostService postService;

    // Пользователь не может опубликовать чужой пост
    @Test
    void publishPost_shouldThrow_whenUserIsNotAuthor() {
        Post post = new Post();
        post.setAuthorId(100L);
        post.setText("Test");
        post.setStatus(PostStatus.CREATED);

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));

        assertThrows(AccessException.class, () -> postService.publishPost(1L, 999L));

        // Пост НЕ должен быть сохранён
        verify(postRepository, never()).save(any());
    }

    // Нельзя опубликовать уже опубликованный пост
    @Test
    void publishPost_shouldThrow_whenPostAlreadyPublished() {
        Post post = new Post();
        post.setAuthorId(100L);
        post.setText("Test");
        post.setStatus(PostStatus.PUBLISHED);

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));

        assertThrows(ConflictException.class, () -> postService.publishPost(1L, 100L));

        verify(postRepository, never()).save(any());
    }

    // Нельзя обновить чужой пост
    @Test
    void update_shouldThrow_whenUserIsNotAuthor() {
        Post post = new Post();
        post.setAuthorId(100L);
        post.setText("Test");

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));

        PostRequest request = new PostRequest(100L, "New text");

        assertThrows(AccessException.class, () -> postService.update(1L, 999L, request));

        verify(postRepository, never()).save(any());
    }

    // Пост не найден
    @Test
    void findById_shouldThrow_whenNotFound() {
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> postService.findById(999L));
    }
}