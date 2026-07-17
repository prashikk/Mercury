package com.mercury.mercury.kafka.producer;

import com.mercury.mercury.event.TradeApprovedEvent;
import com.mercury.mercury.event.TradeCreatedEvent;
import com.mercury.mercury.event.TradeSettledEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TradeKafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TradeKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendTradeCreated(TradeCreatedEvent event) {
        log.info("Sending TradeCreatedEvent | Trade ID: {} to topic: {}", event.getTradeId(), KafkaTopicConstants.TRADE_EVENTS);
        kafkaTemplate.send(KafkaTopicConstants.TRADE_EVENTS, "CREATED_" + event.getTradeId(), event);
    }

    public void sendTradeApproved(TradeApprovedEvent event) {
        log.info("Sending TradeApprovedEvent | Trade ID: {} to topic: {}", event.getTradeId(), KafkaTopicConstants.TRADE_EVENTS);
        kafkaTemplate.send(KafkaTopicConstants.TRADE_EVENTS, "APPROVED_" + event.getTradeId(), event);
    }

    public void sendTradeSettled(TradeSettledEvent event) {
        log.info("Sending TradeSettledEvent | Trade ID: {} to topic: {}", event.getTradeId(), KafkaTopicConstants.TRADE_EVENTS);
        kafkaTemplate.send(KafkaTopicConstants.TRADE_EVENTS, "SETTLED_" + event.getTradeId(), event);
    }
}
