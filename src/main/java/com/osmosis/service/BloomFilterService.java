package com.osmosis.service;

import com.osmosis.util.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Elementary Bloom Filter service backed by Redis bitmap.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BloomFilterService {

    public static final String BLOOM_FILTER_KEY = "osmosis:bloom";
    public static final long BIT_ARRAY_SIZE = 10_000_000L;
    public static final int NUM_HASHES = 5;

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Checks if a payload has already been seen using Redis bitmap as a Bloom filter.
     * If not present, all calculated bit indices are set to 1.
     *
     * @param payload the data string to check for duplication
     * @return true if the payload was already present, false otherwise
     */
    public boolean isDuplicate(String payload) {
        long startTime = System.nanoTime();

        long[] indices = HashUtil.getHashIndices(payload, NUM_HASHES, BIT_ARRAY_SIZE);

        boolean allBitsSet = true;
        for (long index : indices) {
            Boolean bit = redisTemplate.opsForValue().getBit(BLOOM_FILTER_KEY, index);
            if (bit == null || !bit) {
                allBitsSet = false;
                break;
            }
        }

        if (!allBitsSet) {
            for (long index : indices) {
                redisTemplate.opsForValue().setBit(BLOOM_FILTER_KEY, index, true);
            }
        }

        double latencyMs = (System.nanoTime() - startTime) / 1_000_000.0;
        System.out.println("Bloom filter check latency: " + latencyMs + " ms");
        log.debug("Payload check latency: {} ms, isDuplicate: {}", latencyMs, allBitsSet);

        return allBitsSet;
    }
}
