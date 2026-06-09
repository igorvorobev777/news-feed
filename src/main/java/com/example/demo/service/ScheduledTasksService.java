package com.example.demo.service;

import com.example.demo.model.Post;
import com.example.demo.model.PostStatus;
import com.example.demo.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScheduledTasksService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasksService.class);

    @Autowired
    private PostRepository postRepository;

    // Авто-удаление неопубликованных постов (в статусе CREATED) старше 30 дней
    @Scheduled(cron = "0 0 3 * * *") // запускается каждый день в 3:00
    @Transactional
    public void deleteOldUnpublishedPosts() {
        log.info("Starting scheduled task: delete old unpublished posts");
        
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        List<Post> oldPosts = postRepository.findByStatusAndCreatedAtBefore(
            PostStatus.CREATED, 
            thirtyDaysAgo
        );
        
        int deletedCount = 0;
        for (Post post : oldPosts) {
            postRepository.delete(post);
            deletedCount++;
        }
        
        log.info("Scheduled task completed: deleted {} unpublished posts older than 30 days", deletedCount);
    }
}