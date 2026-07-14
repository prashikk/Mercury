package com.mercury.mercury.Trade;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    public TradeController(TradeService tradeService){
        this.tradeService = tradeService;
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
            @ModelAttribute TradeSearchRequest request, // Binds parameters into the DTO safely
            Pageable pageable) { // Automatically captures page, size, and sorting directives

        Page<TradeResponseDTO> trades = tradeService.getFilteredTrades(request, pageable);
        return ResponseEntity.ok(trades);
    }

}
