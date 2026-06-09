package com.example.demo.event;

import org.springframework.context.ApplicationEvent;

public class ReportClosedEvent extends ApplicationEvent {
    private final Long reportId;
    private final Long postId;
    private final boolean postBlocked;

    public ReportClosedEvent(Object source, Long reportId, Long postId, boolean postBlocked) {
        super(source);
        this.reportId = reportId;
        this.postId = postId;
        this.postBlocked = postBlocked;
    }

    public Long getReportId() { return reportId; }
    public Long getPostId() { return postId; }
    public boolean isPostBlocked() { return postBlocked; }
}