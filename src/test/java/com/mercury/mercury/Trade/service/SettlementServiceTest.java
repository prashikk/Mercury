package com.mercury.mercury.Trade.service;

import com.mercury.mercury.Client.ClientEntity;
import com.mercury.mercury.Client.Enum.KycStatus;
import com.mercury.mercury.Portfolio.service.PortfolioService;
import com.mercury.mercury.Trade.Enum.TradeStatus;
import com.mercury.mercury.Common.BusinessValidationException;
import com.mercury.mercury.Instruments.InstrumentEntity;
import com.mercury.mercury.Trade.entity.TradeEntity;
import com.mercury.mercury.Trade.repository.TradeRepo;
import com.mercury.mercury.Trade.specification.SettlementValidator;
import com.mercury.mercury.User.service.AuthenticatedUserService;
import com.mercury.mercury.notification.service.NotificationService;
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

    @Spy
    private SettlementValidator settlementValidator;

    @Mock
    private TradeLifecycleService lifecycleService;

    @Mock
    private PortfolioService portfolioService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuthenticatedUserService authenticatedUserService; // 💡 Mocked security context

    @InjectMocks
    private SettlementService settlementService;

    @Test
    @DisplayName("Test Trade VALIDATED -> SETTLED should Pass")
    void test_ValidatedToSettledShouldPass() {
        String opsUsername = "opsUser";
        Long opsUserId = 5L;

        TradeEntity trade = createBaseValidTrade();
        trade.setStatus(TradeStatus.VALIDATED);

        when(authenticatedUserService.getCurrentUsername()).thenReturn(opsUsername);
        when(authenticatedUserService.getCurrentUserId()).thenReturn(opsUserId);
        when(tradeRepo.findById(101L)).thenReturn(Optional.of(trade));
        when(tradeRepo.save(any(TradeEntity.class))).thenReturn(trade);

        java.util.Map<String, Object> result = settlementService.settleTrade(101L);

        assertNotNull(result);
        assertEquals(101L, result.get("tradeId"));

        verify(lifecycleService, times(1)).transationStatus(101L, TradeStatus.SETTLED);
        verify(tradeRepo, times(1)).save(trade);
        verify(portfolioService, times(1)).updatePortfolioPosition(trade);
        verify(notificationService, times(1)).createSettlementNotification(eq(101L), anyString());
    }

    @Test
    @DisplayName("Test Trade NEW -> Settlement should Fail")
    void test_NewTradeSettlementShouldFail() {
        TradeEntity trade = createBaseValidTrade();
        trade.setStatus(TradeStatus.NEW);

        when(authenticatedUserService.getCurrentUsername()).thenReturn("opsUser");
        when(authenticatedUserService.getCurrentUserId()).thenReturn(5L);
        when(tradeRepo.findById(101L)).thenReturn(Optional.of(trade));

        assertThrows(BusinessValidationException.class, () ->
                settlementService.settleTrade(101L)
        );
        verify(tradeRepo, never()).save(any());
    }

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