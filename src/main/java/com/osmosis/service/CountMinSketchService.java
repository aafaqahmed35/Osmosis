package com.osmosis.service;

import com.osmosis.util.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Distributed Count-Min Sketch service backed by Redis Hashes for frequency estimation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CountMinSketchService {

    public static final String CMS_KEY = "osmosis:cms";
    public static final int WIDTH = 100_000;
    public static final int DEPTH = 5;

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Increments the frequency counters for the payload across all sketch rows
     * and returns the conservative frequency estimate (the minimum across all rows).
     *
     * @param payload the element payload to register
     * @return conservative minimum frequency estimate
     */
    public long incrementAndGetFrequency(String payload) {
        long startTime = System.nanoTime();

        long[] indices = HashUtil.getHashIndices(payload, DEPTH, WIDTH);
        long minFrequency = Long.MAX_VALUE;

        for (int i = 0; i < indices.length; i++) {
            String field = i + ":" + indices[i];
            Long newVal = redisTemplate.opsForHash().increment(CMS_KEY, field, 1);
            if (newVal != null) {
                minFrequency = Math.min(minFrequency, newVal);
            }
        }

        long result = minFrequency == Long.MAX_VALUE ? 0L : minFrequency;

        double latencyMs = (System.nanoTime() - startTime) / 1_000_000.0;
        System.out.println("Count-Min Sketch latency: " + latencyMs + " ms");
        log.debug("CMS increment latency: {} ms, frequency: {}", latencyMs, result);

        return result;
    }
}
