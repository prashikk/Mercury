package com.mercury.mercury.Trade.service;

import com.mercury.mercury.Client.ClientEntity;
import com.mercury.mercury.Client.ClientRepo;
import com.mercury.mercury.Trade.Enum.TradeStatus;
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
import com.mercury.mercury.User.service.AuthenticatedUserService;
import com.mercury.mercury.event.TradeCreatedEvent;
import com.mercury.mercury.event.publisher.TradeEventPublisher;
import com.mercury.mercury.monitoring.TradeMetricsService;
import com.mercury.mercury.notification.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
public class TradeService {
    private final TradeRepo tradeRepo;
    private final ClientRepo clientRepo;
    private final InstrumentRepo instrumentRepo;
    private final TradeMapper tradeMapper;
    private final AuthenticatedUserService authenticatedUserService;
    private final TradeEventPublisher tradeEventPublisher;
    private final TradeMetricsService tradeMetricsService;

    public TradeService(TradeRepo tradeRepo, ClientRepo clientRepo, InstrumentRepo instrumentRepo, TradeMapper tradeMapper, AuthenticatedUserService authenticatedUserService, TradeEventPublisher tradeEventPublisher, TradeMetricsService tradeMetricsService){
        this.clientRepo = clientRepo;
        this.tradeRepo = tradeRepo;
        this.instrumentRepo = instrumentRepo;
        this.tradeMapper = tradeMapper;
        this.authenticatedUserService = authenticatedUserService;
        this.tradeEventPublisher = tradeEventPublisher;
        this.tradeMetricsService = tradeMetricsService;
    }

    @Transactional
    public TradeResponseDTO executeTrade(TradeRequestDTO requestDTO){
        tradeMetricsService.incrementCreated();
        String currentActor = authenticatedUserService.getCurrentUsername();
        Long actorId = authenticatedUserService.getCurrentUserId();

        log.info("User '{}' initiated trade creation pipeline execution.", currentActor);

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
        tradeEntity.setCreatedBy(actorId);
        tradeEntity.setStatus(TradeStatus.NEW);
        LocalDateTime now = LocalDateTime.now();
        tradeEntity.setTrade_date(now);
        tradeEntity.setSettled_date(now.plusDays(2));

        BigDecimal totalValue = requestDTO.getPrice().multiply(BigDecimal.valueOf(requestDTO.getQuantity()));
        BigDecimal threshold = BigDecimal.valueOf(100000000); //10cr is the limit

        if(totalValue.compareTo(threshold) > 0){
            tradeEntity.setStatus(TradeStatus.PENDING_APPROVAL);
            log.info("High value trade calculated: {} > threshold {}. Routing trade status to PENDING_APPROVAL.", totalValue, threshold);        }
        else{
            tradeEntity.setStatus(TradeStatus.VALIDATED);
            log.info("Trade value calculated: {} <= threshold {}. Automatically promoting status to VALIDATED.", totalValue, threshold);
        }


        TradeEntity savedTrade = tradeRepo.save(tradeEntity);

        log.info("Publishing TradeCreatedEvent | Trade ID {}", savedTrade.getTrade_id());

        tradeEventPublisher.publishTradeCreated(
                new TradeCreatedEvent(savedTrade.getTrade_id(), actorId, java.time.LocalDateTime.now())
        );
        return tradeMapper.toResponseDTO(savedTrade);
    }

    public Page<TradeResponseDTO> getFilteredTrades(TradeSearchRequest request, Pageable pageable) {
        log.info("Filter queries triggered by User '{}'", authenticatedUserService.getCurrentUsername());
        request.validate();
        Specification<TradeEntity> spec = TradeSpecification.getTradeByFilters(request);
        Page<TradeEntity> tradeEntities = tradeRepo.findAll(spec, pageable);
        return tradeEntities.map(tradeMapper::toResponseDTO);
    }

    @Transactional
    public TradeResponseDTO updateTrade(Long tradeId, TradeUpdateRequestDTO updateDTO){
        String currentActor = authenticatedUserService.getCurrentUsername();
        Long actorId = authenticatedUserService.getCurrentUserId();

        log.info("User '{}' started update transaction on Trade ID: {}", currentActor, tradeId);

        TradeEntity trade = tradeRepo.findById(tradeId).orElseThrow(() -> new EntityNotFoundException("Trade not found with ID " +tradeId));

        trade.setQuantity(updateDTO.getQuantity());
        trade.setPrice(updateDTO.getPrice());
        trade.setStatus(updateDTO.getStatus());

        TradeEntity updatedTrade = tradeRepo.save(trade);
        log.info("User '{}' successfully committed updates to Trade ID: {}", currentActor, tradeId);
        return tradeMapper.toResponseDTO(updatedTrade);
    }
}
