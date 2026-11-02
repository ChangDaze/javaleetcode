package com.leetcode.problems;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class p0380_insert_delete_getrandom_o1Test {

    // ==================== Tests for RandomizedSet ====================

    @Test
    void testLeetCodeExample() {
        p0380_insert_delete_getrandom_o1.RandomizedSet randomizedSet =
                new p0380_insert_delete_getrandom_o1.RandomizedSet();

        assertTrue(randomizedSet.insert(1));   // Inserts 1 to the set. Returns true as 1 was inserted successfully.
        assertFalse(randomizedSet.remove(2));  // Returns false as 2 does not exist in the set.
        assertTrue(randomizedSet.insert(2));   // Inserts 2 to the set, returns true. Set now contains [1, 2].

        int randomVal = randomizedSet.getRandom();
        assertTrue(randomVal == 1 || randomVal == 2); // getRandom() should return either 1 or 2 randomly.

        assertTrue(randomizedSet.remove(1));   // Removes 1 from the set, returns true. Set now contains [2].
        assertFalse(randomizedSet.insert(2));  // 2 was already in the set, so return false.
        assertEquals(2, randomizedSet.getRandom()); // Since 2 is the only number in the set, getRandom() will always return 2.
    }

    @Test
    void testRemoveOnlyElementAndReinsert() {
        p0380_insert_delete_getrandom_o1.RandomizedSet set =
                new p0380_insert_delete_getrandom_o1.RandomizedSet();

        // Edge case: removing the only element (which is also the last element in the list)
        assertTrue(set.insert(10));
        assertEquals(10, set.getRandom());
        assertTrue(set.remove(10));
        assertFalse(set.remove(10));

        // Re-inserting after emptying the set
        assertTrue(set.insert(10));
        assertEquals(10, set.getRandom());
    }

    @Test
    void testRemoveLastElementVsFirstElement() {
        p0380_insert_delete_getrandom_o1.RandomizedSet set =
                new p0380_insert_delete_getrandom_o1.RandomizedSet();

        assertTrue(set.insert(1));
        assertTrue(set.insert(2));
        assertTrue(set.insert(3));

        // Remove first element (swaps with 3)
        assertTrue(set.remove(1));
        assertFalse(set.remove(1));

        // Remove current last element
        assertTrue(set.remove(2));
        assertFalse(set.remove(2));

        // Only 3 should remain
        for (int i = 0; i < 20; i++) {
            assertEquals(3, set.getRandom());
        }
    }

    @Test
    void testGetRandomUniformDistribution() {
        p0380_insert_delete_getrandom_o1.RandomizedSet set =
                new p0380_insert_delete_getrandom_o1.RandomizedSet();

        Set<Integer> expectedElements = Set.of(10, 20, 30);
        for (int val : expectedElements) {
            assertTrue(set.insert(val));
        }

        Map<Integer, Integer> counts = new HashMap<>();
        int totalTrials = 6000;
        for (int i = 0; i < totalTrials; i++) {
            int val = set.getRandom();
            assertTrue(expectedElements.contains(val));
            counts.put(val, counts.getOrDefault(val, 0) + 1);
        }

        // Each of the 3 elements should appear roughly ~2000 times (check within reasonable bounds [1600, 2400])
        for (int val : expectedElements) {
            int count = counts.getOrDefault(val, 0);
            assertTrue(count >= 1600 && count <= 2400,
                    "Expected roughly uniform distribution (~2000), but got " + count + " for value " + val);
        }
    }

    // ==================== Tests for RandomizedSet2 (Optimized Lookups) ====================

    @Test
    void testRandomizedSet2LeetCodeExample() {
        p0380_insert_delete_getrandom_o1.RandomizedSet2 randomizedSet =
                new p0380_insert_delete_getrandom_o1.RandomizedSet2();

        assertTrue(randomizedSet.insert(1));
        assertFalse(randomizedSet.remove(2));
        assertTrue(randomizedSet.insert(2));

        int randomVal = randomizedSet.getRandom();
        assertTrue(randomVal == 1 || randomVal == 2);

        assertTrue(randomizedSet.remove(1));
        assertFalse(randomizedSet.insert(2));
        assertEquals(2, randomizedSet.getRandom());
    }

    @Test
    void testRandomizedSet2RemoveOnlyElementAndReinsert() {
        p0380_insert_delete_getrandom_o1.RandomizedSet2 set =
                new p0380_insert_delete_getrandom_o1.RandomizedSet2();

        assertTrue(set.insert(10));
        assertEquals(10, set.getRandom());
        assertTrue(set.remove(10));
        assertFalse(set.remove(10));

        assertTrue(set.insert(10));
        assertEquals(10, set.getRandom());
    }

    @Test
    void testRandomizedSet2RemoveLastElementVsFirstElement() {
        p0380_insert_delete_getrandom_o1.RandomizedSet2 set =
                new p0380_insert_delete_getrandom_o1.RandomizedSet2();

        assertTrue(set.insert(1));
        assertTrue(set.insert(2));
        assertTrue(set.insert(3));

        // Remove first element (swaps with 3)
        assertTrue(set.remove(1));
        assertFalse(set.remove(1));

        // Remove current last element (skips swap branch since removeIndex == list.size())
        assertTrue(set.remove(2));
        assertFalse(set.remove(2));

        for (int i = 0; i < 20; i++) {
            assertEquals(3, set.getRandom());
        }
    }

    @Test
    void testRandomizedSet2GetRandomUniformDistribution() {
        p0380_insert_delete_getrandom_o1.RandomizedSet2 set =
                new p0380_insert_delete_getrandom_o1.RandomizedSet2();

        Set<Integer> expectedElements = Set.of(10, 20, 30);
        for (int val : expectedElements) {
            assertTrue(set.insert(val));
        }

        Map<Integer, Integer> counts = new HashMap<>();
        int totalTrials = 6000;
        for (int i = 0; i < totalTrials; i++) {
            int val = set.getRandom();
            assertTrue(expectedElements.contains(val));
            counts.put(val, counts.getOrDefault(val, 0) + 1);
        }

        for (int val : expectedElements) {
            int count = counts.getOrDefault(val, 0);
            assertTrue(count >= 1600 && count <= 2400,
                    "Expected roughly uniform distribution (~2000), but got " + count + " for value " + val);
        }
    }
}
