package com.mercury.mercury.Trade;

import com.mercury.mercury.Client.Enum.TradeStatus;
import com.mercury.mercury.Client.Enum.TradeType;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/trades")
public class TradeController {
    private final TradeService tradeService;

    public TradeController(TradeService tradeService){
        this.tradeService = tradeService;
    }

    @PostMapping
    public ResponseEntity<TradeResponseDTO> createTrade(@Valid @RequestBody TradeRequestDTO requestDTO){
        TradeResponseDTO response = tradeService.executeTrade(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<TradeResponseDTO>> searchTrades(
            @ModelAttribute TradeSearchRequest request, // Binds parameters into the DTO safely
            Pageable pageable) { // Automatically captures page, size, and sorting directives

        Page<TradeResponseDTO> trades = tradeService.getFilteredTrades(request, pageable);
        return ResponseEntity.ok(trades);
    }

}
