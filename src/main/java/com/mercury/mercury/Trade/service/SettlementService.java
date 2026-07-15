package com.mercury.mercury.Trade.service;

import com.mercury.mercury.Portfolio.service.PortfolioService;
import com.mercury.mercury.Trade.Enum.TradeStatus;
import com.mercury.mercury.Trade.entity.TradeEntity;
import com.mercury.mercury.Trade.repository.TradeRepo;
import com.mercury.mercury.Trade.specification.SettlementValidator;
import com.mercury.mercury.notification.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
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

    public SettlementService(TradeRepo tradeRepo, TradeLifecycleService tradeLifecycleService, SettlementValidator settlementValidator, PortfolioService portfolioService, NotificationService notificationService) {
        this.tradeRepo = tradeRepo;
        this.tradeLifecycleService = tradeLifecycleService;
        this.settlementValidator = settlementValidator;
        this.portfolioService = portfolioService;
        this.notificationService = notificationService;
    }

    private static final AtomicLong SEQUENCE = new AtomicLong(1);

    @Transactional
    public java.util.Map<String, Object> settleTrade(Long tradeId, Long processingUserId){
        log.info("Starting settlement process for trade ID: {}", tradeId);

        TradeEntity trade = tradeRepo.findById(tradeId)
                .orElseThrow(() -> new RuntimeException("Trade not found with ID: " + tradeId));

        settlementValidator.validateSettlement(trade);
        tradeLifecycleService.transationStatus(tradeId, TradeStatus.SETTLED);

        String settlementReference = generateSettlementReference();

        trade.setSettlementReference(settlementReference);
        trade.setSettled_by(processingUserId);
        trade.setSettled_date(java.time.LocalDateTime.now());
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
