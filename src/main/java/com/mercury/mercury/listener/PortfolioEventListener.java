package com.mercury.mercury.listener;

import com.mercury.mercury.Portfolio.service.PortfolioService;
import com.mercury.mercury.Trade.entity.TradeEntity;
import com.mercury.mercury.Trade.repository.TradeRepo;
import com.mercury.mercury.event.TradeSettledEvent;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PortfolioEventListener {
    private final PortfolioService portfolioService;
    private final TradeRepo tradeRepo;

    public PortfolioEventListener(PortfolioService portfolioService, TradeRepo tradeRepo) {
        this.portfolioService = portfolioService;
        this.tradeRepo = tradeRepo;
    }

    @EventListener
    public void handleTradeSettled(TradeSettledEvent event) {
        log.info("TradeSettledEvent received for Trade ID: {}. Triggering Portfolio Accounting Matrix updates.", event.getTradeId());

        TradeEntity trade = tradeRepo.findById(event.getTradeId())
                .orElseThrow(() -> new EntityNotFoundException("Trade Record target missing from context registers: " + event.getTradeId()));

        portfolioService.updatePortfolioPosition(trade);
    }
}
