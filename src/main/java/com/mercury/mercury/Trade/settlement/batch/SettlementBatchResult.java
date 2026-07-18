package com.mercury.mercury.Trade.settlement.batch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementBatchResult {
    private int totalTrades;
    private int processed;
    private int successful;
    private int failed;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String duration;
    private List<Long> failedTradeId;
}
