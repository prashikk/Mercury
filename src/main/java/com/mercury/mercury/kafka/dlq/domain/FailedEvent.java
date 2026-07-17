package com.mercury.mercury.kafka.dlq.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "failed_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FailedEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long failedId;
    private String eventKey;
    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String payload;
    @Column(columnDefinition = "TEXT")
    private String failureReason;

    private LocalDateTime failedAt;
    private Integer retryAttempts;
    private String status;
}
