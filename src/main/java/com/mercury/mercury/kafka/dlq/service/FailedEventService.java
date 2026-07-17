package com.mercury.mercury.kafka.dlq.service;

import com.mercury.mercury.kafka.dlq.domain.FailedEvent;
import com.mercury.mercury.kafka.dlq.repository.FailedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FailedEventService {
    private final FailedEventRepository failedEventRepository;

    public Page<FailedEvent> getFailedEvents(Pageable pageable){
        return failedEventRepository.findAll(pageable);
    }
}
