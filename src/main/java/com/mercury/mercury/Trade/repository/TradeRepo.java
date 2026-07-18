package com.mercury.mercury.Trade.repository;

import com.mercury.mercury.Trade.Enum.TradeStatus;
import com.mercury.mercury.Trade.entity.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TradeRepo extends JpaRepository<TradeEntity, Long>, JpaSpecificationExecutor<TradeEntity> {
    List<TradeEntity> findByStatus(TradeStatus status);
    long countByStatus(TradeStatus status);

    @Query("SELECT SUM(t.quantity * t.price) FROM TradeEntity t WHERE t.status = 'SETTLED' AND t.isDeleted = false")
    BigDecimal calculateTotalTradeValue();

    @Query("SELECT t.client_id.clientName, COUNT(t), SUM(t.quantity * t.price) " +
            "FROM TradeEntity t " +
            "WHERE t.status = 'SETTLED' AND t.isDeleted = false " +
            "GROUP BY t.client_id.clientName " +
            "ORDER BY SUM(t.quantity * t.price) DESC")
    List<Object[]> findTopClientsAggregated();

    @Query("SELECT t.instrument_id.ticker, COUNT(t), SUM(t.quantity * t.price) " +
            "FROM TradeEntity t " +
            "WHERE t.status = 'SETTLED' AND t.isDeleted = false " +
            "GROUP BY t.instrument_id.ticker " +
            "ORDER BY SUM(t.quantity * t.price) DESC")
    List<Object[]> findTopInstrumentsAggregated();

    @Query("SELECT COUNT(t), MIN(t.settled_at), MAX(t.settled_at) FROM TradeEntity t WHERE t.status = 'SETTLED'")
    List<Object[]> getRawSettlementMetrics();
}
