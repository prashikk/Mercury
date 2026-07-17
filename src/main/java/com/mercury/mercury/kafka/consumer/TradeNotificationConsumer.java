package com.mercury.mercury.kafka.consumer;

import com.mercury.mercury.event.TradeApprovedEvent;
import com.mercury.mercury.event.TradeCreatedEvent;
import com.mercury.mercury.event.TradeSettledEvent;
import com.mercury.mercury.kafka.producer.KafkaTopicConstants;
import com.mercury.mercury.notification.service.NotificationService;
import com.mercury.mercury.notification.domain.Enum.NotificationType; // 💡 Import your notification type enum
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class TradeNotificationConsumer {
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public TradeNotificationConsumer(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = KafkaTopicConstants.TRADE_EVENTS, groupId = "notification-group")
    public void consumeTradeNotificationEvent(ConsumerRecord<String, Object> record) {
        String key = record.key(); // 💡 FIXED: Kafka ConsumerRecord method is lower-case record.key()
        Object payload = record.value();

        log.info("[Notification Consumer] Received message packet from partition: {} with key: {}", record.partition(), key);

        if (key == null) return;

        try {
            if (key.startsWith("CREATED_")) {
                TradeCreatedEvent event = objectMapper.convertValue(payload, TradeCreatedEvent.class);
                log.info("[Notification Consumer] Executing notification layout routing for Trade Created ID: {}", event.getTradeId());

                notificationService.saveNotification(
                        event.getTradeId(),
                        event.getUserId(),
                        NotificationType.TRADE_CREATED,
                        "Trade Created",
                        "Trade initialized successfully by User ID: " + event.getUserId()
                );

            } else if (key.startsWith("APPROVED_")) {
                TradeApprovedEvent event = objectMapper.convertValue(payload, TradeApprovedEvent.class);
                log.info("[Notification Consumer] Executing notification layout routing for Trade Approved ID: {}", event.getTradeId());

                notificationService.saveNotification(
                        event.getTradeId(),
                        event.getUserId(),
                        NotificationType.TRADE_APPROVED,
                        "Trade Approved",
                        "Trade approved by User ID: " + event.getUserId()
                );

            } else if (key.startsWith("SETTLED_")) {
                TradeSettledEvent event = objectMapper.convertValue(payload, TradeSettledEvent.class);
                log.info("[Notification Consumer] Executing notification layout routing for Trade Settled ID: {}", event.getTradeId());

                notificationService.saveNotification(
                        event.getTradeId(),
                        null,
                        NotificationType.TRADE_SETTLED,
                        "Trade Settled",
                        "Trade settled successfully."
                );
            }
        } catch (Exception e) {
            log.error("[Notification Consumer] Target message conversion processing error on key '{}'", key, e);
        }
    }
}