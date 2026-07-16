package com.mercury.mercury.event.publisher;

import com.mercury.mercury.event.TradeApprovedEvent;
import com.mercury.mercury.event.TradeCreatedEvent;
import com.mercury.mercury.event.TradeSettledEvent;

public interface TradeEventPublisher {
    void publishTradeCreated(TradeCreatedEvent event);
    void publishTradeApproved(TradeApprovedEvent event);
    void publishTradeSettled(TradeSettledEvent event);

}
