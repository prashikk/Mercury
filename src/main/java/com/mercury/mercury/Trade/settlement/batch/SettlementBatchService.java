package com.mercury.mercury.Trade.settlement.batch;

import com.mercury.mercury.Trade.Enum.TradeStatus;
import com.mercury.mercury.Trade.entity.TradeEntity;
import com.mercury.mercury.Trade.repository.TradeRepo;
import com.mercury.mercury.Trade.service.SettlementService;
import com.mercury.mercury.monitoring.TradeMetricsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementBatchService {
    private final TradeRepo tradeRepo;
    private final SettlementService settlementService;
    private final TradeMetricsService tradeMetricsService;

    public SettlementBatchResult executeSettlmentBatch(){
        LocalDateTime startTime = LocalDateTime.now();
        tradeMetricsService.incrementBatchExecutions();

        log.info("[Batch Started] Initiating automated settlement engine routine execution pipeline.");

        List<TradeEntity> executableTrades= tradeRepo.findByStatus(TradeStatus.VALIDATED);

        int totalCount = executableTrades.size();
        log.info("[Loading {} Validated Approved Trades] Active database records grabbed.", totalCount);

        int processed = 0;
        int successful = 0;
        int failed = 0;
        List<Long> failedIds = new ArrayList<>();

        for(TradeEntity trade: executableTrades){
            processed++;
            Long currentTradeId = trade.getTrade_id();
            try{
                settleSingleTradeInIsolation(currentTradeId);
                successful++;
                log.info("→ Trade {} Settled successfully.", currentTradeId);
            }catch (Exception e){
                failed++;
                failedIds.add(currentTradeId);
                log.error("⚠ Trade {} Failed during batch processing pipeline. Error: {}", currentTradeId, e.getMessage());
            }
        }

        LocalDateTime endTime = LocalDateTime.now();
        long durationMillis = Duration.between(startTime, endTime).toMillis();
        String displayDuration = String.format("%.2f sec", durationMillis / 1000.0);

        log.info("[Batch Completed] Execution routine concluded cleanly. Processed: {}, Successful: {}, Failed: {}, Duration: {}",
                processed, successful, failed, displayDuration);
        long duration = java.time.Duration.between(startTime, endTime).toMillis();
        tradeMetricsService.recordBatchPerformance(totalCount, successful, failed, duration);

        return SettlementBatchResult.builder()
                .totalTrades(totalCount)
                .processed(processed)
                .successful(successful)
                .failed(failed)
                .startedAt(startTime)
                .completedAt(endTime)
                .duration(displayDuration)
                .failedTradeId(failedIds)
                .build();
    }
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void settleSingleTradeInIsolation(Long tradeId) {
        settlementService.settleTrade(tradeId);
    }
}
