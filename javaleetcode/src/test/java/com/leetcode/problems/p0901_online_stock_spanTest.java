package com.leetcode.problems;

import org.junit.jupiter.api.Test;

import java.util.function.IntUnaryOperator;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class p0901_online_stock_spanTest {

    private int[] executeSequence(int[] prices, IntUnaryOperator nextMethod) {
        int[] actual = new int[prices.length];
        for (int i = 0; i < prices.length; i++) {
            actual[i] = nextMethod.applyAsInt(prices[i]);
        }
        return actual;
    }

    private void assertAllScenarios(java.util.function.Supplier<IntUnaryOperator> spannerFactory) {
        // 1. LeetCode Example 1
        assertArrayEquals(
                new int[]{1, 1, 1, 2, 1, 4, 6},
                executeSequence(new int[]{100, 80, 60, 70, 60, 75, 85}, spannerFactory.get())
        );

        // 2. Single day
        assertArrayEquals(
                new int[]{1},
                executeSequence(new int[]{50}, spannerFactory.get())
        );

        // 3. Strictly decreasing prices
        assertArrayEquals(
                new int[]{1, 1, 1, 1, 1},
                executeSequence(new int[]{100, 90, 80, 70, 60}, spannerFactory.get())
        );

        // 4. Strictly increasing prices (exercises baseIndex < 0 / empty stack on every step after day 0)
        assertArrayEquals(
                new int[]{1, 2, 3, 4, 5},
                executeSequence(new int[]{10, 20, 30, 40, 50}, spannerFactory.get())
        );

        // 5. All equal prices (price_i <= price_today)
        assertArrayEquals(
                new int[]{1, 2, 3, 4},
                executeSequence(new int[]{50, 50, 50, 50}, spannerFactory.get())
        );

        // 6. Jump over new all-time high
        assertArrayEquals(
                new int[]{1, 2, 1, 4},
                executeSequence(new int[]{10, 20, 15, 25}, spannerFactory.get())
        );

        // 7. Alternating peaks and valleys
        assertArrayEquals(
                new int[]{1, 1, 2, 1, 2, 6, 7},
                executeSequence(new int[]{30, 20, 25, 10, 15, 30, 35}, spannerFactory.get())
        );
    }

    private void assertLargeInputMaxConstraint(IntUnaryOperator nextMethod) {
        // LeetCode constraint: up to 10^4 calls, 1 <= price <= 10^5
        int n = 10_000;
        for (int i = 1; i <= n; i++) {
            assertEquals(i, nextMethod.applyAsInt(i));
        }
    }

    // ==================== Tests for StockSpanner (Original Jump Method) ====================

    private int[] runStockSpanner(int[] prices) {
        p0901_online_stock_span.StockSpanner spanner =
                new p0901_online_stock_span.StockSpanner();
        return executeSequence(prices, spanner::next);
    }

    @Test
    void testStockSpannerAllScenarios() {
        assertAllScenarios(() -> new p0901_online_stock_span.StockSpanner()::next);
    }

    @Test
    void testStockSpannerLeetCodeExample1() {
        int[] prices = {100, 80, 60, 70, 60, 75, 85};
        int[] expected = {1, 1, 1, 2, 1, 4, 6};
        assertArrayEquals(expected, runStockSpanner(prices));
    }

    @Test
    void testStockSpannerLargeInputMaxConstraint() {
        assertLargeInputMaxConstraint(new p0901_online_stock_span.StockSpanner()::next);
    }

    // ==================== Tests for StockSpanner2 (Clean Jump Method) ====================

    private int[] runStockSpanner2(int[] prices) {
        p0901_online_stock_span.StockSpanner2 spanner =
                new p0901_online_stock_span.StockSpanner2();
        return executeSequence(prices, spanner::next);
    }

    @Test
    void testStockSpanner2AllScenarios() {
        assertAllScenarios(() -> new p0901_online_stock_span.StockSpanner2()::next);
    }

    @Test
    void testStockSpanner2LeetCodeExample1() {
        int[] prices = {100, 80, 60, 70, 60, 75, 85};
        int[] expected = {1, 1, 1, 2, 1, 4, 6};
        assertArrayEquals(expected, runStockSpanner2(prices));
    }

    @Test
    void testStockSpanner2LargeInputMaxConstraint() {
        assertLargeInputMaxConstraint(new p0901_online_stock_span.StockSpanner2()::next);
    }

    // ==================== Tests for StockSpanner3 (Monotonic Stack) ====================

    private int[] runStockSpanner3(int[] prices) {
        p0901_online_stock_span.StockSpanner3 spanner =
                new p0901_online_stock_span.StockSpanner3();
        return executeSequence(prices, spanner::next);
    }

    @Test
    void testStockSpanner3AllScenarios() {
        assertAllScenarios(() -> new p0901_online_stock_span.StockSpanner3()::next);
    }

    @Test
    void testStockSpanner3LeetCodeExample1() {
        int[] prices = {100, 80, 60, 70, 60, 75, 85};
        int[] expected = {1, 1, 1, 2, 1, 4, 6};
        assertArrayEquals(expected, runStockSpanner3(prices));
    }

    @Test
    void testStockSpanner3LargeInputMaxConstraint() {
        assertLargeInputMaxConstraint(new p0901_online_stock_span.StockSpanner3()::next);
    }
}
