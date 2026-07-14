package com.mercury.mercury.Trade.dto;

import com.mercury.mercury.Client.Enum.TradeStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TradeStatusUpdateRequest {
    @NotNull(message = "Status cannot be Null")
    private TradeStatus status;
}

