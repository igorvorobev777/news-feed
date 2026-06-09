package com.example.demo.event;

import org.springframework.context.ApplicationEvent;

public class PostPublishedEvent extends ApplicationEvent {
    private final Long postId;
    private final Long authorId;

    public PostPublishedEvent(Object source, Long postId, Long authorId) {
        super(source);
        this.postId = postId;
        this.authorId = authorId;
    }

    public Long getPostId() { return postId; }
    public Long getAuthorId() { return authorId; }
}