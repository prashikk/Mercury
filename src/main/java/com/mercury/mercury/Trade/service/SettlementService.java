package com.mercury.mercury.Trade.service;

import com.mercury.mercury.Portfolio.service.PortfolioService;
import com.mercury.mercury.Trade.Enum.TradeStatus;
import com.mercury.mercury.Trade.entity.TradeEntity;
import com.mercury.mercury.Trade.repository.TradeRepo;
import com.mercury.mercury.Trade.specification.SettlementValidator;
import com.mercury.mercury.User.entity.UserEntity;
import com.mercury.mercury.User.repository.UserRepository;
import com.mercury.mercury.notification.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class SettlementService {
    private final TradeRepo tradeRepo;
    private final TradeLifecycleService tradeLifecycleService;
    private final SettlementValidator settlementValidator;
    private final PortfolioService portfolioService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public SettlementService(TradeRepo tradeRepo, TradeLifecycleService tradeLifecycleService, SettlementValidator settlementValidator, PortfolioService portfolioService, NotificationService notificationService, UserRepository userRepository) {
        this.tradeRepo = tradeRepo;
        this.tradeLifecycleService = tradeLifecycleService;
        this.settlementValidator = settlementValidator;
        this.portfolioService = portfolioService;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    private static final AtomicLong SEQUENCE = new AtomicLong(1);

    @Transactional
    public java.util.Map<String, Object> settleTrade(Long tradeId, String operationsUsername){
        log.info("Starting settlement process for trade ID: {} by user: {}", tradeId, operationsUsername);

        UserEntity operationsUser = userRepository.findByUsername(operationsUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated operations user not found in records: " + operationsUsername));

        Long processingUserId = operationsUser.getUserId();

        TradeEntity trade = tradeRepo.findById(tradeId)
                .orElseThrow(() -> new RuntimeException("Trade not found with ID: " + tradeId));

        settlementValidator.validateSettlement(trade);
        tradeLifecycleService.transationStatus(tradeId, TradeStatus.SETTLED);

        String settlementReference = generateSettlementReference();
        LocalDateTime now = java.time.LocalDateTime.now();
        trade.setSettlementReference(settlementReference);
        trade.setSettled_by(processingUserId);
        trade.setSettled_at(now);
        trade.setSettled_date(now);
        tradeRepo.save(trade);

        log.info("Settlement process completed for trade ID: {}. Settlement Reference: {}", tradeId, settlementReference);

        portfolioService.updatePortfolioPosition(trade);

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("tradeId", tradeId);
        response.put("settlementReference", settlementReference);
        response.put("settledDate", trade.getSettled_date());

        notificationService.createSettlementNotification(processingUserId, tradeId, settlementReference);
        return response;
    }

    public String generateSettlementReference() {
        String dateToken = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        long uniqueTimeToken = System.currentTimeMillis() % 1000000;
        return String.format("SET-%s-%06d", dateToken, uniqueTimeToken);
    }
}
