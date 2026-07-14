package com.mercury.mercury.Trade.service;

import com.mercury.mercury.Client.Enum.TradeStatus;
import com.mercury.mercury.Trade.dto.TradeResponseDTO;
import com.mercury.mercury.Trade.entity.TradeEntity;
import com.mercury.mercury.Trade.mapper.TradeMapper;
import com.mercury.mercury.Trade.repository.TradeRepo;
import com.mercury.mercury.Trade.state.TradeStateValidator;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TradeLifecycleService {
    public final TradeRepo tradeRepo;
    public final TradeStateValidator tradeStateValidator;
    public final TradeMapper tradeMapper;


    public TradeLifecycleService(TradeRepo tradeRepo, TradeStateValidator tradeStateValidator, TradeMapper tradeMapper) {
        this.tradeRepo = tradeRepo;
        this.tradeStateValidator = tradeStateValidator;
        this.tradeMapper = tradeMapper;
    }

    @Transactional
    public TradeResponseDTO TransationStatus(Long Id, TradeStatus newStatus){
        TradeEntity tradeEntity = tradeRepo.findById(Id).orElseThrow(() -> new RuntimeException("Trade not found with ID: " + Id));

        tradeStateValidator.validateTransition(tradeEntity.getStatus(), newStatus);
        tradeEntity.setStatus(newStatus);

        TradeEntity updatedEntity = tradeRepo.save(tradeEntity);
        return tradeMapper.toResponseDTO(updatedEntity);
    }
}
