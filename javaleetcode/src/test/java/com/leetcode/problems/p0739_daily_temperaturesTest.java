package com.leetcode.problems;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class p0739_daily_temperaturesTest {

    private final p0739_daily_temperatures solution = new p0739_daily_temperatures();

    private void assertAllScenarios(UnaryOperator<int[]> method) {
        // LeetCode Example 1
        assertArrayEquals(
                new int[]{1, 1, 4, 2, 1, 1, 0, 0},
                method.apply(new int[]{73, 74, 75, 71, 69, 72, 76, 73})
        );

        // LeetCode Example 2
        assertArrayEquals(
                new int[]{1, 1, 1, 0},
                method.apply(new int[]{30, 40, 50, 60})
        );

        // LeetCode Example 3
        assertArrayEquals(
                new int[]{1, 1, 0},
                method.apply(new int[]{30, 60, 90})
        );

        // Single day (min constraint length = 1)
        assertArrayEquals(
                new int[]{0},
                method.apply(new int[]{75})
        );

        // Strictly decreasing temperatures
        assertArrayEquals(
                new int[]{0, 0, 0, 0, 0},
                method.apply(new int[]{90, 80, 70, 60, 50})
        );

        // All equal temperatures (must be strictly warmer >)
        assertArrayEquals(
                new int[]{0, 0, 0, 0},
                method.apply(new int[]{70, 70, 70, 70})
        );

        // Duplicate temperatures before a warmer day
        assertArrayEquals(
                new int[]{3, 2, 1, 0},
                method.apply(new int[]{70, 70, 70, 75})
        );

        // Valley and peak pattern
        assertArrayEquals(
                new int[]{5, 1, 2, 1, 1, 0},
                method.apply(new int[]{80, 60, 70, 60, 75, 85})
        );
    }

    private void assertLargeInputMaxConstraint(UnaryOperator<int[]> method) {
        // LeetCode constraint: 1 <= temperatures.length <= 10^5
        int n = 100_000;
        int[] temperatures = new int[n];
        Arrays.fill(temperatures, 30);
        temperatures[n - 1] = 100;

        int[] expected = new int[n];
        for (int i = 0; i < n - 1; i++) {
            expected[i] = (n - 1) - i;
        }
        expected[n - 1] = 0;

        assertArrayEquals(expected, method.apply(temperatures));
    }

    // ==================== Tests for dailyTemperatures (Deque<int[]> Right-to-Left) ====================

    @Test
    void testDailyTemperaturesAllScenarios() {
        assertAllScenarios(solution::dailyTemperatures);
    }

    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void testDailyTemperaturesLargeInput() {
        assertLargeInputMaxConstraint(solution::dailyTemperatures);
    }

    // ==================== Tests for dailyTemperatures2 (Deque<Integer> Right-to-Left) ====================

    @Test
    void testDailyTemperatures2AllScenarios() {
        assertAllScenarios(solution::dailyTemperatures2);
    }

    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void testDailyTemperatures2LargeInput() {
        assertLargeInputMaxConstraint(solution::dailyTemperatures2);
    }

    // ==================== Tests for dailyTemperatures3 (Deque<Integer> Left-to-Right) ====================

    @Test
    void testDailyTemperatures3AllScenarios() {
        assertAllScenarios(solution::dailyTemperatures3);
    }

    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void testDailyTemperatures3LargeInput() {
        assertLargeInputMaxConstraint(solution::dailyTemperatures3);
    }

    // ==================== Tests for dailyTemperatures4 (Backward Jump O(1) Space) ====================

    @Test
    void testDailyTemperatures4AllScenarios() {
        assertAllScenarios(solution::dailyTemperatures4);
    }

    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void testDailyTemperatures4LargeInput() {
        assertLargeInputMaxConstraint(solution::dailyTemperatures4);
    }
}
