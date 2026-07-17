package com.mercury.mercury.kafka.consumer;

import com.mercury.mercury.kafka.producer.KafkaTopicConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TradeAuditConsumer {
    @KafkaListener(topics = KafkaTopicConstants.TRADE_EVENTS, groupId = "audit-group")
    public void consumeTradeAuditEvent(ConsumerRecord<String, Object> record){
        String key = record.key();
        Object payload = record.value();
        log.info("[Audit Consumer Group] → AUDIT EVENT CAPTURED | Context Key: {} | Payload Schema Content: {}", key, payload);
    }
}
