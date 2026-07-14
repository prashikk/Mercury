package com.mercury.mercury.Trade;

import com.mercury.mercury.Trade.dto.*;
import com.mercury.mercury.Trade.service.SettlementService;
import com.mercury.mercury.Trade.service.TradeLifecycleService;
import com.mercury.mercury.Trade.service.TradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trades")
@Tag(name = "Trade Management", description = "End point for Trade Controller")
public class TradeController {
    private final TradeService tradeService;
    public final TradeLifecycleService tradeLifecycleService;
    public final SettlementService settlementService;

    public TradeController(TradeService tradeService, TradeLifecycleService tradeLifecycleService, SettlementService settlementService) {
        this.tradeService = tradeService;
        this.tradeLifecycleService = tradeLifecycleService;
        this.settlementService = settlementService;
    }

    @PostMapping
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
        TradeResponseDTO response = tradeLifecycleService.TransationStatus(id, payload.getStatus());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{tradeId}/settle")
    @Operation(summary = "POST Settle Trade", description = "Executes automated back-office clearing pipeline actions to securely settle a validated transaction record instance.")
    public ResponseEntity<java.util.Map<String , Object>> settleTrade(@PathVariable Long tradeId, @RequestParam(defaultValue = "1") Long processingUserId){
        java.util.Map<String, Object> response = settlementService.settleTrade(tradeId, processingUserId);
        return ResponseEntity.ok(response);
    }
}
