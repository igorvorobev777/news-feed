package com.example.demo;

import com.example.demo.model.Post;
import com.example.demo.model.PostStatus;
import com.example.demo.model.Report;
import com.example.demo.model.ReportStatus;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class ReportModerationRaceConditionTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Long testReportId;
    private Long testPostId;

    @BeforeEach
    void setUp() {
        transactionTemplate.executeWithoutResult(status -> {
            reportRepository.deleteAll();
            postRepository.deleteAll();
        });

        transactionTemplate.executeWithoutResult(status -> {
            Post post = new Post();
            post.setAuthorId(1L);
            post.setText("Post for report moderation test");
            post.setStatus(PostStatus.PUBLISHED);
            post = postRepository.save(post);
            testPostId = post.getId();

            Report report = new Report();
            report.setPostId(post.getId());
            report.setAuthorId(2L);
            report.setReason("This post violates community guidelines");
            report.setStatus(ReportStatus.OPEN);
            report = reportRepository.save(report);
            testReportId = report.getId();
        });
    }

    /**
     * Два админа одновременно пытаются модерировать одну жалобу.
     * 
     * 1. Админ A читает жалобу (status=OPEN, version=0)
     * 2. Админ B читает жалобу (status=OPEN, version=0)
     * 3. Админ A закрывает жалобу и блокирует пост -> успех (version=1)
     * 4. Админ B пытается закрыть жалобу -> OptimisticLockException (version уже не 0)
     * 
     * ожидается:
     * 1 модерация успешна
     * 1 модерация получает OptimisticLockException → 409 Conflict
     * Жалоба закрыта, пост заблокирован
     */
    @Test
    void testConcurrentReportModerationCausesOptimisticLockException() throws InterruptedException {
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        // Админ A
        executor.submit(() -> {
            try {
                transactionTemplate.executeWithoutResult(status -> {
                    try {
                        Report report = reportRepository.findById(testReportId).orElseThrow();
                        startLatch.await();

                        report.setStatus(ReportStatus.CLOSED);
                        reportRepository.saveAndFlush(report);

                        Post post = postRepository.findById(report.getPostId()).orElseThrow();
                        post.setStatus(PostStatus.BLOCKED);
                        postRepository.saveAndFlush(post);

                        successCount.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        if (isOptimisticLockException(e)) {
                            conflictCount.incrementAndGet();
                        } else {
                            throw e;
                        }
                    }
                });
            } finally {
                doneLatch.countDown();
            }
        });

        // Админ B
        executor.submit(() -> {
            try {
                transactionTemplate.executeWithoutResult(status -> {
                    try {
                        Report report = reportRepository.findById(testReportId).orElseThrow();
                        startLatch.await();

                        report.setStatus(ReportStatus.CLOSED);
                        reportRepository.saveAndFlush(report);

                        Post post = postRepository.findById(report.getPostId()).orElseThrow();
                        post.setStatus(PostStatus.BLOCKED);
                        postRepository.saveAndFlush(post);

                        successCount.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        if (isOptimisticLockException(e)) {
                            conflictCount.incrementAndGet();
                        } else {
                            throw e;
                        }
                    }
                });
            } finally {
                doneLatch.countDown();
            }
        });

        // Запускаем оба потока одновременно
        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        assertEquals(1, successCount.get(), "Ровно одна модерация должна быть успешной");
        assertEquals(1, conflictCount.get(), "Ровно одна модерация должна вызвать OptimisticLockException");

        Report finalReport = reportRepository.findById(testReportId).orElseThrow();
        assertEquals(ReportStatus.CLOSED, finalReport.getStatus(), "Жалоба должна быть закрыта");
        assertEquals(1L, finalReport.getVersion(), "Версия жалобы должна увеличиться на 1");

        Post finalPost = postRepository.findById(testPostId).orElseThrow();
        assertEquals(PostStatus.BLOCKED, finalPost.getStatus(), "Пост должен быть заблокирован");
    }

    private boolean isOptimisticLockException(Throwable e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof OptimisticLockingFailureException ||
                cause instanceof ObjectOptimisticLockingFailureException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}