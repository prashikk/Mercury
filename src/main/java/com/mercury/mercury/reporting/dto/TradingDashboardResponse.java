package com.mercury.mercury.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradingDashboardResponse {
    private long totalTrades;
    private long pendingApproval;
    private long validatedTrades;
    private long approvedTrades;
    private long settledTrades;
    private long rejectedTrades;
    private long failedTrades;
    private BigDecimal totalTradeValue;
}
