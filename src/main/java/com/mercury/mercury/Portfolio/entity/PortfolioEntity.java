package com.mercury.mercury.Portfolio.entity;

import com.mercury.mercury.Client.ClientEntity;
import com.mercury.mercury.Instruments.InstrumentEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Portfolio")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_id")
    private Long portfolioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity clientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id", nullable = false)
    private InstrumentEntity instrumentId;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Column(name = "average_buy_price",precision = 18, scale = 4, nullable = false)
    private BigDecimal averageBuyPrice = BigDecimal.ZERO;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @Version
    @Column(name = "version")
    private Long version;

}
