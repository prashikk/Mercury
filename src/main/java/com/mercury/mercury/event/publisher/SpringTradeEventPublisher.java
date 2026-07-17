package com.mercury.mercury.event.publisher;

import com.mercury.mercury.event.TradeApprovedEvent;
import com.mercury.mercury.event.TradeCreatedEvent;
import com.mercury.mercury.event.TradeSettledEvent;
import com.mercury.mercury.kafka.producer.TradeKafkaProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Primary
@Component
public class SpringTradeEventPublisher implements TradeEventPublisher{

    private final TradeKafkaProducer tradeKafkaProducer;

    public SpringTradeEventPublisher(TradeKafkaProducer tradeKafkaProducer) {
        this.tradeKafkaProducer = tradeKafkaProducer;
    }

    @Override
    public void publishTradeCreated(TradeCreatedEvent event) {
        // Task 5: Updated logging requirement
        log.info("Publishing TradeCreatedEvent via EventPublisher | Trade ID: {}", event.getTradeId());
        tradeKafkaProducer.sendTradeCreated(event);
    }

    @Override
    public void publishTradeApproved(TradeApprovedEvent event) {
        log.info("Publishing TradeApprovedEvent via EventPublisher | Trade ID: {}", event.getTradeId());
        tradeKafkaProducer.sendTradeApproved(event);
    }

    @Override
    public void publishTradeSettled(TradeSettledEvent event) {
        log.info("Publishing TradeSettledEvent via EventPublisher | Trade ID: {}", event.getTradeId());
        tradeKafkaProducer.sendTradeSettled(event);
    }
}
