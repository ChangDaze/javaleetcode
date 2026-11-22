package com.leetcode.problems;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class p0210_course_schedule_iiTest {

    @FunctionalInterface
    private interface CourseScheduleIISolver {
        int[] findOrder(int numCourses, int[][] prerequisites);
    }

    private final p0210_course_schedule_ii solution = new p0210_course_schedule_ii();

    /**
     * Verifies that {@code order} is a valid topological ordering of {@code 0..numCourses-1}
     * respecting every prerequisite edge {@code [target, prereq]} (prereq must appear before target).
     */
    private void assertValidOrder(int numCourses, int[][] prerequisites, int[] order) {
        assertEquals(numCourses, order.length,
                "Expected order of length " + numCourses + ", got " + Arrays.toString(order));

        int[] pos = new int[numCourses];
        boolean[] seen = new boolean[numCourses];
        for (int i = 0; i < numCourses; i++) {
            int course = order[i];
            assertTrue(course >= 0 && course < numCourses,
                    "Course ID out of range: " + course);
            assertFalse(seen[course],
                    "Duplicate course in order: " + course + " in " + Arrays.toString(order));
            seen[course] = true;
            pos[course] = i;
        }

        for (int[] edge : prerequisites) {
            int target = edge[0];
            int prereq = edge[1];
            assertTrue(pos[prereq] < pos[target],
                    "Prerequisite " + prereq + " must appear before " + target +
                            " in " + Arrays.toString(order));
        }
    }

    private void assertImpossible(int[] order) {
        assertArrayEquals(new int[0], order,
                "Expected empty array for cyclic graph, got " + Arrays.toString(order));
    }

    private void assertAllScenarios(CourseScheduleIISolver solver) {
        // 1. LeetCode Example 1: 0 -> 1 (unique order)
        int[][] ex1 = {{1, 0}};
        int[] res1 = solver.findOrder(2, ex1);
        assertValidOrder(2, ex1, res1);
        assertArrayEquals(new int[]{0, 1}, res1);

        // 2. LeetCode Example 2: Diamond DAG (0 -> 1, 0 -> 2, 1 -> 3, 2 -> 3)
        //    Multiple valid orders exist: [0, 1, 2, 3] or [0, 2, 1, 3]
        int[][] ex2 = {{1, 0}, {2, 0}, {3, 1}, {3, 2}};
        assertValidOrder(4, ex2, solver.findOrder(4, ex2));

        // 3. LeetCode Example 3: Single course, no prerequisites
        int[][] ex3 = {};
        int[] res3 = solver.findOrder(1, ex3);
        assertValidOrder(1, ex3, res3);
        assertArrayEquals(new int[]{0}, res3);

        // 4. Multiple courses, no prerequisites
        int[][] noPrereqs = {};
        assertValidOrder(5, noPrereqs, solver.findOrder(5, noPrereqs));

        // 5. 2-node cycle: 0 <-> 1
        assertImpossible(solver.findOrder(2, new int[][]{{1, 0}, {0, 1}}));

        // 6. Self-loop (a course depends on itself)
        assertImpossible(solver.findOrder(1, new int[][]{{0, 0}}));
        assertImpossible(solver.findOrder(3, new int[][]{{1, 1}}));

        // 7. Linear chain forward: 0 -> 1 -> 2 -> 3 (unique order)
        int[][] forwardChain = {{1, 0}, {2, 1}, {3, 2}};
        int[] resForward = solver.findOrder(4, forwardChain);
        assertValidOrder(4, forwardChain, resForward);
        assertArrayEquals(new int[]{0, 1, 2, 3}, resForward);

        // 8. Reverse linear chain: 3 -> 2 -> 1 -> 0 (unique order; verifies DFS out-of-order starts)
        int[][] reverseChain = {{0, 1}, {1, 2}, {2, 3}};
        int[] resReverse = solver.findOrder(4, reverseChain);
        assertValidOrder(4, reverseChain, resReverse);
        assertArrayEquals(new int[]{3, 2, 1, 0}, resReverse);

        // 9. Longer cycle: 0 -> 1 -> 2 -> 3 -> 0
        assertImpossible(solver.findOrder(4, new int[][]{{1, 0}, {2, 1}, {3, 2}, {0, 3}}));

        // 10. Disconnected components — all acyclic (including isolated node 4)
        int[][] disconnectedDag = {{1, 0}, {3, 2}};
        assertValidOrder(5, disconnectedDag, solver.findOrder(5, disconnectedDag));

        // 11. Disconnected components — one acyclic island + one cycle island (2 -> 3 -> 4 -> 2)
        assertImpossible(solver.findOrder(5, new int[][]{{1, 0}, {3, 2}, {4, 3}, {2, 4}}));

        // 12. Lollipop / rho graph: tail leading into a cycle (0 -> 1 -> 2 -> 3 -> 1)
        assertImpossible(solver.findOrder(4, new int[][]{{1, 0}, {2, 1}, {3, 2}, {1, 3}}));

        // 13. Cycle with an outgoing leaf branch (0 <-> 1, 1 -> 2)
        assertImpossible(solver.findOrder(3, new int[][]{{1, 0}, {0, 1}, {2, 1}}));

        // 14. Fan-in & fan-out DAG (0 unlocks 1, 2, 3; all three unlock 4)
        int[][] fanDag = {
                {1, 0}, {2, 0}, {3, 0},
                {4, 1}, {4, 2}, {4, 3}
        };
        int[] resFan = solver.findOrder(5, fanDag);
        assertValidOrder(5, fanDag, resFan);
        assertEquals(0, resFan[0]);
        assertEquals(4, resFan[4]);
    }

    private void assertDiamondChainMemoization(CourseScheduleIISolver solver) {
        // 30 chained diamonds (91 nodes, 120 edges): without DFS state 2 memoization,
        // revisiting shared join nodes causes 2^30 (~1 billion) traversals and times out.
        int diamonds = 30;
        int numCourses = diamonds * 3 + 1;
        List<int[]> edges = new ArrayList<>(diamonds * 4);
        for (int d = 0; d < diamonds; d++) {
            int start = d * 3;
            int left = start + 1;
            int right = start + 2;
            int join = start + 3;
            edges.add(new int[]{left, start});
            edges.add(new int[]{right, start});
            edges.add(new int[]{join, left});
            edges.add(new int[]{join, right});
        }
        int[][] prereqs = edges.toArray(new int[0][]);
        assertValidOrder(numCourses, prereqs, solver.findOrder(numCourses, prereqs));
    }

    private void assertLargeInputMaxConstraint(CourseScheduleIISolver solver) {
        // LeetCode constraints: 1 <= numCourses <= 2000
        int n = 2000;

        // 1. Reverse chain of length 2000 (acyclic, unique order [1999, 1998, ..., 0])
        int[][] reverseChain = new int[n - 1][2];
        int[] expectedReverse = new int[n];
        for (int i = 0; i < n - 1; i++) {
            reverseChain[i] = new int[]{i, i + 1};
        }
        for (int i = 0; i < n; i++) {
            expectedReverse[i] = n - 1 - i;
        }
        int[] resReverse = solver.findOrder(n, reverseChain);
        assertValidOrder(n, reverseChain, resReverse);
        assertArrayEquals(expectedReverse, resReverse);

        // 2. Same chain closed into a 2000-node cycle
        int[][] largeCycle = new int[n][2];
        for (int i = 0; i < n - 1; i++) {
            largeCycle[i] = new int[]{i, i + 1};
        }
        largeCycle[n - 1] = new int[]{n - 1, 0};
        assertImpossible(solver.findOrder(n, largeCycle));

        // 3. 5000-edge acyclic DAG (edges always point from smaller to larger index)
        int[][] denseDag = new int[5000][2];
        int idx = 0;
        for (int step = 1; idx < 5000; step++) {
            for (int u = 0; u + step < n && idx < 5000; u++) {
                denseDag[idx++] = new int[]{u + step, u};
            }
        }
        assertValidOrder(n, denseDag, solver.findOrder(n, denseDag));
    }

    // ==================== Tests for findOrder (Kahn's Algorithm / BFS with ArrayDeque) ====================

    @Test
    void testFindOrderAllScenarios() {
        assertAllScenarios(solution::findOrder);
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testFindOrderDiamondChain() {
        assertDiamondChainMemoization(solution::findOrder);
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testFindOrderLargeInputMaxConstraint() {
        assertLargeInputMaxConstraint(solution::findOrder);
    }

    // ==================== Tests for findOrder2 (Kahn's Algorithm / BFS with fused order[] queue) ====================

    @Test
    void testFindOrder2AllScenarios() {
        assertAllScenarios(solution::findOrder2);
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testFindOrder2DiamondChain() {
        assertDiamondChainMemoization(solution::findOrder2);
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testFindOrder2LargeInputMaxConstraint() {
        assertLargeInputMaxConstraint(solution::findOrder2);
    }

    // ==================== Tests for findOrder3 (DFS Post-Order + 3-Color Cycle Detection) ====================

    @Test
    void testFindOrder3AllScenarios() {
        assertAllScenarios(solution::findOrder3);
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testFindOrder3DiamondChain() {
        assertDiamondChainMemoization(solution::findOrder3);
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testFindOrder3LargeInputMaxConstraint() {
        assertLargeInputMaxConstraint(solution::findOrder3);
    }
}
