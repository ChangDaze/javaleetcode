package com.leetcode.problems;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class p0033_search_in_rotated_sorted_arrayTest {

    @FunctionalInterface
    interface RotatedArraySearcher {
        int search(int[] nums, int target);
    }

    private final p0033_search_in_rotated_sorted_array solution =
            new p0033_search_in_rotated_sorted_array();

    private void assertAllScenarios(RotatedArraySearcher solver) {
        // 1. LeetCode official examples
        assertEquals(4, solver.search(new int[]{4, 5, 6, 7, 0, 1, 2}, 0));
        assertEquals(-1, solver.search(new int[]{4, 5, 6, 7, 0, 1, 2}, 3));
        assertEquals(-1, solver.search(new int[]{1}, 0));

        // 2. Single-element boundary cases (catches `while (l < r)` skipping the only element)
        assertEquals(0, solver.search(new int[]{1}, 1));
        assertEquals(-1, solver.search(new int[]{1}, -5));
        assertEquals(-1, solver.search(new int[]{1}, 5));

        // 3. Two-element rotated & unrotated cases (catches `nums[l] < nums[mid]` bug and `l = mid` infinite loop)
        assertEquals(1, solver.search(new int[]{3, 1}, 1));
        assertEquals(0, solver.search(new int[]{3, 1}, 3));
        assertEquals(-1, solver.search(new int[]{3, 1}, 2));
        assertEquals(-1, solver.search(new int[]{3, 1}, 0));
        assertEquals(-1, solver.search(new int[]{3, 1}, 4));

        assertEquals(0, solver.search(new int[]{1, 3}, 1));
        assertEquals(1, solver.search(new int[]{1, 3}, 3));
        assertEquals(-1, solver.search(new int[]{1, 3}, 2));
        assertEquals(-1, solver.search(new int[]{1, 3}, 0));
        assertEquals(-1, solver.search(new int[]{1, 3}, 4));

        // 4. Strictly increasing (unrotated, pivot == 0)
        int[] unrotated = new int[]{2, 4, 6, 8, 10, 12, 14};
        for (int i = 0; i < unrotated.length; i++) {
            assertEquals(i, solver.search(unrotated, unrotated[i]));
        }
        assertEquals(-1, solver.search(unrotated, 1));
        assertEquals(-1, solver.search(unrotated, 7));
        assertEquals(-1, solver.search(unrotated, 15));

        // 5. Extreme asymmetric rotations (pivot at index 1 and index n - 1)
        int[] pivotAtOne = new int[]{10, 1, 2, 3, 4, 5, 6};
        for (int i = 0; i < pivotAtOne.length; i++) {
            assertEquals(i, solver.search(pivotAtOne, pivotAtOne[i]));
        }
        assertEquals(-1, solver.search(pivotAtOne, 0));
        assertEquals(-1, solver.search(pivotAtOne, 8));
        assertEquals(-1, solver.search(pivotAtOne, 11));

        int[] pivotAtLast = new int[]{2, 3, 4, 5, 6, 7, 1};
        for (int i = 0; i < pivotAtLast.length; i++) {
            assertEquals(i, solver.search(pivotAtLast, pivotAtLast[i]));
        }
        assertEquals(-1, solver.search(pivotAtLast, 0));
        assertEquals(-1, solver.search(pivotAtLast, 8));

        // 6. Negative numbers and LeetCode boundary values (-10^4 to 10^4)
        int[] negatives = new int[]{0, 5000, 10000, -10000, -5000, -1};
        for (int i = 0; i < negatives.length; i++) {
            assertEquals(i, solver.search(negatives, negatives[i]));
        }
        assertEquals(-1, solver.search(negatives, -10001));
        assertEquals(-1, solver.search(negatives, -2500));
        assertEquals(-1, solver.search(negatives, 10001));

        // 7. Exhaustive check across ALL rotation offsets (odd length 7 and even length 6)
        assertAllRotations(solver, new int[]{10, 20, 30, 40, 50, 60, 70});
        assertAllRotations(solver, new int[]{10, 20, 30, 40, 50, 60});
    }

    private void assertAllRotations(RotatedArraySearcher solver, int[] sorted) {
        int n = sorted.length;
        for (int shift = 0; shift < n; shift++) {
            int[] rotated = new int[n];
            for (int i = 0; i < n; i++) {
                rotated[i] = sorted[(i + shift) % n];
            }
            for (int i = 0; i < n; i++) {
                assertEquals(i, solver.search(rotated, rotated[i]));
            }
            // Missing values: below min, inside gap, above max
            assertEquals(-1, solver.search(rotated, 5));
            assertEquals(-1, solver.search(rotated, 35));
            assertEquals(-1, solver.search(rotated, 99));
        }
    }

    private void assertMaxConstraintStressTest(RotatedArraySearcher solver) {
        // LeetCode constraint: 1 <= nums.length <= 5000, -10^4 <= nums[i], target <= 10^4
        int n = 5000;
        int pivot = 3179;
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) {
            int sortedIndex = (i - pivot + n) % n;
            nums[i] = sortedIndex * 4 - 10000; // even distinct values in [-10000, 9996]
        }

        // Verify every element in the 5000-element rotated array
        for (int i = 0; i < n; i++) {
            assertEquals(i, solver.search(nums, nums[i]));
        }

        // Missing values (below min, odd gaps, above max)
        assertEquals(-1, solver.search(nums, -10001));
        assertEquals(-1, solver.search(nums, -9999));
        assertEquals(-1, solver.search(nums, 1));
        assertEquals(-1, solver.search(nums, 9997));
        assertEquals(-1, solver.search(nums, 10000));
    }

    // ==================== Tests for search (2-Pass Pivot + Virtual % Offset) ====================

    @Test
    void testSearchAllScenarios() {
        assertAllScenarios(solution::search);
    }

    @Test
    void testSearchLeetCodeExample1() {
        assertEquals(4, solution.search(new int[]{4, 5, 6, 7, 0, 1, 2}, 0));
    }

    @Test
    void testSearchMaxConstraintStressTest() {
        assertMaxConstraintStressTest(solution::search);
    }

    // ==================== Tests for search2 (2-Pass Pivot + Sorted-Half Pruning) ====================

    @Test
    void testSearch2AllScenarios() {
        assertAllScenarios(solution::search2);
    }

    @Test
    void testSearch2LeetCodeExample1() {
        assertEquals(4, solution.search2(new int[]{4, 5, 6, 7, 0, 1, 2}, 0));
    }

    @Test
    void testSearch2MaxConstraintStressTest() {
        assertMaxConstraintStressTest(solution::search2);
    }

    // ==================== Tests for search3 (1-Pass Sorted-Half Binary Search) ====================

    @Test
    void testSearch3AllScenarios() {
        assertAllScenarios(solution::search3);
    }

    @Test
    void testSearch3LeetCodeExample1() {
        assertEquals(4, solution.search3(new int[]{4, 5, 6, 7, 0, 1, 2}, 0));
    }

    @Test
    void testSearch3MaxConstraintStressTest() {
        assertMaxConstraintStressTest(solution::search3);
    }

    // ==================== Tests for search4 (1-Pass Virtual ±∞ Projection) ====================

    @Test
    void testSearch4AllScenarios() {
        assertAllScenarios(solution::search4);
    }

    @Test
    void testSearch4LeetCodeExample1() {
        assertEquals(4, solution.search4(new int[]{4, 5, 6, 7, 0, 1, 2}, 0));
    }

    @Test
    void testSearch4MaxConstraintStressTest() {
        assertMaxConstraintStressTest(solution::search4);
    }
}
