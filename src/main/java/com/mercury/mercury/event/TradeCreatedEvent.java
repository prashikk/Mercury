package com.mercury.mercury.event;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TradeCreatedEvent {
    private final Long tradeId;
    private final Long userId;
    private final LocalDateTime occuredAt;

    public TradeCreatedEvent(Long tradeId, Long userId, LocalDateTime occuredAt) {
        this.tradeId = tradeId;
        this.userId = userId;
        this.occuredAt = occuredAt;
    }
}
