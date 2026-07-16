package com.mercury.mercury.event;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TradeApprovedEvent {
    private final Long tradeId;
    private final Long userId;
    private final LocalDateTime occuredAt;

    public TradeApprovedEvent(Long tradeId, Long userId, LocalDateTime occuredAt) {
        this.tradeId = tradeId;
        this.userId = userId;
        this.occuredAt = occuredAt;
    }
}
