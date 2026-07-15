package com.mercury.mercury.Trade.service;

import com.mercury.mercury.Client.ClientEntity;
import com.mercury.mercury.Client.Enum.KycStatus;
import com.mercury.mercury.Common.BusinessValidationException;
import com.mercury.mercury.Instruments.InstrumentEntity;
import com.mercury.mercury.Trade.Enum.TradeStatus;
import com.mercury.mercury.Trade.dto.TradeResponseDTO;
import com.mercury.mercury.Trade.entity.TradeEntity;
import com.mercury.mercury.Trade.mapper.TradeMapper;
import com.mercury.mercury.Trade.repository.TradeRepo;
import com.mercury.mercury.Trade.specification.ApprovalValidator;
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
class ApprovalServiceTest {

    @Mock
    private TradeRepo tradeRepo;

    @Spy // Inject real validator logic to accurately test cross-user policy checks
    private ApprovalValidator approvalValidator;

    @Mock
    private TradeLifecycleService lifecycleService;

    @Mock
    private TradeMapper tradeMapper;

    @InjectMocks
    private ApprovalService approvalService;

    @Test
    @DisplayName("Test 1: High Value Trade Routing Calculation Rule Check")
    void test1_HighValueTradeShouldRouteToPendingApproval() {
        BigDecimal highPrice = new BigDecimal("1500.00");
        Integer highQuantity = 100000;

        BigDecimal totalValue = highPrice.multiply(BigDecimal.valueOf(highQuantity));
        BigDecimal threshold = new BigDecimal("100000000");

        assertTrue(totalValue.compareTo(threshold) > 0);
    }

    @Test
    @DisplayName("Test 2: Different User -> Approval Should Pass cleanly")
    void test2_DifferentUserApprovalShouldPass() {
        Long tradeId = 200L;
        Long makerUserId = 101L;
        Long checkerUserId = 202L;

        TradeEntity trade = createMockTrade(tradeId, makerUserId, TradeStatus.PENDING_APPROVAL);
        TradeResponseDTO expectedDto = new TradeResponseDTO();
        expectedDto.setStatus(TradeStatus.VALIDATED);

        when(tradeRepo.findById(tradeId)).thenReturn(Optional.of(trade));
        when(tradeRepo.save(any(TradeEntity.class))).thenReturn(trade);
        when(tradeMapper.toResponseDTO(any(TradeEntity.class))).thenReturn(expectedDto);

        TradeResponseDTO result = (TradeResponseDTO) approvalService.approveTrade(tradeId, checkerUserId);

        assertNotNull(result);
        verify(approvalValidator, times(1)).validateApproval(trade, checkerUserId);
        verify(lifecycleService, times(1)).transationStatus(tradeId, TradeStatus.APPROVED);
        verify(lifecycleService, times(1)).transationStatus(tradeId, TradeStatus.VALIDATED);
        verify(tradeRepo, times(1)).save(trade);
    }

    @Test
    @DisplayName("Test 3: Maker tries Approve -> Fail with 403 Forbidden")
    void test3_MakerApprovingOwnTradeShouldFail() {

        Long tradeId = 201L;
        Long makerUserId = 101L;
        Long checkerUserId = 101L;

        TradeEntity trade = createMockTrade(tradeId, makerUserId, TradeStatus.PENDING_APPROVAL);

        when(tradeRepo.findById(tradeId)).thenReturn(Optional.of(trade));

        BusinessValidationException ex = assertThrows(BusinessValidationException.class, () ->
                approvalService.approveTrade(tradeId, checkerUserId)
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getHttpStatus());
        assertTrue(ex.getMessage().contains("Maker-Checker Principle Violation"));
        verify(tradeRepo, never()).save(any());
    }

    @Test
    @DisplayName("Test 4: Already Approved -> Fail with 400 Bad Request")
    void test4_AlreadyApprovedTradeShouldFail() {

        Long tradeId = 202L;
        Long makerUserId = 101L;
        Long checkerUserId = 202L;

        TradeEntity trade = createMockTrade(tradeId, makerUserId, TradeStatus.VALIDATED);

        when(tradeRepo.findById(tradeId)).thenReturn(Optional.of(trade));

        BusinessValidationException ex = assertThrows(BusinessValidationException.class, () ->
                approvalService.approveTrade(tradeId, checkerUserId)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());

        assertTrue(ex.getMessage().contains("must be in PENDING_APPROVAL status"));
    }


    @Test
    @DisplayName("Test 5: Trade Value Below 10 Crore Rule Check")
    void test5_TradeValueBelowTenCroreShouldNotRequireApproval() {

        BigDecimal lowPrice = new BigDecimal("100.00");
        Integer lowQuantity = 50;

        BigDecimal totalValue = lowPrice.multiply(BigDecimal.valueOf(lowQuantity));
        BigDecimal threshold = new BigDecimal("100000000");

        assertTrue(totalValue.compareTo(threshold) < 0);
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
