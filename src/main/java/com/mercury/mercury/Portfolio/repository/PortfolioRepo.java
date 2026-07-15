package com.mercury.mercury.Portfolio.repository;

import com.mercury.mercury.Portfolio.entity.PortfolioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PortfolioRepo extends JpaRepository<PortfolioEntity, Long> {

    Optional<PortfolioEntity> findByClientId_ClientIDAndInstrumentId_InstrumentID(Long clientId, Long instrumentId);
}

