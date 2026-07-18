package com.mercury.mercury.Trade.settlement.batch;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settlement")
@RequiredArgsConstructor
@Tag(name = "Settlement Batch Management", description = "Endpoints for handling automated clearing actions across multiple records")
public class SettlementBatchController {
    private final SettlementBatchService settlementBatchService;

    @PostMapping("/run-batch")
    @PreAuthorize("hasRole('OPERATIONS') or hasRole('ADMIN')")
    @Operation(summary = "Run Settlement Batch", description = "Triggers the back-office clearing pipeline to settle all outstanding approved transactions concurrently.")
    public ResponseEntity<SettlementBatchResult> runSettlementBatch() {
        SettlementBatchResult summaryReport = settlementBatchService.executeSettlmentBatch();
        return ResponseEntity.ok(summaryReport);
    }
}
