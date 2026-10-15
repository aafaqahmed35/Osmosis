package com.osmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HyperLogLogOperations;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HyperLogLogServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private HyperLogLogOperations<String, Object> hyperLogLogOperations;

    @InjectMocks
    private HyperLogLogService hyperLogLogService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForHyperLogLog()).thenReturn(hyperLogLogOperations);
    }

    @Test
    @DisplayName("Should add payload to HyperLogLog and return estimated unique count")
    void shouldAddAndReturnEstimatedUniqueCount() {
        String payload = "osmosis-user-999";
        when(hyperLogLogOperations.add(HyperLogLogService.HLL_KEY, payload)).thenReturn(1L);
        when(hyperLogLogOperations.size(HyperLogLogService.HLL_KEY)).thenReturn(42L);

        long count = hyperLogLogService.addAndGetUniqueCount(payload);

        assertThat(count).isEqualTo(42L);
        verify(hyperLogLogOperations).add(HyperLogLogService.HLL_KEY, payload);
        verify(hyperLogLogOperations).size(HyperLogLogService.HLL_KEY);
    }

    @Test
    @DisplayName("Should return 0 when size returns null")
    void shouldHandleNullSizeSafely() {
        String payload = "osmosis-user-1000";
        when(hyperLogLogOperations.add(HyperLogLogService.HLL_KEY, payload)).thenReturn(1L);
        when(hyperLogLogOperations.size(HyperLogLogService.HLL_KEY)).thenReturn(null);

        long count = hyperLogLogService.addAndGetUniqueCount(payload);

        assertThat(count).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when payload is null")
    void shouldThrowWhenPayloadIsNull() {
        assertThatThrownBy(() -> hyperLogLogService.addAndGetUniqueCount(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
