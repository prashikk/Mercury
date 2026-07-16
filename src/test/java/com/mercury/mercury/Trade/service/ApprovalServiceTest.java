package com.mercury.mercury.Trade.service;

import com.mercury.mercury.Client.ClientEntity;
import com.mercury.mercury.Client.Enum.KycStatus;
import com.mercury.mercury.Common.BusinessValidationException;
import com.mercury.mercury.Instruments.InstrumentEntity;
import com.mercury.mercury.Trade.Enum.TradeStatus;
import com.mercury.mercury.Trade.entity.TradeEntity;
import com.mercury.mercury.Trade.repository.TradeRepo;
import com.mercury.mercury.Trade.specification.ApprovalValidator;
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
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

    @Mock
    private TradeRepo tradeRepo;

    @Spy
    private ApprovalValidator approvalValidator;

    @Mock
    private TradeLifecycleService lifecycleService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuthenticatedUserService authenticatedUserService; // 💡 Mocking our new security service

    @InjectMocks
    private ApprovalService approvalService;

    @Test
    @DisplayName("Test Different User -> Approval Should Pass")
    void test_DifferentUserApprovalShouldPass() {
        Long tradeId = 200L;
        Long makerUserId = 101L;
        Long checkerUserId = 202L;

        TradeEntity trade = createMockTrade(tradeId, makerUserId, TradeStatus.PENDING_APPROVAL);

        when(authenticatedUserService.getCurrentUsername()).thenReturn("managerUser");
        when(authenticatedUserService.getCurrentUserId()).thenReturn(checkerUserId);
        when(tradeRepo.findById(tradeId)).thenReturn(Optional.of(trade));
        when(tradeRepo.save(any(TradeEntity.class))).thenReturn(trade);

        Map<String, Object> response = approvalService.approveTrade(tradeId);

        assertNotNull(response);
        assertEquals("VALIDATED", response.get("status"));

        verify(approvalValidator, times(1)).validateApproval(trade, checkerUserId);
        verify(lifecycleService, times(1)).transationStatus(tradeId, TradeStatus.APPROVED);
        verify(lifecycleService, times(1)).transationStatus(tradeId, TradeStatus.VALIDATED);
        verify(notificationService, times(1)).createApprovalNotification(tradeId);
    }

    @Test
    @DisplayName("Test Maker tries Approve -> Fail with 403 Forbidden")
    void test_MakerApprovingOwnTradeShouldFail() {
        Long tradeId = 201L;
        Long makerUserId = 101L;

        TradeEntity trade = createMockTrade(tradeId, makerUserId, TradeStatus.PENDING_APPROVAL);

        when(authenticatedUserService.getCurrentUsername()).thenReturn("creatorUser");
        when(authenticatedUserService.getCurrentUserId()).thenReturn(makerUserId); // Same ID!
        when(tradeRepo.findById(tradeId)).thenReturn(Optional.of(trade));

        BusinessValidationException ex = assertThrows(BusinessValidationException.class, () ->
                approvalService.approveTrade(tradeId)
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getHttpStatus());
        verify(tradeRepo, never()).save(any());
    }

    private TradeEntity createMockTrade(Long id, Long creatorId, TradeStatus status) {
        ClientEntity client = new ClientEntity();
        client.setClientID(1L);
        client.setKycStatus(KycStatus.APPROVED);

        InstrumentEntity instrument = new InstrumentEntity();
        instrument.setInstrumentID(1L);

        TradeEntity trade = new TradeEntity();
        trade.setTrade_id(id);
        trade.setCreatedBy(creatorId);
        trade.setStatus(status);
        trade.setClient_id(client);
        trade.setInstrument_id(instrument);
        trade.setQuantity(100);
        trade.setPrice(BigDecimal.valueOf(150.00));
        return trade;
    }
}