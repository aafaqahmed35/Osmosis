package com.osmosis.service;

import com.osmosis.util.HashUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BloomFilterServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private BloomFilterService bloomFilterService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Should return true when all bits are already 1 (duplicate payload)")
    void shouldReturnTrueWhenAllBitsAreSet() {
        String payload = "osmosis-test-event";
        long[] indices = HashUtil.getHashIndices(payload, BloomFilterService.NUM_HASHES, BloomFilterService.BIT_ARRAY_SIZE);

        for (long index : indices) {
            when(valueOperations.getBit(BloomFilterService.BLOOM_FILTER_KEY, index)).thenReturn(true);
        }

        boolean isDuplicate = bloomFilterService.isDuplicate(payload);

        assertThat(isDuplicate).isTrue();
        verify(valueOperations, never()).setBit(eq(BloomFilterService.BLOOM_FILTER_KEY), anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("Should return false and set all bits when at least one bit is 0 (new payload)")
    void shouldReturnFalseAndSetBitsWhenBitIsZero() {
        String payload = "osmosis-new-event";
        long[] indices = HashUtil.getHashIndices(payload, BloomFilterService.NUM_HASHES, BloomFilterService.BIT_ARRAY_SIZE);

        when(valueOperations.getBit(eq(BloomFilterService.BLOOM_FILTER_KEY), anyLong())).thenReturn(false);

        boolean isDuplicate = bloomFilterService.isDuplicate(payload);

        assertThat(isDuplicate).isFalse();

        for (long index : indices) {
            verify(valueOperations).setBit(BloomFilterService.BLOOM_FILTER_KEY, index, true);
        }
    }

    @Test
    @DisplayName("Should return false and set all bits when bit returns null")
    void shouldReturnFalseWhenBitIsNull() {
        String payload = "osmosis-null-bit-event";
        long[] indices = HashUtil.getHashIndices(payload, BloomFilterService.NUM_HASHES, BloomFilterService.BIT_ARRAY_SIZE);

        when(valueOperations.getBit(eq(BloomFilterService.BLOOM_FILTER_KEY), anyLong())).thenReturn(null);

        boolean isDuplicate = bloomFilterService.isDuplicate(payload);

        assertThat(isDuplicate).isFalse();

        for (long index : indices) {
            verify(valueOperations).setBit(BloomFilterService.BLOOM_FILTER_KEY, index, true);
        }
    }
}
