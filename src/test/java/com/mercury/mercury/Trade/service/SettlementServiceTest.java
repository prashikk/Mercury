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
import com.mercury.mercury.User.entity.UserEntity;
import com.mercury.mercury.User.repository.UserRepository;
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

    @Spy // Inject real validator to test deep operational rules integration accurately
    private SettlementValidator settlementValidator;

    @Mock
    private TradeLifecycleService lifecycleService;

    @Mock
    private PortfolioService portfolioService; // 💡 Mock dependency

    @Mock
    private NotificationService notificationService; // 💡 Mock dependency

    @Mock
    private UserRepository userRepository; // 💡 Mock dependency

    @InjectMocks
    private SettlementService settlementService;

    @Test
    @DisplayName("Test 1: Trade VALIDATED -> SETTLED should Pass successfully")
    void test1_ValidatedToSettledShouldPass() {
        // Arrange
        String opsUsername = "opsUser";
        Long opsUserId = 5L;

        UserEntity mockUser = new UserEntity();
        mockUser.setUserId(opsUserId);
        mockUser.setUsername(opsUsername);

        TradeEntity trade = createBaseValidTrade();
        trade.setStatus(TradeStatus.VALIDATED);

        when(userRepository.findByUsername(opsUsername)).thenReturn(Optional.of(mockUser));
        when(tradeRepo.findById(101L)).thenReturn(Optional.of(trade));
        when(tradeRepo.save(any(TradeEntity.class))).thenReturn(trade);

        // Act
        java.util.Map<String, Object> result = settlementService.settleTrade(101L, opsUsername);

        // Assert
        assertNotNull(result);
        assertEquals(101L, result.get("tradeId"));
        assertNotNull(result.get("settlementReference"));
        assertTrue(result.get("settlementReference").toString().startsWith("SET-"));

        verify(lifecycleService, times(1)).transationStatus(101L, TradeStatus.SETTLED);
        verify(tradeRepo, times(1)).save(trade);
        verify(portfolioService, times(1)).updatePortfolioPosition(trade);
        verify(notificationService, times(1)).createSettlementNotification(eq(opsUserId), eq(101L), anyString());
    }

    @Test
    @DisplayName("Test 2: Trade NEW -> Settlement should Fail with 409 Conflict")
    void test2_NewTradeSettlementShouldFail() {
        // Arrange
        String opsUsername = "opsUser";
        Long opsUserId = 5L;

        UserEntity mockUser = new UserEntity();
        mockUser.setUserId(opsUserId);
        mockUser.setUsername(opsUsername);

        TradeEntity trade = createBaseValidTrade();
        trade.setStatus(TradeStatus.NEW); // Break status rule

        when(userRepository.findByUsername(opsUsername)).thenReturn(Optional.of(mockUser));
        when(tradeRepo.findById(101L)).thenReturn(Optional.of(trade));

        // Act & Assert
        BusinessValidationException ex = assertThrows(BusinessValidationException.class, () ->
                settlementService.settleTrade(101L, opsUsername)
        );
        assertEquals(HttpStatus.CONFLICT, ex.getHttpStatus());
        verify(tradeRepo, never()).save(any());
    }

    @Test
    @DisplayName("Test 3: Client KYC=PENDING -> Settlement should Fail with 400 Bad Request")
    void test3_KycPendingShouldFail() {
        // Arrange
        String opsUsername = "opsUser";
        Long opsUserId = 5L;

        UserEntity mockUser = new UserEntity();
        mockUser.setUserId(opsUserId);
        mockUser.setUsername(opsUsername);

        TradeEntity trade = createBaseValidTrade();
        trade.setStatus(TradeStatus.VALIDATED);
        trade.getClient_id().setKycStatus(KycStatus.PENDING); // Break KYC rule

        when(userRepository.findByUsername(opsUsername)).thenReturn(Optional.of(mockUser));
        when(tradeRepo.findById(101L)).thenReturn(Optional.of(trade));

        // Act & Assert
        BusinessValidationException ex = assertThrows(BusinessValidationException.class, () ->
                settlementService.settleTrade(101L, opsUsername)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
    }

    @Test
    @DisplayName("Test 4: Instrument Missing -> Settlement should Fail with 400 Bad Request")
    void test4_InstrumentMissingShouldFail() {
        // Arrange
        String opsUsername = "opsUser";
        Long opsUserId = 5L;

        UserEntity mockUser = new UserEntity();
        mockUser.setUserId(opsUserId);
        mockUser.setUsername(opsUsername);

        TradeEntity trade = createBaseValidTrade();
        trade.setStatus(TradeStatus.VALIDATED);
        trade.setInstrument_id(null); // Break Instrument assignment rule

        when(userRepository.findByUsername(opsUsername)).thenReturn(Optional.of(mockUser));
        when(tradeRepo.findById(101L)).thenReturn(Optional.of(trade));

        // Act & Assert
        BusinessValidationException ex = assertThrows(BusinessValidationException.class, () ->
                settlementService.settleTrade(101L, opsUsername)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
    }

    @Test
    @DisplayName("Test 5: Settlement Reference Pattern Verification (SET-yyyyMMdd-XXXXXX)")
    void test5_VerifySettlementReferencePattern() {
        // Act
        String reference = settlementService.generateSettlementReference();

        // Assert
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