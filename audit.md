# Execution Audit Log - Phase 2: Bloom Filter Engine Implementation

**Execution Step:** Phase 2 - Bloom Filter Engine Implementation  
**Timestamp:** 2026-10-15  
**Project:** Osmosis (Distributed Deduplication Engine)  

---

## Overview

This audit document details the files created, modified, and verified exclusively during **Phase 2**. In this phase, an elementary, whiteboard-friendly distributed Bloom filter engine was implemented using Google Guava's 128-bit MurmurHash3 and Redis bitmap operations via `RedisTemplate`.

---

## Manifest of Modified and Created Files

### 1. Build Configuration
- **`pom.xml`** *(Modified)*:
  - Added Google Guava dependency:
    - GroupId: `com.google.guava`
    - ArtifactId: `guava`
    - Version: `33.0.0-jre`
  - Enables `Hashing.murmur3_128()` for generating uniform, low-collision hash values for Bloom filter bit positions.

### 2. Utility Layer
- **`src/main/java/com/osmosis/util/HashUtil.java`** *(Created)*:
  - Package: `com.osmosis.util`
  - Implements `public static long[] getHashIndices(String payload, int numHashes, long bitArraySize)`.
  - Uses Guava's `Hashing.murmur3_128().hashString(payload + i, StandardCharsets.UTF_8).asLong()` to generate independent hash values for each index `i`.
  - Normalizes raw hash values using `Math.abs()` and modulo `bitArraySize` to guarantee strictly non-negative indices within `[0, bitArraySize)`.
  - Includes defensive validations for null payloads, non-positive hash counts, and non-positive bit array sizes.

### 3. Service Layer
- **`src/main/java/com/osmosis/service/BloomFilterService.java`** *(Created)*:
  - Package: `com.osmosis.service`
  - Injects `RedisTemplate<String, Object>` via Lombok's `@RequiredArgsConstructor`.
  - Defines core constants:
    - `BLOOM_FILTER_KEY = "osmosis:bloom"`
    - `BIT_ARRAY_SIZE = 10000000L` (10 million bits)
    - `NUM_HASHES = 5`
  - Implements `public boolean isDuplicate(String payload)`:
    - Records start time with `System.nanoTime()`.
    - Computes hash indices via `HashUtil.getHashIndices(payload, NUM_HASHES, BIT_ARRAY_SIZE)`.
    - Queries Redis bitmap via `redisTemplate.opsForValue().getBit(BLOOM_FILTER_KEY, index)` for each index.
    - If any bit is `0` (or `null`/`false`), marks `allBitsSet = false`, sets all calculated bit indices to `1` using `redisTemplate.opsForValue().setBit(...)`, and treats the item as a new unique record.
    - Measures execution latency using `(System.nanoTime() - startTime) / 1_000_000.0` (in milliseconds) and logs the output directly to `System.out` for telemetry tracking.
    - Returns `true` if all bits were already present (`1`), or `false` otherwise.

### 4. Automated Tests
- **`src/test/java/com/osmosis/service/BloomFilterServiceTest.java`** *(Created)*:
  - Unit tests utilizing Mockito (`@ExtendWith(MockitoExtension.class)`) to mock `RedisTemplate<String, Object>` and `ValueOperations<String, Object>`.
  - Verifies:
    1. Returns `true` and never calls `setBit` when all calculated bit indices return `true`.
    2. Returns `false` and calls `setBit(BLOOM_FILTER_KEY, index, true)` for all 5 calculated indices when a bit is `false`.
    3. Returns `false` and calls `setBit` when a bit is `null`.
- **`src/test/java/com/osmosis/util/HashUtilTest.java`** *(Created)*:
  - Validates index count, range bounds (`[0, bitArraySize)`), determinism across multiple runs, and argument validation exception handling.

---

## Verification Results
- Executed `mvn clean test` in `/Users/mohammedaafaqahmed/Developer/Osmosis`.
- **Results:** 9 tests run across all test suites, 0 failures, 0 errors, 0 skipped.
- **Status:** `BUILD SUCCESS`.
