package com.mercury.mercury.reporting.service;

import com.mercury.mercury.Trade.Enum.TradeStatus;
import com.mercury.mercury.Trade.repository.TradeRepo;
import com.mercury.mercury.reporting.dto.SettlementStatisticsResponse;
import com.mercury.mercury.reporting.dto.TopEntityResponse;
import com.mercury.mercury.reporting.dto.TradingDashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradingReportingService {

    private final TradeRepo tradeRepo;

    public TradingDashboardResponse getDashboardMetrics() {
        BigDecimal totalVal = tradeRepo.calculateTotalTradeValue();

        return TradingDashboardResponse.builder()
                .totalTrades(tradeRepo.count())
                .pendingApproval(tradeRepo.countByStatus(TradeStatus.PENDING_APPROVAL))
                .validatedTrades(tradeRepo.countByStatus(TradeStatus.VALIDATED))
                .approvedTrades(tradeRepo.countByStatus(TradeStatus.APPROVED))
                .settledTrades(tradeRepo.countByStatus(TradeStatus.SETTLED))
                .failedTrades(tradeRepo.countByStatus(TradeStatus.FAILED))
                .rejectedTrades(0) // Safe placeholder depending on extended workflows
                .totalTradeValue(totalVal != null ? totalVal : BigDecimal.ZERO)
                .build();
    }

    public List<TopEntityResponse> getTopClients() {
        return tradeRepo.findTopClientsAggregated().stream()
                .limit(5)
                .map(row -> new TopEntityResponse((String) row[0], (Long) row[1], (BigDecimal) row[2]))
                .collect(Collectors.toList());
    }

    public List<TopEntityResponse> getTopInstruments() {
        return tradeRepo.findTopInstrumentsAggregated().stream()
                .limit(5)
                .map(row -> new TopEntityResponse((String) row[0], (Long) row[1], (BigDecimal) row[2]))
                .collect(Collectors.toList());
    }

    public SettlementStatisticsResponse getSettlementStats() {
        List<Object[]> rawMetrics = tradeRepo.getRawSettlementMetrics();
        long totalSettled = 0;

        if (!rawMetrics.isEmpty() && rawMetrics.get(0)[0] != null) {
            totalSettled = (Long) rawMetrics.get(0)[0];
        }

        return SettlementStatisticsResponse.builder()
                .averageSettlementTime("Not yet available (Requires timestamp interval tracking)")
                .fastestSettlement("Not yet available")
                .slowestSettlement("Not yet available")
                .totalSettledToday(totalSettled)
                .build();
    }
}