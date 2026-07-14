package com.mercury.mercury.Trade.dto;

import com.mercury.mercury.Client.Enum.TradeStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TradeUpdateRequestDTO {
    @NotNull(message = "Trade price cannot be null ")
    @Positive(message = "Trade price must be greater than 0 ")
    private BigDecimal price;

    @NotNull(message = "Trade Quantiy cannot be Null")
    @Positive(message = "Trade Quantity must be greater than 0")
    private Integer quantity;

    @NotNull(message = "Status cannot be Null")
    private TradeStatus status;
}
