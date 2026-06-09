package com.example.demo.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


//Обработчик события закрытия жалобы.
//Логирует результат модерации.

@Component
public class ReportClosedEventHandler {

    private static final Logger log = LoggerFactory.getLogger(ReportClosedEventHandler.class);

    @EventListener
    public void handleReportClosed(ReportClosedEvent event) {
        log.info("Обработка события ReportClosedEvent: reportId={}, postId={}, blocked={}", 
                event.getReportId(), event.getPostId(), event.isPostBlocked());
        
        if (event.isPostBlocked()) {
            log.warn("Пост {} был ЗАБЛОКИРОВАН по жалобе {}", 
                    event.getPostId(), event.getReportId());
        } else {
            log.info("Жалоба {} закрыта без блокировки поста {}", 
                    event.getReportId(), event.getPostId());
        }
    }
}