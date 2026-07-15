package com.mercury.mercury.Trade.specification;

import com.mercury.mercury.Common.BusinessValidationException;
import com.mercury.mercury.Trade.Enum.TradeStatus;
import com.mercury.mercury.Trade.entity.TradeEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ApprovalValidator {
    public void validateApproval(TradeEntity trade, Long checkerUserId){
        if(trade.getStatus() != TradeStatus.PENDING_APPROVAL){
            throw new BusinessValidationException("Approal Failed: Trade must be in PENDING_APPROVAL status to be approved.", HttpStatus.BAD_REQUEST);
        }
        if(trade.getCreatedBy() != null && trade.getCreatedBy().equals(checkerUserId)){
            throw new BusinessValidationException("Approval Failed: Checker cannot be the same as the creator of the trade.", HttpStatus.FORBIDDEN);
        }
    }
}
