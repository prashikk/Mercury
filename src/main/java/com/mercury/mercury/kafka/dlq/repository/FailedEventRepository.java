package com.mercury.mercury.kafka.dlq.repository;

import com.mercury.mercury.kafka.dlq.domain.FailedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FailedEventRepository extends JpaRepository<FailedEvent, Long> {
}
