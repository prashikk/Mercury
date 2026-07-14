package com.mercury.mercury.Trade.dto;

import com.mercury.mercury.Trade.Enum.TradeStatus;
import com.mercury.mercury.Trade.Enum.TradeType;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TradeSearchRequest {
    private Long clientId;
    private Long instrumentId;
    private TradeStatus status;
    private TradeType tradeType;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime toDate;

    private Integer minQuantity;
    private Integer maxQuantity;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Long createdBy;

    public void validate() {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("Validation Failed: fromDate cannot be after toDate");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Validation Failed: minPrice cannot be greater than maxPrice");
        }
    }
}