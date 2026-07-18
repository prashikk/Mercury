package com.mercury.mercury.Trade.service;

import com.mercury.mercury.Trade.Enum.TradeStatus;
import com.mercury.mercury.Trade.dto.TradeResponseDTO;
import com.mercury.mercury.Trade.entity.TradeEntity;
import com.mercury.mercury.Trade.mapper.TradeMapper;
import com.mercury.mercury.Trade.repository.TradeRepo;
import com.mercury.mercury.Trade.specification.ApprovalValidator;
import com.mercury.mercury.User.entity.Role;
import com.mercury.mercury.User.entity.UserEntity;
import com.mercury.mercury.User.repository.UserRepository;
import com.mercury.mercury.User.service.AuthenticatedUserService;
import com.mercury.mercury.event.TradeApprovedEvent;
import com.mercury.mercury.event.TradeSettledEvent;
import com.mercury.mercury.event.publisher.TradeEventPublisher;
import com.mercury.mercury.monitoring.TradeMetricsService;
import com.mercury.mercury.notification.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
public class ApprovalService {
    private final TradeRepo tradeRepo;
    private final ApprovalValidator approvalValidator;
    private final TradeLifecycleService tradeLifecycleService;
    private final AuthenticatedUserService authenticatedUserService;
    private final ApplicationEventPublisher eventPublisher;
    private final TradeEventPublisher tradeEventPublisher;
    private final TradeMetricsService tradeMetricsService;


    public ApprovalService(TradeRepo tradeRepo, ApprovalValidator approvalValidator, TradeLifecycleService tradeLifecycleService, AuthenticatedUserService authenticatedUserService, ApplicationEventPublisher eventPublisher, TradeEventPublisher tradeEventPublisher, TradeMetricsService tradeMetricsService) {
        this.tradeRepo = tradeRepo;
        this.approvalValidator = approvalValidator;
        this.tradeLifecycleService = tradeLifecycleService;
        this.authenticatedUserService = authenticatedUserService;
        this.eventPublisher = eventPublisher;
        this.tradeEventPublisher = tradeEventPublisher;
        this.tradeMetricsService = tradeMetricsService;
    }

    @Transactional
    public java.util.Map<String, Object> approveTrade(Long tradeId){
        UserEntity managerEntity = authenticatedUserService.getCurrentUser();
        Long checkerUserId = managerEntity.getUserId();
        Role checkerRole = managerEntity.getRole();

        log.info("User '{}' (Role: {}) initiated approval for trade ID: {}",
                managerEntity.getUsername(), checkerRole, tradeId);

        TradeEntity trade = tradeRepo.findById(tradeId)
                .orElseThrow(() -> new RuntimeException("Trade not found with ID: " + tradeId));

        BigDecimal tradeValue = trade.getPrice().multiply(BigDecimal.valueOf(trade.getQuantity()));
        log.info("Trade processing values -> Calculated Value: {}, Maker User ID: {}, Checker User ID: {}",
                tradeValue, trade.getCreatedBy(), checkerUserId);

        log.info("Trade value calculated: {}", tradeValue);
        log.info("Trader UserId: {}", trade.getCreatedBy());
        log.info("checkerUserId: {}", checkerUserId);

        try {
            approvalValidator.validateApproval(trade, checkerUserId, checkerRole);
            log.info("Checker rule evaluation succeeded for Trade ID: {}", tradeId);
        } catch (Exception e) {
            tradeMetricsService.incrementFailed();
            log.error("Approval failed for trade ID: {}. Reject Reason: {}", tradeId, e.getMessage());
            throw e;
        }

        tradeLifecycleService.transationStatus(tradeId, TradeStatus.APPROVED);
        tradeLifecycleService.transationStatus(tradeId, TradeStatus.VALIDATED);

        trade.setApprovedBy(checkerUserId);
        trade.setApprovedAt(LocalDateTime.now());

        TradeEntity saved = tradeRepo.save(trade);
        log.info("User '{}' successfully approved and validated Trade ID: {}", managerEntity.getUsername(), tradeId);
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("tradeId", saved.getTrade_id());
        response.put("status", "VALIDATED");
        response.put("message", "Trade approved and validated successfully.");

        log.info("Publishing TradeApprovedEvent for Trade ID: {}", tradeId);
        tradeEventPublisher.publishTradeApproved(
                new TradeApprovedEvent(tradeId, checkerUserId, LocalDateTime.now())
        );
        tradeMetricsService.incrementApproved();
        return response;
    }

}
