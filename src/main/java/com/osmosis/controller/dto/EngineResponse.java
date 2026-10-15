package com.osmosis.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unified response payload summarizing deduplication, frequency, and cardinality metrics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EngineResponse {
    private boolean duplicate;
    private long frequency;
    private long globalUniqueElements;
    private double totalProcessingTimeMs;
}
