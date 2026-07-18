package com.mercury.mercury.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class TradeMetricsService {

    private final MeterRegistry meterRegistry;

    // Counters
    private final Counter tradesCreatedCounter;
    private final Counter tradesApprovedCounter;
    private final Counter tradesSettledCounter;
    private final Counter tradesFailedCounter;
    private final Counter notificationsSentCounter;
    private final Counter batchExecutionsCounter;
    private final Counter dlqEventsCounter;

    public TradeMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.tradesCreatedCounter = Counter.builder("mercury.trade.created")
                .description("Total number of trades initialized in the lifecycle")
                .register(meterRegistry);

        this.tradesApprovedCounter = Counter.builder("mercury.trade.approved")
                .description("Total number of trades approved by management managers")
                .register(meterRegistry);

        this.tradesSettledCounter = Counter.builder("mercury.trade.settled")
                .description("Total number of trades securely cleared and settled")
                .register(meterRegistry);

        this.tradesFailedCounter = Counter.builder("mercury.trade.failed")
                .description("Total number of business validation failures captured")
                .register(meterRegistry);

        this.notificationsSentCounter = Counter.builder("mercury.notifications.sent")
                .description("Total number of outbound notification packets dispatched")
                .register(meterRegistry);

        this.batchExecutionsCounter = Counter.builder("mercury.batch.executions")
                .description("Total number of settlement clearing batch executions run")
                .register(meterRegistry);

        this.dlqEventsCounter = Counter.builder("mercury.dlq.events")
                .description("Total number of message packets relegated to the DLQ dead letter queue")
                .register(meterRegistry);
    }

    public void incrementCreated() { tradesCreatedCounter.increment(); }
    public void incrementApproved() { tradesApprovedCounter.increment(); }
    public void incrementSettled() { tradesSettledCounter.increment(); }
    public void incrementFailed() { tradesFailedCounter.increment(); }
    public void incrementNotifications() { notificationsSentCounter.increment(); }
    public void incrementBatchExecutions() { batchExecutionsCounter.increment(); }
    public void incrementDlq() { dlqEventsCounter.increment(); }

    public void recordBatchPerformance(int size, int successful, int failed, long durationMillis) {
        meterRegistry.gauge("mercury.batch.size.last", size);
        meterRegistry.gauge("mercury.batch.success.last", successful);
        meterRegistry.gauge("mercury.batch.failed.last", failed);

        Timer.builder("mercury.batch.execution.duration")
                .description("Time taken to clear the automated settlement engine routine")
                .register(meterRegistry)
                .record(durationMillis, TimeUnit.MILLISECONDS);
    }
}
