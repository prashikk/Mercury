package com.mercury.mercury.kafka.dlq.controller;

import com.mercury.mercury.kafka.dlq.domain.FailedEvent;
import com.mercury.mercury.kafka.dlq.service.FailedEventService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/failed-events")
@RequiredArgsConstructor
public class FailedEventController {
    private final FailedEventService failedEventService;

    @GetMapping
    public ResponseEntity<Page<FailedEvent>> getAllFailedEvents(@PageableDefault(size = 20)Pageable pageable){
        return ResponseEntity.ok(failedEventService.getFailedEvents(pageable));
    }
}
