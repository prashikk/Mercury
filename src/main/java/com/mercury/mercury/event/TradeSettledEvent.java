package com.mercury.mercury.event;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(force = true)
public class TradeSettledEvent {
    private final Long tradeId;
    private final String settlementRef;
    private final LocalDateTime occuredAt;

    public TradeSettledEvent(Long tradeId, String settlementRef, LocalDateTime occuredAt) {
        this.tradeId = tradeId;
        this.settlementRef = settlementRef;
        this.occuredAt = occuredAt;
    }
}
