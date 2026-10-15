package com.osmosis.controller;

import com.osmosis.controller.dto.EngineResponse;
import com.osmosis.controller.dto.EventPayload;
import com.osmosis.service.BloomFilterService;
import com.osmosis.service.CountMinSketchService;
import com.osmosis.service.HyperLogLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller orchestrating high-throughput stream ingestion through
 * Bloom filter, Count-Min sketch, and HyperLogLog engines.
 */
@RestController
@RequestMapping("/api/v1/engine")
@RequiredArgsConstructor
public class IngestionController {

    private final BloomFilterService bloomFilterService;
    private final CountMinSketchService countMinSketchService;
    private final HyperLogLogService hyperLogLogService;

    @PostMapping("/process")
    public ResponseEntity<EngineResponse> process(@RequestBody EventPayload payload) {
        long startTime = System.nanoTime();

        String key = payload != null ? payload.getStreamKey() : null;

        boolean isDuplicate = bloomFilterService.isDuplicate(key);
        long frequency = countMinSketchService.incrementAndGetFrequency(key);
        long uniqueElements = hyperLogLogService.addAndGetUniqueCount(key);

        double processingTimeMs = (System.nanoTime() - startTime) / 1_000_000.0;

        EngineResponse response = EngineResponse.builder()
                .duplicate(isDuplicate)
                .frequency(frequency)
                .globalUniqueElements(uniqueElements)
                .totalProcessingTimeMs(processingTimeMs)
                .build();

        return ResponseEntity.ok(response);
    }
}
