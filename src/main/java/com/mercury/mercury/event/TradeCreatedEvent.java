package com.mercury.mercury.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class TradeCreatedEvent {
    private final Long tradeId;
    private final Long userId;
    private final LocalDateTime occuredAt;
}
