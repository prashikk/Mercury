package com.mercury.mercury.Trade;

import com.mercury.mercury.Client.ClientEntity;
import com.mercury.mercury.Client.ClientRepo;
import com.mercury.mercury.Client.Enum.TradeStatus;
import com.mercury.mercury.Instruments.InstrumentEntity;
import com.mercury.mercury.Instruments.InstrumentRepo;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
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
}
