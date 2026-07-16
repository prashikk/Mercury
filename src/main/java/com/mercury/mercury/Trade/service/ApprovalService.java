package com.mercury.mercury.Trade.service;

import com.mercury.mercury.Trade.Enum.TradeStatus;
import com.mercury.mercury.Trade.dto.TradeResponseDTO;
import com.mercury.mercury.Trade.entity.TradeEntity;
import com.mercury.mercury.Trade.mapper.TradeMapper;
import com.mercury.mercury.Trade.repository.TradeRepo;
import com.mercury.mercury.Trade.specification.ApprovalValidator;
import com.mercury.mercury.User.entity.UserEntity;
import com.mercury.mercury.User.repository.UserRepository;
import com.mercury.mercury.notification.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
public class ApprovalService {
    private final TradeRepo tradeRepo;
    private final ApprovalValidator approvalValidator;
    private final TradeLifecycleService tradeLifecycleService;
    private final TradeMapper tradeMapper;
    private final NotificationService notificationService;

    private final UserRepository userRepository;

    public ApprovalService(TradeRepo tradeRepo, ApprovalValidator approvalValidator, TradeLifecycleService tradeLifecycleService, TradeMapper tradeMapper, NotificationService notificationService, UserRepository userRepository) {
        this.tradeRepo = tradeRepo;
        this.approvalValidator = approvalValidator;
        this.tradeLifecycleService = tradeLifecycleService;
        this.tradeMapper = tradeMapper;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @Transactional
    public java.util.Map<String, Object> approveTrade(Long tradeId, String managerUsername){
        log.info("Approval process started for trade ID: {} by manager: {}", tradeId, managerUsername);

        UserEntity manager = userRepository.findByUsername(managerUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated manager not found in records: " + managerUsername));

        Long checkerUserId = manager.getUserId();

        TradeEntity trade = tradeRepo.findById(tradeId)
                .orElseThrow(() -> new RuntimeException("Trade not found with ID: " + tradeId));

        BigDecimal tradeValue = trade.getPrice().multiply(BigDecimal.valueOf(trade.getQuantity()));
        log.info("Trade value calculated: {}", tradeValue);
        log.info("Trader UserId: {}", trade.getCreatedBy());
        log.info("checkerUserId: {}", checkerUserId);

        try {
            approvalValidator.validateApproval(trade, checkerUserId);
            log.info("Approval Granted");
        } catch (Exception e) {
            log.error("Approval Failed: {}", e.getMessage());
            throw e;
        }

        tradeLifecycleService.transationStatus(tradeId, TradeStatus.APPROVED);
        tradeLifecycleService.transationStatus(tradeId, TradeStatus.VALIDATED);

        trade.setApprovedBy(checkerUserId);
        trade.setApprovedAt(LocalDateTime.now());

        TradeEntity saved = tradeRepo.save(trade);
        log.info("Trade approved and validated successfully for trade ID: {}", tradeId);
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("tradeId", saved.getTrade_id());
        response.put("status", "VALIDATED");
        response.put("message", "Trade approved and validated successfully.");

        notificationService.createApprovalNotification(saved.getApprovedBy(), saved.getTrade_id());
        return response;
    }

}
