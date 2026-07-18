package com.mercury.mercury.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementStatisticsResponse {
    private String averageSettlementTime;
    private String fastestSettlement;
    private String slowestSettlement;
    private long totalSettledToday;
}
