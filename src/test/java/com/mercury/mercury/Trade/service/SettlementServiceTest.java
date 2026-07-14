package com.mercury.mercury.Trade.service;

import com.mercury.mercury.Client.ClientEntity;
import com.mercury.mercury.Client.Enum.KycStatus;
import com.mercury.mercury.Trade.Enum.TradeStatus;
import com.mercury.mercury.Common.SettlementException;
import com.mercury.mercury.Instruments.InstrumentEntity;
import com.mercury.mercury.Trade.entity.TradeEntity;
import com.mercury.mercury.Trade.repository.TradeRepo;
import com.mercury.mercury.Trade.specification.SettlementValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private TradeRepo tradeRepo;

    @Spy // Inject real validator to test deep operational rules integration accurately
    private SettlementValidator settlementValidator;

    @Mock
    private TradeLifecycleService lifecycleService;

    @InjectMocks
    private SettlementService settlementService;

    @Test
    @DisplayName("Test 1: Trade VALIDATED -> SETTLED should Pass successfully")
    void test1_ValidatedToSettledShouldPass() {
        TradeEntity trade = createBaseValidTrade();
        trade.setStatus(TradeStatus.VALIDATED);

        when(tradeRepo.findById(101L)).thenReturn(Optional.of(trade));

        java.util.Map<String, Object> result = settlementService.settleTrade(101L, 1L);

        assertNotNull(result);

        assertEquals(101L, result.get("tradeId"));
        assertNotNull(result.get("settlementReference"));
        assertTrue(result.get("settlementReference").toString().startsWith("SET-"));

        verify(lifecycleService, times(1)).transationStatus(101L, TradeStatus.SETTLED);
        verify(tradeRepo, times(1)).save(trade);
    }


    @Test
    @DisplayName("Test 2: Trade NEW -> Settlement should Fail with 409 Conflict")
    void test2_NewTradeSettlementShouldFail() {
        // Arrange
        TradeEntity trade = createBaseValidTrade();
        trade.setStatus(TradeStatus.NEW); // Break status rule

        when(tradeRepo.findById(101L)).thenReturn(Optional.of(trade));

        // Act & Assert
        SettlementException ex = assertThrows(SettlementException.class, () ->
                settlementService.settleTrade(101L, 1L)
        );
        assertEquals(HttpStatus.CONFLICT, ex.getHttpStatus());
        verify(tradeRepo, never()).save(any());
    }

    @Test
    @DisplayName("Test 3: Client KYC=PENDING -> Settlement should Fail with 400 Bad Request")
    void test3_KycPendingShouldFail() {
        // Arrange
        TradeEntity trade = createBaseValidTrade();
        trade.setStatus(TradeStatus.VALIDATED);
        trade.getClient_id().setKycStatus(KycStatus.PENDING); // Break KYC rule

        when(tradeRepo.findById(101L)).thenReturn(Optional.of(trade));

        // Act & Assert
        SettlementException ex = assertThrows(SettlementException.class, () ->
                settlementService.settleTrade(101L, 1L)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
    }

    @Test
    @DisplayName("Test 4: Instrument Missing -> Settlement should Fail with 400 Bad Request")
    void test4_InstrumentMissingShouldFail() {
        // Arrange
        TradeEntity trade = createBaseValidTrade();
        trade.setStatus(TradeStatus.VALIDATED);
        trade.setInstrument_id(null); // Break Instrument assignment rule

        when(tradeRepo.findById(101L)).thenReturn(Optional.of(trade));

        // Act & Assert
        SettlementException ex = assertThrows(SettlementException.class, () ->
                settlementService.settleTrade(101L, 1L)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
    }

    @Test
    @DisplayName("Test 5: Settlement Reference Pattern Verification (SET-yyyyMMdd-XXXXXX)")
    void test5_VerifySettlementReferencePattern() {
        // Act
        String reference = settlementService.generateSettlementReference();

        // Assert: Match regex expression logic tracking format precisely
        // Explains pattern: Starts with SET-, followed by 8 numbers, a dash, and exactly 6 digits
        assertTrue(reference.matches("^SET-\\d{8}-\\d{6}$"));
    }

    // --- Helper Utility to construct valid entity profiles mock blocks ---
    private TradeEntity createBaseValidTrade() {
        ClientEntity client = new ClientEntity();
        client.setClientID(1L);
        client.setKycStatus(KycStatus.APPROVED);

        InstrumentEntity instrument = new InstrumentEntity();
        instrument.setInstrumentID(1L);

        TradeEntity trade = new TradeEntity();
        trade.setTrade_id(101L);
        trade.setClient_id(client);
        trade.setInstrument_id(instrument);
        trade.setQuantity(100);
        trade.setPrice(BigDecimal.valueOf(150.00));
        return trade;
    }
}