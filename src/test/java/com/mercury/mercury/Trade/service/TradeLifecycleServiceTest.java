package com.mercury.mercury.Trade.service;

import com.mercury.mercury.Trade.Enum.TradeStatus;
import com.mercury.mercury.Trade.dto.TradeResponseDTO;
import com.mercury.mercury.Trade.entity.TradeEntity;
import com.mercury.mercury.Trade.mapper.TradeMapper;
import com.mercury.mercury.Trade.repository.TradeRepo;
import com.mercury.mercury.Trade.state.TradeStateValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Enables Mockito annotation support engine
class TradeLifecycleServiceTest {

    @Mock
    private TradeRepo tradeRepo;

    @Mock
    private TradeStateValidator stateValidator;

    @Mock
    private TradeMapper tradeMapper;

    @InjectMocks
    private TradeLifecycleService lifecycleService; // Automatically injects the mocks above

    @Test
    @DisplayName("Should successfully transition state and save trade")
    void testSuccessfulTransitionWorkflow() {
        // Arrange
        Long tradeId = 97L;
        TradeEntity sampleTrade = new TradeEntity();
        sampleTrade.setTrade_id(tradeId);
        sampleTrade.setStatus(TradeStatus.NEW);

        TradeResponseDTO mockResponse = new TradeResponseDTO();
        mockResponse.setTradeId(tradeId);
        mockResponse.setStatus(TradeStatus.VALIDATED);

        // Stubbing database behavior rules
        when(tradeRepo.findById(tradeId)).thenReturn(Optional.of(sampleTrade));
        when(tradeRepo.save(any(TradeEntity.class))).thenReturn(sampleTrade);
        when(tradeMapper.toResponseDTO(any(TradeEntity.class))).thenReturn(mockResponse);

        // Act
        TradeResponseDTO result = lifecycleService.transationStatus(tradeId, TradeStatus.VALIDATED);

        // Assert
        assertNotNull(result);
        assertEquals(TradeStatus.VALIDATED, result.getStatus());

        // Verify that collaborators were interacted with exactly as expected
        verify(stateValidator, times(1)).validateTransition(TradeStatus.NEW, TradeStatus.VALIDATED);
        verify(tradeRepo, times(1)).save(sampleTrade);
    }

    @Test
    @DisplayName("Should throw exception if the requested trade does not exist")
    void testTransitionShouldFailWhenTradeNotFound() {
        // Arrange
        Long invalidId = 999L;
        when(tradeRepo.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                lifecycleService.transationStatus(invalidId, TradeStatus.VALIDATED)
        );

        // Verify that the save operation was aborted completely to protect the system state
        verify(tradeRepo, never()).save(any());
    }
}
