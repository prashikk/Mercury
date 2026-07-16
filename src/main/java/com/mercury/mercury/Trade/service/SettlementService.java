package com.mercury.mercury.Trade.service;

import com.mercury.mercury.Portfolio.service.PortfolioService;
import com.mercury.mercury.Trade.Enum.TradeStatus;
import com.mercury.mercury.Trade.entity.TradeEntity;
import com.mercury.mercury.Trade.repository.TradeRepo;
import com.mercury.mercury.Trade.specification.SettlementValidator;
import com.mercury.mercury.User.entity.UserEntity;
import com.mercury.mercury.User.repository.UserRepository;
import com.mercury.mercury.User.service.AuthenticatedUserService;
import com.mercury.mercury.event.publisher.TradeEventPublisher;
import com.mercury.mercury.notification.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import com.mercury.mercury.event.TradeSettledEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class SettlementService {
    private final TradeRepo tradeRepo;
    private final TradeLifecycleService tradeLifecycleService;
    private final SettlementValidator settlementValidator;
    private final AuthenticatedUserService authenticatedUserService;
    private final TradeEventPublisher tradeEventPublisher;

    public SettlementService(TradeRepo tradeRepo, TradeLifecycleService tradeLifecycleService, SettlementValidator settlementValidator, AuthenticatedUserService authenticatedUserService, TradeEventPublisher tradeEventPublisher) {
        this.tradeRepo = tradeRepo;
        this.tradeLifecycleService = tradeLifecycleService;
        this.settlementValidator = settlementValidator;
        this.authenticatedUserService = authenticatedUserService;
        this.tradeEventPublisher = tradeEventPublisher;
    }

    private static final AtomicLong SEQUENCE = new AtomicLong(1);

    @Transactional
    public java.util.Map<String, Object> settleTrade(Long tradeId){
        String operationsUsername = authenticatedUserService.getCurrentUsername();
        Long processingUserId = authenticatedUserService.getCurrentUserId();

        log.info("User '{}' initiated settlement clearing pipeline execution for Trade ID: {}", operationsUsername, tradeId);

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

        log.info("Clearing operations completed for Trade ID: {}. Assigned Ref: '{}' under context of user '{}'",
                tradeId, settlementReference, operationsUsername);

        log.info("Publishing TradeSettledEvent | Trade ID {}", tradeId);
        tradeEventPublisher.publishTradeSettled(
                new TradeSettledEvent(tradeId, settlementReference, LocalDateTime.now())
        );

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("tradeId", tradeId);
        response.put("settlementReference", settlementReference);
        response.put("settledDate", trade.getSettled_date());
        return response;
    }

    public String generateSettlementReference() {
        String dateToken = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        long uniqueTimeToken = System.currentTimeMillis() % 1000000;
        return String.format("SET-%s-%06d", dateToken, uniqueTimeToken);
    }
}
