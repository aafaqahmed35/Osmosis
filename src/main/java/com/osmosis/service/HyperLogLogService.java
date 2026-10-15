package com.osmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Distributed HyperLogLog service backed by Redis native HyperLogLog commands
 * for unique element cardinality estimation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HyperLogLogService {

    public static final String HLL_KEY = "osmosis:hll";

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Adds the payload to the HyperLogLog register and returns the estimated unique count.
     *
     * @param payload element payload to register
     * @return estimated total distinct cardinality
     */
    public long addAndGetUniqueCount(String payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Payload cannot be null");
        }

        long startTime = System.nanoTime();

        redisTemplate.opsForHyperLogLog().add(HLL_KEY, payload);
        Long size = redisTemplate.opsForHyperLogLog().size(HLL_KEY);
        long estimatedCount = size != null ? size : 0L;

        double latencyMs = (System.nanoTime() - startTime) / 1_000_000.0;
        System.out.println("HyperLogLog latency: " + latencyMs + " ms");
        log.debug("HyperLogLog add latency: {} ms, estimated unique count: {}", latencyMs, estimatedCount);

        return estimatedCount;
    }
}
