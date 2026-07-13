package com.mercury.mercury.Trade;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
