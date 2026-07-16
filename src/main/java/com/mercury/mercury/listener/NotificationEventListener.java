package com.mercury.mercury.listener;

import com.mercury.mercury.event.TradeCreatedEvent;
import com.mercury.mercury.event.TradeSettledEvent;
import com.mercury.mercury.notification.service.NotificationService;
import jakarta.persistence.Column;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationEventListener {
    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    public void handleTradeCreation(TradeCreatedEvent event){
        log.info("TradeCreatedEvent received for Trade ID: {}. Processing Notification.", event.getTradeId());
        try{
            notificationService.createTradeNotification(event.getTradeId());
        }catch (Exception e){
            log.error("Resilience Warning: Notification routing failed for Trade ID: {}. Core flow unaffected. Error: {}",
                    event.getTradeId(), e.getMessage());
        }
    }

    @EventListener
    public void handleTradeApproved(TradeCreatedEvent event){
        log.info("TradeApprovedEvent received for Trade ID: {}. Processing Notification.", event.getTradeId());
        try {
            notificationService.createApprovalNotification(event.getTradeId());
        }catch (Exception e) {
            log.error("Resilience Warning: Approval Notification failed for Trade ID: {}. Error: {}", event.getTradeId(), e.getMessage());
        }
    }

    @EventListener
    public void handleTradeSettled(TradeSettledEvent event){
        log.info("TradeSettledEvent received for Trade ID: {}. Processing Notification.", event.getTradeId());
        try {
            notificationService.createSettlementNotification(event.getTradeId(), event.getSettlementRef());
        } catch (Exception e) {
            log.error("Resilience Warning: Settlement Notification failed for Trade ID: {}. Error: {}", event.getTradeId(), e.getMessage());
        }
    }
}
