package com.mercury.mercury.Trade.dto;

import com.mercury.mercury.Client.Enum.TradeType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeRequestDTO {
    @NotNull(message = "Client ID is Required")
    private Long clientId;

    @NotNull(message = "Instrument ID is required")
    private Long instrumentId;

    @NotNull(message = "Trade type is required (Must be BUY or SELL)")
    private TradeType tradeType;

    @NotNull(message = "quantity is required")
    @Min(value = 1, message = "minimum 1 quantity is required")
    private Integer quantity;
    
    @NotNull(message = "price is required")
    @DecimalMin(value = "0.01", message = "price must be greater than 0")
    private BigDecimal price;
}
