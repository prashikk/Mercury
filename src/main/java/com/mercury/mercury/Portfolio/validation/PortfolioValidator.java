package com.mercury.mercury.Portfolio.validation;

import com.mercury.mercury.Portfolio.entity.PortfolioEntity;
import com.mercury.mercury.Trade.entity.TradeEntity;
import org.springframework.stereotype.Component;

@Component
public class PortfolioValidator {

    public void validateHoldings(PortfolioEntity portfolioEntity, TradeEntity tradeEntity) {

        if(portfolioEntity == null || portfolioEntity.getQuantity() < tradeEntity.getQuantity()) {
            throw new IllegalArgumentException("Insufficient Holdings: Sale quantity exceeds current portfolio balance holdings.");
        }
    }
}
