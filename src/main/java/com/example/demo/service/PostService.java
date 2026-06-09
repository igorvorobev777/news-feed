package com.example.demo.service;

import com.example.demo.dto.PostRequest;
import com.example.demo.event.PostPublishedEvent;
import com.example.demo.exception.AccessException;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Post;
import com.example.demo.model.PostStatus;
import com.example.demo.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private FeedService feedService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    // Чтение поста кэшируется
    @Cacheable(value = "postById", key = "#id")
    public Post findById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
    }

    // Создание поста очищает кэш списка
    @CacheEvict(value = "posts", allEntries = true)
    @Transactional
    public Post create(PostRequest request) {
        Post post = new Post();
        post.setAuthorId(request.authorId());
        post.setText(request.text());
        return postRepository.save(post);
    }

    // Обновление поста обновляет кэш
    @CachePut(value = "postById", key = "#id")
    @CacheEvict(value = "posts", allEntries = true)
    @Transactional
    public Post update(Long id, Long userId, PostRequest request) {
        Post post = findById(id);
        
        if (!post.getAuthorId().equals(userId)) {
            throw new AccessException("Редактировать можно только свои посты");
        }
        
        post.setText(request.text());
        return postRepository.save(post);
    }

    // Удаление поста очищает кэш
    @CacheEvict(value = {"posts", "postById"}, allEntries = true)
    @Transactional
    public void delete(Long id, Long userId) {
        Post post = findById(id);
        
        if (!post.getAuthorId().equals(userId)) {
            throw new AccessException("Удалять можно только свои посты"); // <-- Изменено
        }
        
        postRepository.delete(post);
    }

    public Page<Post> findFilteredPosts(PostStatus status, Long authorId, String textSearch, Pageable pageable) {
        return postRepository.findFiltered(status, authorId, textSearch, pageable);
    }

    // Публикация поста — очищает кэш
    @CacheEvict(value = {"posts", "postById"}, allEntries = true)
    @Transactional
    public Post publishPost(Long id, Long userId) {
        Post post = findById(id);
        
        if (!post.getAuthorId().equals(userId)) {
            throw new AccessException("Публиковать можно только свои посты");
        }
        
        if (post.getStatus() != PostStatus.CREATED) {
            throw new ConflictException("Пост можно опубликовать только из статуса CREATED. Текущий: " + post.getStatus());
        }
        
        post.setStatus(PostStatus.PUBLISHED);
        post = postRepository.save(post);
        
        // Асинхронная генерация FeedItem для подписчиков
        feedService.generateFeedItemsForPost(post.getId(), post.getAuthorId());
        
        eventPublisher.publishEvent(new PostPublishedEvent(this, post.getId(), post.getAuthorId()));
        
        return post;
    }

    @Transactional
    public Post blockPost(Long id) {
        Post post = findById(id);
    
        // блокировать можно только опубликованные посты
        if (post.getStatus() != PostStatus.PUBLISHED) {
            throw new ConflictException(
                "Post can only be blocked from PUBLISHED status. Current: " + post.getStatus());
        }
    
        post.setStatus(PostStatus.BLOCKED);
        return postRepository.save(post);
    }
}