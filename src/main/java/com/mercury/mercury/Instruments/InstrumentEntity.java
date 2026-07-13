package com.mercury.mercury.Instruments;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
@Table(name = "Instrument")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "instrument_Id")
    private Long instrumentID;

    @Column(name = "ticker")
    private String ticker;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "exchange")
    private String exchange;

    @Column(name = "currency")
    private String currency;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime created_at;

    @CreationTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updated_at;
}
