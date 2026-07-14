package com.mercury.mercury.Trade.mapper;

import com.mercury.mercury.Trade.entity.TradeEntity;
import com.mercury.mercury.Trade.dto.TradeRequestDTO;
import com.mercury.mercury.Trade.dto.TradeResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class TradeMapper {
    public TradeEntity toEntity(TradeRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        TradeEntity entity = new TradeEntity();

        entity.setTrade_type(dto.getTradeType());
        entity.setQuantity(dto.getQuantity());
        entity.setPrice(dto.getPrice());

        return entity;
    }

    public TradeResponseDTO toResponseDTO(TradeEntity entity) {
        if (entity == null) {
            return null;
        }

        TradeResponseDTO dto = new TradeResponseDTO();

        dto.setTradeId(entity.getTrade_id());
        dto.setTradeType(entity.getTrade_type());
        dto.setQuantity(entity.getQuantity());
        dto.setPrice(entity.getPrice());
        dto.setTradeDate(entity.getTrade_date());
        dto.setSettledDate(entity.getSettled_date());
        dto.setStatus(entity.getStatus());
        dto.setCreatedTime(entity.getCreatedTime());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getClient_id() != null) {
            dto.setClientId(entity.getClient_id().getClientID());
            dto.setClientName(entity.getClient_id().getClientName());
        }

        if (entity.getInstrument_id() != null) {
            dto.setInstrumentId(entity.getInstrument_id().getInstrumentID());
            dto.setTicker(entity.getInstrument_id().getTicker());
        }

        return dto;
    }
}
