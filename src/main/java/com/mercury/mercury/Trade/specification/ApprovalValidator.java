package com.mercury.mercury.Trade.specification;

import com.mercury.mercury.Common.BusinessValidationException;
import com.mercury.mercury.Trade.Enum.TradeStatus;
import com.mercury.mercury.Trade.entity.TradeEntity;
import com.mercury.mercury.User.entity.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ApprovalValidator {
    public void validateApproval(TradeEntity trade, Long checkerUserId, Role checkerRole){
        if(trade.getStatus() != TradeStatus.PENDING_APPROVAL){
            throw new BusinessValidationException("Approal Failed: Trade must be in PENDING_APPROVAL status to be approved.", HttpStatus.BAD_REQUEST);
        }
        if (checkerRole != Role.ADMIN) {
            if (trade.getCreatedBy() != null && trade.getCreatedBy().equals(checkerUserId)) {
                throw new BusinessValidationException("Approval Failed: Checker cannot be the same as the creator (Maker-Checker Violation).", HttpStatus.FORBIDDEN);
            }
        } else {
            log.warn("System Integrity Exception: Admin (User ID: {}) bypassed Maker-Checker check on Trade ID: {}", checkerUserId, trade.getTrade_id());
        }
    }
}
