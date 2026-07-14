package com.mercury.mercury.Trade.service;

import com.mercury.mercury.Client.Enum.TradeStatus;
import com.mercury.mercury.Trade.entity.TradeEntity;
import com.mercury.mercury.Trade.repository.TradeRepo;
import com.mercury.mercury.Trade.specification.SettlementValidator;
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

    public SettlementService(TradeRepo tradeRepo, TradeLifecycleService tradeLifecycleService, SettlementValidator settlementValidator) {
        this.tradeRepo = tradeRepo;
        this.tradeLifecycleService = tradeLifecycleService;
        this.settlementValidator = settlementValidator;
    }

    private static final AtomicLong SEQUENCE = new AtomicLong(1);

    public java.util.Map<String, Object> settleTrade(Long tradeId, Long processingUserId){
        log.info("Starting settlement process for trade ID: {}", tradeId);

        TradeEntity trade = tradeRepo.findById(tradeId)
                .orElseThrow(() -> new RuntimeException("Trade not found with ID: " + tradeId));

        settlementValidator.validateSettlement(trade);
        tradeLifecycleService.TransationStatus(tradeId, TradeStatus.SETTLED);
        String settlementReference = generateSettlementReference();

        trade.setSettlementReference(settlementReference);
        trade.setSettled_by(processingUserId);
        trade.setSettled_date(java.time.LocalDateTime.now());
        tradeRepo.save(trade);

        log.info("Settlement process completed for trade ID: {}. Settlement Reference: {}", tradeId, settlementReference);

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("tradeId", tradeId);
        response.put("settlementReference", settlementReference);
        response.put("settledDate", trade.getSettled_date());
        return response;
    }

    public String generateSettlementReference(){
        String dateToken = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("SET-%s-%06d", dateToken, SEQUENCE.getAndIncrement());
    }
}
