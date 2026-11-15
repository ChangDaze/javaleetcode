package com.leetcode.problems;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.ToIntFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class p0084_largest_rectangle_in_histogramTest {

    private final p0084_largest_rectangle_in_histogram solution =
            new p0084_largest_rectangle_in_histogram();

    private void assertAllScenarios(ToIntFunction<int[]> solver) {
        // 1. LeetCode Example 1: [2, 1, 5, 6, 2, 3] -> max area = 10 (5 and 6 covering width 2)
        assertEquals(10, solver.applyAsInt(new int[]{2, 1, 5, 6, 2, 3}));

        // 2. LeetCode Example 2: [2, 4] -> max area = 4
        assertEquals(4, solver.applyAsInt(new int[]{2, 4}));

        // 3. Single bar
        assertEquals(7, solver.applyAsInt(new int[]{7}));

        // 4. Single bar with height 0
        assertEquals(0, solver.applyAsInt(new int[]{0}));

        // 5. All equal heights (tests duplicate height handling in monotonic stack: e.g., [4, 4])
        assertEquals(8, solver.applyAsInt(new int[]{4, 4}));
        assertEquals(20, solver.applyAsInt(new int[]{5, 5, 5, 5}));

        // 6. Strictly decreasing heights (tests left extension into already-popped taller bars: e.g., [5, 4])
        assertEquals(8, solver.applyAsInt(new int[]{5, 4}));
        assertEquals(12, solver.applyAsInt(new int[]{6, 5, 4, 3, 2, 1}));

        // 7. Strictly increasing heights (exercises full stack flush at the end)
        assertEquals(12, solver.applyAsInt(new int[]{1, 2, 3, 4, 5, 6}));

        // 8. Valley in the middle (tests full-width base rectangle vs. tall spikes)
        assertEquals(4, solver.applyAsInt(new int[]{4, 1, 4}));
        assertEquals(6, solver.applyAsInt(new int[]{4, 2, 4}));
        assertEquals(10, solver.applyAsInt(new int[]{5, 2, 5, 2, 5}));

        // 9. Peak in the middle (pyramid)
        assertEquals(9, solver.applyAsInt(new int[]{1, 3, 5, 3, 1}));

        // 10. Bars containing zeros (0-height bars split valid rectangles completely)
        assertEquals(6, solver.applyAsInt(new int[]{2, 0, 6, 0, 3, 3}));

        // 11. Left extension after popping taller middle bar ([1, 5, 4] -> 4 extends left across 5)
        assertEquals(8, solver.applyAsInt(new int[]{1, 5, 4}));
    }

    private void assertLargeInputMaxConstraint(ToIntFunction<int[]> solver) {
        // LeetCode constraint: 1 <= heights.length <= 10^5, 0 <= heights[i] <= 10^4
        int n = 100_000;
        int[] heights = new int[n];
        Arrays.fill(heights, 10_000);
        // 100,000 * 10,000 = 1,000,000,000 (fits in 32-bit signed int)
        assertEquals(1_000_000_000, solver.applyAsInt(heights));
    }

    // ==================== Tests for largestRectangleArea (2-Pass Stack + Span) ====================

    @Test
    void testLargestRectangleAreaAllScenarios() {
        assertAllScenarios(solution::largestRectangleArea);
    }

    @Test
    void testLargestRectangleAreaLeetCodeExample1() {
        assertEquals(10, solution.largestRectangleArea(new int[]{2, 1, 5, 6, 2, 3}));
    }

    @Test
    void testLargestRectangleAreaLargeInputMaxConstraint() {
        assertLargeInputMaxConstraint(solution::largestRectangleArea);
    }

    // ==================== Tests for largestRectangleArea2 (2-Pass Jump Pointers) ====================

    @Test
    void testLargestRectangleArea2AllScenarios() {
        assertAllScenarios(solution::largestRectangleArea2);
    }

    @Test
    void testLargestRectangleArea2LeetCodeExample1() {
        assertEquals(10, solution.largestRectangleArea2(new int[]{2, 1, 5, 6, 2, 3}));
    }

    @Test
    void testLargestRectangleArea2LargeInputMaxConstraint() {
        assertLargeInputMaxConstraint(solution::largestRectangleArea2);
    }

    // ==================== Tests for largestRectangleArea3 (1-Pass Deque Monotonic Stack) ====================

    @Test
    void testLargestRectangleArea3AllScenarios() {
        assertAllScenarios(solution::largestRectangleArea3);
    }

    @Test
    void testLargestRectangleArea3LeetCodeExample1() {
        assertEquals(10, solution.largestRectangleArea3(new int[]{2, 1, 5, 6, 2, 3}));
    }

    @Test
    void testLargestRectangleArea3LargeInputMaxConstraint() {
        assertLargeInputMaxConstraint(solution::largestRectangleArea3);
    }

    // ==================== Tests for largestRectangleArea4 (1-Pass Primitive Array Stack + Sentinel) ====================

    @Test
    void testLargestRectangleArea4AllScenarios() {
        assertAllScenarios(solution::largestRectangleArea4);
    }

    @Test
    void testLargestRectangleArea4LeetCodeExample1() {
        assertEquals(10, solution.largestRectangleArea4(new int[]{2, 1, 5, 6, 2, 3}));
    }

    @Test
    void testLargestRectangleArea4LargeInputMaxConstraint() {
        assertLargeInputMaxConstraint(solution::largestRectangleArea4);
    }
}
