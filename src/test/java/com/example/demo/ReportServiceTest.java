package com.example.demo.service;

import com.example.demo.dto.ReportRequest;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.*;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.ReportRepository;
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
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReportService reportService;

    // Нельзя модерировать уже закрытую жалобу
    @Test
    void moderateReport_shouldThrow_whenReportAlreadyClosed() {
        Report report = new Report();
        report.setPostId(1L);
        report.setStatus(ReportStatus.CLOSED); // Уже закрыта!

        when(reportRepository.findById(anyLong())).thenReturn(Optional.of(report));

        assertThrows(ConflictException.class, () -> reportService.moderateReport(1L, true));

        verify(reportRepository, never()).save(any());
    }

    // Модерация с блокировкой поста
    @Test
    void moderateReport_shouldBlockPost_whenShouldBlockIsTrue() {
        Report report = new Report();
        report.setPostId(1L);
        report.setStatus(ReportStatus.OPEN);

        Post post = new Post();
        post.setAuthorId(100L);
        post.setStatus(PostStatus.PUBLISHED);

        when(reportRepository.findById(anyLong())).thenReturn(Optional.of(report));
        when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));
        when(reportRepository.save(any(Report.class))).thenAnswer(inv -> inv.getArgument(0));
        when(postRepository.save(any(Post.class))).thenAnswer(inv -> inv.getArgument(0));

        Report result = reportService.moderateReport(1L, true);

        assertEquals(ReportStatus.CLOSED, result.getStatus());
        assertEquals(PostStatus.BLOCKED, post.getStatus());
        verify(reportRepository).save(any(Report.class));
        verify(postRepository).save(any(Post.class));
    }

    // Модерация без блокировки поста
    @Test
    void moderateReport_shouldNotBlockPost_whenShouldBlockIsFalse() {
        Report report = new Report();
        report.setPostId(1L);
        report.setStatus(ReportStatus.OPEN);

        when(reportRepository.findById(anyLong())).thenReturn(Optional.of(report));
        when(reportRepository.save(any(Report.class))).thenAnswer(inv -> inv.getArgument(0));

        Report result = reportService.moderateReport(1L, false);

        assertEquals(ReportStatus.CLOSED, result.getStatus());
        verify(reportRepository).save(any(Report.class));
        verify(postRepository, never()).save(any(Post.class)); // Пост не сохранялся
    }

    // Жалоба на несуществующий пост
    @Test
    void createReport_shouldThrow_whenPostNotFound() {
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        ReportRequest request = new ReportRequest(999L, 200L, "Test reason");

        assertThrows(ResourceNotFoundException.class, () -> reportService.createReport(request));
    }
}