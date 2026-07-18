package com.mercury.mercury.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TopEntityResponse {
    private String identifier;
    private long totalTrades;
    private BigDecimal totalValue;
}
