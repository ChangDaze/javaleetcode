package com.leetcode.problems;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class p0146_lru_cacheTest {

    // ==================== Tests for LRUCache (Custom Doubly Linked List) ====================

    @Test
    void testLeetCodeExample() {
        p0146_lru_cache.LRUCache lRUCache = new p0146_lru_cache.LRUCache(2);

        lRUCache.put(1, 1); // cache: [1=1]
        lRUCache.put(2, 2); // cache: [2=2, 1=1]
        assertEquals(1, lRUCache.get(1)); // returns 1, cache: [1=1, 2=2]

        lRUCache.put(3, 3); // LRU key was 2, evicts key 2, cache: [3=3, 1=1]
        assertEquals(-1, lRUCache.get(2)); // returns -1 (not found)

        lRUCache.put(4, 4); // LRU key was 1, evicts key 1, cache: [4=4, 3=3]
        assertEquals(-1, lRUCache.get(1)); // returns -1 (not found)
        assertEquals(3, lRUCache.get(3)); // returns 3, cache: [3=3, 4=4]
        assertEquals(4, lRUCache.get(4)); // returns 4, cache: [4=4, 3=3]
    }

    @Test
    void testUpdateExistingKeyDoesNotEvict() {
        p0146_lru_cache.LRUCache cache = new p0146_lru_cache.LRUCache(2);

        cache.put(1, 10);
        cache.put(2, 20);

        // Update key 1 with a new value
        cache.put(1, 100);

        // Capacity is 2, updating existing key should NOT evict key 2
        assertEquals(100, cache.get(1));
        assertEquals(20, cache.get(2));

        // Now key 1 is LRU because key 2 was just read
        cache.put(3, 30); // should evict key 1
        assertEquals(-1, cache.get(1));
        assertEquals(20, cache.get(2));
        assertEquals(30, cache.get(3));
    }

    @Test
    void testCapacityOne() {
        p0146_lru_cache.LRUCache cache = new p0146_lru_cache.LRUCache(1);

        cache.put(2, 1);
        assertEquals(1, cache.get(2));

        cache.put(3, 2); // evicts key 2
        assertEquals(-1, cache.get(2));
        assertEquals(2, cache.get(3));

        // Update key 3
        cache.put(3, 5);
        assertEquals(5, cache.get(3));
    }

    @Test
    void testGetNonExistentKey() {
        p0146_lru_cache.LRUCache cache = new p0146_lru_cache.LRUCache(2);

        assertEquals(-1, cache.get(999));
        cache.put(1, 10);
        assertEquals(-1, cache.get(2));
        assertEquals(10, cache.get(1));
    }

    @Test
    void testFrequentAccessMaintainsFreshness() {
        p0146_lru_cache.LRUCache cache = new p0146_lru_cache.LRUCache(3);

        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3); // cache order: 3, 2, 1 (1 is LRU)

        // Frequently access 1
        assertEquals(1, cache.get(1)); // cache order: 1, 3, 2 (2 is LRU)

        cache.put(4, 4); // should evict 2
        assertEquals(-1, cache.get(2));
        assertEquals(1, cache.get(1));
        assertEquals(3, cache.get(3));
        assertEquals(4, cache.get(4));
    }

    // ==================== Tests for LRUCache2 (LinkedHashMap) ====================

    @Test
    void testLRUCache2LeetCodeExample() {
        p0146_lru_cache.LRUCache2 lRUCache = new p0146_lru_cache.LRUCache2(2);

        lRUCache.put(1, 1);
        lRUCache.put(2, 2);
        assertEquals(1, lRUCache.get(1));

        lRUCache.put(3, 3); // evicts key 2
        assertEquals(-1, lRUCache.get(2));

        lRUCache.put(4, 4); // evicts key 1
        assertEquals(-1, lRUCache.get(1));
        assertEquals(3, lRUCache.get(3));
        assertEquals(4, lRUCache.get(4));
    }

    @Test
    void testLRUCache2UpdateExistingKeyDoesNotEvict() {
        p0146_lru_cache.LRUCache2 cache = new p0146_lru_cache.LRUCache2(2);

        cache.put(1, 10);
        cache.put(2, 20);

        // Update key 1 with a new value
        cache.put(1, 100);

        assertEquals(100, cache.get(1));
        assertEquals(20, cache.get(2));

        cache.put(3, 30); // should evict key 1
        assertEquals(-1, cache.get(1));
        assertEquals(20, cache.get(2));
        assertEquals(30, cache.get(3));
    }

    @Test
    void testLRUCache2CapacityOne() {
        p0146_lru_cache.LRUCache2 cache = new p0146_lru_cache.LRUCache2(1);

        cache.put(2, 1);
        assertEquals(1, cache.get(2));

        cache.put(3, 2); // evicts key 2
        assertEquals(-1, cache.get(2));
        assertEquals(2, cache.get(3));
    }
}
