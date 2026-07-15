package com.mercury.mercury.Trade.specification;

import com.mercury.mercury.Client.Enum.KycStatus;
import com.mercury.mercury.Trade.Enum.TradeStatus;
import com.mercury.mercury.Common.BusinessValidationException;
import com.mercury.mercury.Trade.entity.TradeEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class SettlementValidator {
    public void validateSettlement(TradeEntity tradeEntity){
        if(tradeEntity.getStatus() != TradeStatus.VALIDATED){
            throw new BusinessValidationException("Trade must be in VALIDATED status to be settled.", HttpStatus.CONFLICT);
        }

        if (tradeEntity.getClient_id() == null || tradeEntity.getClient_id().getKycStatus() != KycStatus.APPROVED) {
            throw new BusinessValidationException("Settlement Blocked: Client KYC profile status must be APPROVED.",  HttpStatus.BAD_REQUEST);
        }

        if(tradeEntity.getInstrument_id() == null){
            throw new BusinessValidationException("Settlement Blocked: Instrument must be valid and not null.",  HttpStatus.BAD_REQUEST);
        }

        if(tradeEntity.getQuantity() == null || tradeEntity.getQuantity() <= 0){
            throw new BusinessValidationException("Settlement Blocked: Trade quantity must be greater than 0.",  HttpStatus.BAD_REQUEST);
        }

        if(tradeEntity.getPrice() == null || tradeEntity.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0){
            throw new BusinessValidationException("Settlement Blocked: Trade price must be greater than 0.",  HttpStatus.BAD_REQUEST);
        }

    }
}
