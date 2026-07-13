package com.mercury.mercury.Trade;

import com.mercury.mercury.Client.Enum.TradeStatus;
import com.mercury.mercury.Client.Enum.TradeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeResponseDTO {
    private Long tradeId;

    private Long clientId;
    private String clientName;

    private Long instrumentId;
    private String ticker;

    private TradeType tradeType;
    private Integer quantity;
    private BigDecimal price;

    private LocalDateTime tradeDate;
    private LocalDateTime settledDate;
    private TradeStatus status;

    private LocalDateTime createdTime;
    private LocalDateTime updatedAt;
}
