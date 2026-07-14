package com.mercury.mercury.Trade.service;

import com.mercury.mercury.Client.ClientEntity;
import com.mercury.mercury.Client.ClientRepo;
import com.mercury.mercury.Client.Enum.TradeStatus;
import com.mercury.mercury.Instruments.InstrumentEntity;
import com.mercury.mercury.Instruments.InstrumentRepo;
import com.mercury.mercury.Trade.entity.TradeEntity;
import com.mercury.mercury.Trade.specification.TradeSpecification;
import com.mercury.mercury.Trade.dto.TradeRequestDTO;
import com.mercury.mercury.Trade.dto.TradeResponseDTO;
import com.mercury.mercury.Trade.dto.TradeSearchRequest;
import com.mercury.mercury.Trade.dto.TradeUpdateRequestDTO;
import com.mercury.mercury.Trade.mapper.TradeMapper;
import com.mercury.mercury.Trade.repository.TradeRepo;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class TradeService {
    private final TradeRepo tradeRepo;
    private final ClientRepo clientRepo;
    private final InstrumentRepo instrumentRepo;
    private final TradeMapper tradeMapper;

    public TradeService(TradeRepo tradeRepo, ClientRepo clientRepo, InstrumentRepo instrumentRepo, TradeMapper tradeMapper){
        this.clientRepo = clientRepo;
        this.tradeRepo = tradeRepo;
        this.instrumentRepo = instrumentRepo;
        this.tradeMapper = tradeMapper;
    }

    @Transactional
    public TradeResponseDTO executeTrade(TradeRequestDTO requestDTO){

        log.info("Trade Creation Started");
        ClientEntity client;
        try {
            client = clientRepo.findById(requestDTO.getClientId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Validation Failed: Client not found with ID: " + requestDTO.getClientId()));
        }
        catch (EntityNotFoundException e){
            log.error("Client Not Found");
            log.error("trade Failed");
            throw e;
        }

        InstrumentEntity instrument;
        try {
            instrument = instrumentRepo.findById(requestDTO.getInstrumentId())
                    .orElseThrow(() -> new EntityNotFoundException("Validation Failed: instrument not Found with ID: " + requestDTO.getInstrumentId()));
        }
        catch (EntityNotFoundException e){
            log.error("Instrument not found");
            log.error("Trade Failed");
            throw e;
        }

        TradeEntity tradeEntity = tradeMapper.toEntity(requestDTO);

        tradeEntity.setClient_id(client);
        tradeEntity.setInstrument_id(instrument);
        tradeEntity.setStatus(TradeStatus.NEW);

        LocalDateTime now = LocalDateTime.now();
        tradeEntity.setTrade_date(now);
        tradeEntity.setSettled_date(now.plusDays(2));

        TradeEntity savedDate = tradeRepo.save(tradeEntity);

        log.info("Trade Created");
        return tradeMapper.toResponseDTO(savedDate);
    }

    public Page<TradeResponseDTO> getFilteredTrades(TradeSearchRequest request, Pageable pageable) {
        log.info("Trade Search start");
        request.validate();
        log.info(
                "Searching trades with filters: {}",
                request
        );
        Specification<TradeEntity> spec = TradeSpecification.getTradeByFilters(request);
        Page<TradeEntity> tradeEntities = tradeRepo.findAll(spec, pageable);
        log.info("Number of Records Returned {} ", tradeEntities.getNumberOfElements());
        Page<TradeResponseDTO> responsePage = tradeEntities.map(tradeMapper::toResponseDTO);
        log.info("Search completed");

        return responsePage;
    }

    @Transactional
    public TradeResponseDTO updateTrade(Long tradeId, TradeUpdateRequestDTO updateDTO){
        log.info("Trade Update Started for tradeId: {}", tradeId);
        TradeEntity trade = tradeRepo.findById(tradeId).orElseThrow(() -> new EntityNotFoundException("Trade not found with ID " +tradeId));
        log.info("Trade Found for tradeId: {}", tradeId);
        try {
            log.info("Simulating processing delay for tradeId: {}", tradeId);
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread interrupted during sleep for tradeId: {}", tradeId, e);
        }

        trade.setQuantity(updateDTO.getQuantity());
        trade.setPrice(updateDTO.getPrice());
        trade.setStatus(updateDTO.getStatus());

        TradeEntity updatedTrade = tradeRepo.save(trade);
        log.info("Trade Updated for tradeId: {}", tradeId);
        return tradeMapper.toResponseDTO(updatedTrade);
    }
}
