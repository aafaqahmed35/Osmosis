package com.osmosis.service;

import com.osmosis.util.HashUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CountMinSketchServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @InjectMocks
    private CountMinSketchService countMinSketchService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @Test
    @DisplayName("Should increment all 5 sketch hashes and return minimum frequency")
    void shouldIncrementAndReturnMinimumFrequency() {
        String payload = "osmosis-transaction-101";
        long[] indices = HashUtil.getHashIndices(payload, CountMinSketchService.DEPTH, CountMinSketchService.WIDTH);

        long[] mockedFrequencies = {10L, 7L, 15L, 8L, 12L};
        for (int i = 0; i < indices.length; i++) {
            String field = i + ":" + indices[i];
            when(hashOperations.increment(CountMinSketchService.CMS_KEY, field, 1L))
                    .thenReturn(mockedFrequencies[i]);
        }

        long minFrequency = countMinSketchService.incrementAndGetFrequency(payload);

        assertThat(minFrequency).isEqualTo(7L);

        for (int i = 0; i < indices.length; i++) {
            String field = i + ":" + indices[i];
            verify(hashOperations).increment(CountMinSketchService.CMS_KEY, field, 1L);
        }
        verify(hashOperations, times(5)).increment(eq(CountMinSketchService.CMS_KEY), any(), eq(1L));
    }

    @Test
    @DisplayName("Should return 1 on first occurrence when all counters are 1")
    void shouldReturnOneOnFirstOccurrence() {
        String payload = "osmosis-new-record";
        long[] indices = HashUtil.getHashIndices(payload, CountMinSketchService.DEPTH, CountMinSketchService.WIDTH);

        for (int i = 0; i < indices.length; i++) {
            String field = i + ":" + indices[i];
            when(hashOperations.increment(CountMinSketchService.CMS_KEY, field, 1L))
                    .thenReturn(1L);
        }

        long minFrequency = countMinSketchService.incrementAndGetFrequency(payload);

        assertThat(minFrequency).isEqualTo(1L);

        for (int i = 0; i < indices.length; i++) {
            String field = i + ":" + indices[i];
            verify(hashOperations).increment(CountMinSketchService.CMS_KEY, field, 1L);
        }
    }
}
