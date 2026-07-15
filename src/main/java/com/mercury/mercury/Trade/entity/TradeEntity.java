package com.mercury.mercury.Trade.entity;

import com.mercury.mercury.Client.ClientEntity;
import com.mercury.mercury.Trade.Enum.TradeStatus;
import com.mercury.mercury.Trade.Enum.TradeType;
import com.mercury.mercury.Instruments.InstrumentEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Trade")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trade_Id")
    private Long trade_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_Id", nullable = false)
    private ClientEntity client_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_Id", nullable = false)
    private InstrumentEntity instrument_id;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_Type", nullable = false)
    private TradeType trade_type;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "trade_Date")
    private LocalDateTime trade_date;

    @Column(name = "settled_Date")
    private LocalDateTime settled_date;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TradeStatus status;

    @Column(name = "created_By")
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_Time", updatable = false)
    private LocalDateTime createdTime;

    @Column(name = "is_Deleted")
    private Boolean isDeleted = false;

    @Column(name = "deleted_At")
    private LocalDateTime deletedAt;

    @Column(name = "is_Deleted_By")
    private Long isDeletedBy;

    @UpdateTimestamp
    @Column(name = "updated_At")
    private LocalDateTime updatedAt;

    @Column(name = "updated_By")
    private Long updatedBy;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "settlement_Reference")
    private String settlementReference;

    @Column(name = "settled_By")
    private Long settled_by;

    @Column(name = "settled_At")
    private LocalDateTime settled_at;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
}
