package com.example.demo.controller;

import com.example.demo.dto.ReportRequest;
import com.example.demo.model.Report;
import com.example.demo.model.ReportStatus;
import com.example.demo.service.ReportService;
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
@RequestMapping("/api/v1/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping
    public List<Report> getAllReports() {
        return reportService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Report> getReportById(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Report> createReport(@Valid @RequestBody ReportRequest request) {
        Report created = reportService.createReport(request);
        return ResponseEntity.created(
            URI.create("/api/v1/reports/" + created.getId())
        ).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Report> updateReport(
            @PathVariable Long id,
            @Valid @RequestBody ReportRequest request) {
        return ResponseEntity.ok(reportService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        reportService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/list")
    public ResponseEntity<Page<Report>> getFilteredReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort,
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) Long postId,
            @RequestParam(required = false) Long authorId) {
    
        Sort.Direction direction = sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
    
        Page<Report> reports = reportService.findFilteredReports(status, postId, authorId, pageable);
        return ResponseEntity.ok(reports);
    }

    @PutMapping("/{id}/moderate")
    public ResponseEntity<Report> moderateReport(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean shouldBlockPost) {
        return ResponseEntity.ok(reportService.moderateReport(id, shouldBlockPost));
    }
}