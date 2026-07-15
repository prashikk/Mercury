package com.mercury.mercury.Trade.state;

import com.mercury.mercury.Trade.Enum.TradeStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class TradeStateValidator {
    private static final Map<TradeStatus, Set<TradeStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(TradeStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(TradeStatus.NEW, Set.of(
                TradeStatus.PENDING_APPROVAL,
                TradeStatus.FAILED
        ));

        ALLOWED_TRANSITIONS.put(TradeStatus.PENDING_APPROVAL, Set.of(
                TradeStatus.APPROVED,
                TradeStatus.FAILED
        ));

        ALLOWED_TRANSITIONS.put(TradeStatus.APPROVED, Set.of(
                TradeStatus.VALIDATED,
                TradeStatus.FAILED
        ));

        ALLOWED_TRANSITIONS.put(TradeStatus.VALIDATED, Set.of(
                TradeStatus.SETTLED,
                TradeStatus.FAILED
        ));

        ALLOWED_TRANSITIONS.put(TradeStatus.SETTLED, Set.of());
        ALLOWED_TRANSITIONS.put(TradeStatus.FAILED, Set.of());
    }

    public void validateTransition(TradeStatus currentStatus, TradeStatus targetStatus) {
        log.info("Trade Lifecycle Started");
        log.info("Current Status: {}", currentStatus);
        log.info("Requested Status: {}", targetStatus);

        if (currentStatus == targetStatus) {
            log.info("Transition Allowed");
            return;
        }

        Set<TradeStatus> validNextStates = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());

        if (!validNextStates.contains(targetStatus)) {
            log.error("Transition Rejected");
            throw new IllegalArgumentException(
                    String.format("Invalid Trade State Transition: Cannot move trade from state [%s] to [%s]",
                            currentStatus, targetStatus)
            );
        }

        log.info("Transition Allowed");
        if (targetStatus == TradeStatus.APPROVED) {
            log.info("Trade Approved successfully");
        }
        if (targetStatus == TradeStatus.SETTLED) {
            log.info("Trade Settled");
        }
    }
}
