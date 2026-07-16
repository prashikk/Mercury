package com.mercury.mercury.Trade;

import com.mercury.mercury.Trade.dto.*;
import com.mercury.mercury.Trade.service.ApprovalService;
import com.mercury.mercury.Trade.service.SettlementService;
import com.mercury.mercury.Trade.service.TradeLifecycleService;
import com.mercury.mercury.Trade.service.TradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/trades")
@Tag(name = "Trade Management", description = "End point for Trade Controller")
public class TradeController {
    private final TradeService tradeService;
    public final TradeLifecycleService tradeLifecycleService;
    public final SettlementService settlementService;
    public final ApprovalService approvalService;

    public TradeController(TradeService tradeService, TradeLifecycleService tradeLifecycleService, SettlementService settlementService, ApprovalService approvalService) {
        this.tradeService = tradeService;
        this.tradeLifecycleService = tradeLifecycleService;
        this.settlementService = settlementService;
        this.approvalService = approvalService;
    }

    @PostMapping
    @PreAuthorize("hasRole('TRADER') or hasRole('ADMIN')")
    @Operation(summary = "Create a new Trade", description = "submit new trades in the lifeCycle.")
    public ResponseEntity<TradeResponseDTO> createTrade(@Valid @RequestBody TradeRequestDTO requestDTO){
        TradeResponseDTO response = tradeService.executeTrade(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "filter Trade", description = "filter trade dynamicaly using JPASpecifications.")
    public ResponseEntity<Page<TradeResponseDTO>> searchTrades(
            @ModelAttribute TradeSearchRequest request,
            Pageable pageable) {

        Page<TradeResponseDTO> trades = tradeService.getFilteredTrades(request, pageable);
        return ResponseEntity.ok(trades);
    }

    @PutMapping("/{tradeId}")
    @Operation(summary = "Update Trade", description = "update trade by tradeId ")
    public ResponseEntity<TradeResponseDTO> updateTrade(@PathVariable Long tradeId, @Valid @RequestBody TradeUpdateRequestDTO updateDTO ){
        TradeResponseDTO response = tradeService.updateTrade(tradeId, updateDTO);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update Trade Status", description = "Drives the trade state machine forward following strict operational settlement rules. ")
    public ResponseEntity<TradeResponseDTO> updateStatus(@PathVariable Long id, @Valid @RequestBody TradeStatusUpdateRequest payload){
        TradeResponseDTO response = tradeLifecycleService.transationStatus(id, payload.getStatus());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{tradeId}/settle")
    @PreAuthorize("hasRole('OPERATIONS') or hasRole('ADMIN')")
    @Operation(summary = "POST Settle Trade", description = "Executes automated back-office clearing pipeline actions to securely settle a validated transaction record instance.")
    public ResponseEntity<java.util.Map<String, Object>> settleTrade(
            @PathVariable Long tradeId,
            Authentication authentication) {
        String operationsUsername = authentication.getName();

        java.util.Map<String, Object> response = settlementService.settleTrade(tradeId, operationsUsername);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{tradeId}/approve")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "POST Approve Trade")
    public ResponseEntity<java.util.Map<String, Object>> approveTrade(
            @PathVariable Long tradeId,
            Authentication authentication) {

        String managerUsername = authentication.getName();
        java.util.Map<String, Object> response = approvalService.approveTrade(tradeId, managerUsername);

        return ResponseEntity.ok(response);
    }
}
