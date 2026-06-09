package com.example.demo.service;

import com.example.demo.dto.ReportRequest;
import com.example.demo.event.ReportClosedEvent;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Post;
import com.example.demo.model.PostStatus;
import com.example.demo.model.Report;
import com.example.demo.model.ReportStatus;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public List<Report> findAll() {
        return reportRepository.findAll();
    }

    public Report findById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));
    }

    @Transactional
    public Report createReport(ReportRequest request) {
        Post post = postRepository.findById(request.postId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + request.postId()));
        
        if (post.getStatus() == PostStatus.BLOCKED) {
            throw new ConflictException("Cannot report a blocked post");
        }
        
        Report report = new Report();
        report.setPostId(request.postId());
        report.setAuthorId(request.userId());
        report.setReason(request.reason());
        return reportRepository.save(report);
    }

    @Transactional
    public Report update(Long id, ReportRequest request) {
        Report report = findById(id);
        report.setReason(request.reason());
        return reportRepository.save(report);
    }

    @Transactional
    public void delete(Long id) {
        Report report = findById(id);
        reportRepository.delete(report);
    }

    public Page<Report> findFilteredReports(ReportStatus status, Long postId, Long authorId, Pageable pageable) {
        return reportRepository.findFiltered(status, postId, authorId, pageable);
    }

    @Transactional 
    public Report moderateReport(Long reportId, boolean shouldBlockPost) {
        Report report = findById(reportId);
        
        if (report.getStatus() == ReportStatus.CLOSED) {
            throw new ConflictException("Report is already closed");
        }
        
        report.setStatus(ReportStatus.CLOSED);
        reportRepository.save(report);

        eventPublisher.publishEvent(new ReportClosedEvent(this, report.getId(), report.getPostId(), shouldBlockPost));

        if (shouldBlockPost) {
            Post post = postRepository.findById(report.getPostId())
                    .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
            
            if (post.getStatus() != PostStatus.BLOCKED) {
                post.setStatus(PostStatus.BLOCKED);
                postRepository.save(post);
            }
        }
        
        return report;
    }
}