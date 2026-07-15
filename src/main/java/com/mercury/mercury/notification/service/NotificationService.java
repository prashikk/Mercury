package com.mercury.mercury.notification.service;

import com.mercury.mercury.notification.domain.Enum.NotificationStatus;
import com.mercury.mercury.notification.domain.Enum.NotificationType;
import com.mercury.mercury.notification.domain.NotificationEntity;
import com.mercury.mercury.notification.dto.NotificationResponseDTO;
import com.mercury.mercury.notification.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    private void saveNotification(Long userId, Long tradeId, NotificationType type, String title, String message) {
        log.info("Notification Created"); // Expected trace point

        NotificationEntity notification = new NotificationEntity();
        notification.setUserId(userId);
        notification.setTradeId(tradeId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setCreatedAt(LocalDateTime.now());

        try {
            notificationRepository.save(notification);
            log.info("Notification Saved"); // Expected trace point
        } catch (Exception e) {
            log.error("Notification Failed: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void createTradeNotification(Long userId, Long tradeId) {
        saveNotification(userId, tradeId, NotificationType.TRADE_CREATED,
                "Trade Created Successfully",
                "A new trade transaction with ID " + tradeId + " has been recorded.");
    }

    @Transactional
    public void createApprovalNotification(Long userId, Long tradeId) {
        saveNotification(userId, tradeId, NotificationType.TRADE_APPROVED,
                "Trade Approved",
                "Trade transaction with ID " + tradeId + " has been approved by compliance.");
    }

    @Transactional
    public void createSettlementNotification(Long userId, Long tradeId, String settlementRef) {
        saveNotification(userId, tradeId, NotificationType.TRADE_SETTLED,
                "Trade Settled",
                "Trade with ID " + tradeId + " has settled successfully. Settlement Reference: " + settlementRef);
    }

    @Transactional
    public void createPortfolioNotification(Long userId, Long tradeId, Long currentQuantity) {
        saveNotification(userId, tradeId, NotificationType.PORTFOLIO_UPDATED,
                "Portfolio Updated",
                "Your asset holdings position has been updated. Current Quantity: " + currentQuantity);
    }

    @Transactional
    public Page<NotificationResponseDTO> getUserNotifications(Long userId, Pageable pageable) {
        Page<NotificationEntity> entities = notificationRepository.findByUserId(userId, pageable);
        log.info("Notification Retrieved"); // Expected trace point

        return entities.map(entity -> {
            NotificationResponseDTO dto = new NotificationResponseDTO();
            dto.setNotificationId(entity.getNotificationId());
            dto.setUserId(entity.getUserId());
            dto.setTradeId(entity.getTradeId());
            dto.setType(entity.getType());
            dto.setTitle(entity.getTitle());
            dto.setMessage(entity.getMessage());
            dto.setStatus(entity.getStatus());
            dto.setCreatedAt(entity.getCreatedAt());
            dto.setSentAt(entity.getSentAt());
            return dto;
        });
    }

}
