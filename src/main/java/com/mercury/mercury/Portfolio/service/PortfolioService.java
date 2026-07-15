package com.mercury.mercury.Portfolio.service;

import com.mercury.mercury.Portfolio.entity.PortfolioEntity;
import com.mercury.mercury.Portfolio.exception.InsufficientHoldingsException;
import com.mercury.mercury.Portfolio.repository.PortfolioRepo;
import com.mercury.mercury.Portfolio.validation.PortfolioValidator;
import com.mercury.mercury.Trade.Enum.TradeType;
import com.mercury.mercury.Trade.entity.TradeEntity;
import com.mercury.mercury.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Slf4j
@Service
public class PortfolioService {
    private final PortfolioRepo portfolioRepo;
    private final PortfolioValidator portfolioValidator;
    private final NotificationService notificationService;

    public PortfolioService(PortfolioRepo portfolioRepo, PortfolioValidator portfolioValidator, NotificationService notificationService) {
        this.portfolioRepo = portfolioRepo;
        this.portfolioValidator = portfolioValidator;
        this.notificationService = notificationService;
    }

    public void updatePortfolioPosition(TradeEntity trade){
        log.info("Porfolio Update Started for Trade ID: {}", trade.getTrade_id());

        PortfolioEntity portfolio = portfolioRepo.findByClientId_ClientIDAndInstrumentId_InstrumentID(trade.getClient_id().getClientID(), trade.getInstrument_id().getInstrumentID()).orElse(null);

        if (portfolio != null){
            log.info("Portfolio Found for Client ID: {} and Instrument ID: {}", trade.getClient_id().getClientID(), trade.getInstrument_id().getInstrumentID());
        }
        else {
            log.info("Portfolio Created");
            portfolio = new PortfolioEntity();
            portfolio.setClientId(trade.getClient_id());
            portfolio.setInstrumentId(trade.getInstrument_id());
            portfolio.setQuantity(0L);
            portfolio.setAverageBuyPrice(BigDecimal.ZERO);
        }


        if(trade.getTrade_type() == TradeType.BUY){
            BigDecimal currentTotalCost = BigDecimal.valueOf(portfolio.getQuantity()).multiply(portfolio.getAverageBuyPrice());
            BigDecimal newTradeCost = BigDecimal.valueOf(trade.getQuantity()).multiply(trade.getPrice());
            BigDecimal cumulitiveCost = currentTotalCost.add(newTradeCost);

            Long totalQuantity = portfolio.getQuantity() + trade.getQuantity();
            portfolio.setQuantity(totalQuantity);

            BigDecimal newAveragePrice = cumulitiveCost.divide(BigDecimal.valueOf(totalQuantity), 4, RoundingMode.HALF_UP);
            portfolio.setAverageBuyPrice(newAveragePrice);
            log.info("Average Buy Price Updated to: {}", newAveragePrice);
        }else if(trade.getTrade_type() == TradeType.SELL){
            try{
                portfolioValidator.validateHoldings(portfolio, trade);
            }catch (InsufficientHoldingsException e){
                log.error("Insufficient Holdings: Sale quantity exceeds current portfolio balance holdings.");
                throw e;
            }
            Long newQuantity = portfolio.getQuantity() - trade.getQuantity();
            portfolio.setQuantity(newQuantity);
            log.info("Portfolio Quantity Updated to: {}", newQuantity);
        }
        portfolio.setLastUpdated(LocalDateTime.now());
        portfolioRepo.save(portfolio);
        log.info("Portfolio Updated");
        notificationService.createPortfolioNotification(trade.getClient_id().getClientID(), trade.getTrade_id(), portfolio.getQuantity());
    }
}
