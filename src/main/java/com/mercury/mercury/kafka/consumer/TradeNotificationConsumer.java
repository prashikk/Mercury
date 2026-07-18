package com.mercury.mercury.kafka.consumer;

import com.mercury.mercury.event.TradeApprovedEvent;
import com.mercury.mercury.event.TradeCreatedEvent;
import com.mercury.mercury.event.TradeSettledEvent;
import com.mercury.mercury.kafka.producer.KafkaTopicConstants;
import com.mercury.mercury.monitoring.TradeMetricsService;
import com.mercury.mercury.notification.service.NotificationService;
import com.mercury.mercury.notification.domain.Enum.NotificationType;
import com.mercury.mercury.kafka.dlq.domain.FailedEvent;
import com.mercury.mercury.kafka.dlq.repository.FailedEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@Component
public class TradeNotificationConsumer {
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final FailedEventRepository failedEventRepository;
    private final TradeMetricsService tradeMetricsService;

    public TradeNotificationConsumer(NotificationService notificationService, ObjectMapper objectMapper, FailedEventRepository failedEventRepository, TradeMetricsService tradeMetricsService) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
        this.failedEventRepository = failedEventRepository;
        this.tradeMetricsService = tradeMetricsService;
    }

    @RetryableTopic(attempts = "3", backOff = @BackOff(delay = 2000))
    @KafkaListener(topics = KafkaTopicConstants.TRADE_EVENTS, groupId = "notification-group")
    public void consumeTradeNotificationEvent(ConsumerRecord<String, Object> record) {
        String key = record.key(); // 💡 FIXED: Kafka ConsumerRecord method is lower-case record.key()
        Object payload = record.value();

        log.info("[Notification Consumer] Received message packet from partition: {} with key: {}", record.partition(), key);

        if (key == null) return;

        try { //commented to test dlq
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
            log.error("[Notification Consumer] Target message conversion processing error on key '{}'", key, e);}
    }

    @DltHandler
    public void processDeadLetterQueueEvent(ConsumerRecord<String, Object> record) {
        tradeMetricsService.incrementDlq();
        String key = record.key();
        Object payload = record.value();

        String exceptionMessage = "Unknown pipeline processing exception occurred.";

        Header exceptionHeader = record.headers().lastHeader("org.springframework.kafka.listener.KafkaListenerException.exceptionMessage");
        if (exceptionHeader == null) {
            exceptionHeader = record.headers().lastHeader("kafka_exceptionMessage");
        }

        if (exceptionHeader != null && exceptionHeader.value() != null) {
            exceptionMessage = new String(exceptionHeader.value(), StandardCharsets.UTF_8);
        }

        log.error("[DLQ Consumer Started] 🚨 Message consumption permanently failed for key: '{}'. Reason: {}", key, exceptionMessage);

        FailedEvent failedEvent = new FailedEvent();
        failedEvent.setEventKey(key);
        failedEvent.setEventType(payload != null ? payload.getClass().getSimpleName() : "UNKNOWN");
        failedEvent.setPayload(payload != null ? payload.toString() : "NULL");
        failedEvent.setFailureReason(exceptionMessage);
        failedEvent.setFailedAt(LocalDateTime.now());
        failedEvent.setRetryAttempts(3);
        failedEvent.setStatus("FAILED");

        failedEventRepository.save(failedEvent);
        log.info("[DLQ Event Saved] → Registry storage trace finalized inside database table context for key: {}", key);
    }
}