package com.osmosis.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HashUtilTest {

    @Test
    @DisplayName("Should generate specified number of positive hash indices within range")
    void shouldGenerateValidIndices() {
        String payload = "payload-12345";
        int numHashes = 5;
        long bitArraySize = 10_000_000L;

        long[] indices = HashUtil.getHashIndices(payload, numHashes, bitArraySize);

        assertThat(indices).hasSize(numHashes);
        for (long index : indices) {
            assertThat(index).isNotNegative().isLessThan(bitArraySize);
        }
    }

    @Test
    @DisplayName("Should be deterministic for the same payload")
    void shouldBeDeterministic() {
        String payload = "reproducible-payload";
        long[] firstRun = HashUtil.getHashIndices(payload, 5, 10_000_000L);
        long[] secondRun = HashUtil.getHashIndices(payload, 5, 10_000_000L);

        assertThat(firstRun).containsExactly(secondRun);
    }

    @Test
    @DisplayName("Should throw exception on invalid arguments")
    void shouldThrowOnInvalidArgs() {
        assertThatThrownBy(() -> HashUtil.getHashIndices(null, 5, 10_000_000L))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> HashUtil.getHashIndices("test", 0, 10_000_000L))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> HashUtil.getHashIndices("test", 5, 0L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
