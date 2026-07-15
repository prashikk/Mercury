package com.mercury.mercury.Trade.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequestDTO {
    @NotNull(message = "Field 'approvedBy' is mandatory")
    Long approvedBy;
}
