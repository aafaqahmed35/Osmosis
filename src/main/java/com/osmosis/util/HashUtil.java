package com.osmosis.util;

import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;

/**
 * Utility class providing hashing functions for Bloom filter indexing.
 */
public final class HashUtil {

    private HashUtil() {
        // Prevent instantiation
    }

    /**
     * Generates an array of hash indices for a given payload using MurmurHash3 (128-bit).
     *
     * @param payload      the data to hash
     * @param numHashes    number of independent hash indices to generate
     * @param bitArraySize size of the underlying bit array
     * @return array of positive bit indices within [0, bitArraySize)
     */
    public static long[] getHashIndices(String payload, int numHashes, long bitArraySize) {
        if (payload == null) {
            throw new IllegalArgumentException("Payload cannot be null");
        }
        if (numHashes <= 0) {
            throw new IllegalArgumentException("Number of hashes must be greater than zero");
        }
        if (bitArraySize <= 0) {
            throw new IllegalArgumentException("Bit array size must be greater than zero");
        }

        long[] indices = new long[numHashes];
        for (int i = 0; i < numHashes; i++) {
            long rawHash = Hashing.murmur3_128()
                    .hashString(payload + i, StandardCharsets.UTF_8)
                    .asLong();
            long positiveHash = rawHash == Long.MIN_VALUE ? 0 : Math.abs(rawHash);
            indices[i] = positiveHash % bitArraySize;
        }
        return indices;
    }
}
