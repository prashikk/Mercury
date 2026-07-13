package com.mercury.mercury.Client;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.naming.Name;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Entity
@Table(name = "Client")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_Id")
    private Long clientID;

    @Column(name = "client_Name", nullable = false)
    private String clientName;

    @Column(name = "client_Email", unique = true)
    private String clientEmail;

    @Column(name = "country")
    private String country;

    @Column(name = "KYC_status")
    private String KYC_Status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @CreationTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


}
