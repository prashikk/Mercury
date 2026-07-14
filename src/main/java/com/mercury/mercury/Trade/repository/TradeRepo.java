package com.mercury.mercury.Trade.repository;

import com.mercury.mercury.Trade.entity.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeRepo extends JpaRepository<TradeEntity, Long>, JpaSpecificationExecutor<TradeEntity> {

}
