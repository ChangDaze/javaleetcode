package com.leetcode.problems;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class p0207_course_scheduleTest {

    @FunctionalInterface
    private interface CourseScheduleSolver {
        boolean canFinish(int numCourses, int[][] prerequisites);
    }

    private final p0207_course_schedule solution = new p0207_course_schedule();

    private void assertAllScenarios(CourseScheduleSolver solver) {
        // 1. LeetCode Example 1: 0 -> 1
        assertTrue(solver.canFinish(2, new int[][]{{1, 0}}));

        // 2. LeetCode Example 2: 0 <-> 1 (2-node cycle)
        assertFalse(solver.canFinish(2, new int[][]{{1, 0}, {0, 1}}));

        // 3. Single course, no prerequisites
        assertTrue(solver.canFinish(1, new int[][]{}));

        // 4. Multiple courses, no prerequisites
        assertTrue(solver.canFinish(5, new int[][]{}));

        // 5. Self-loop (a course depends on itself)
        assertFalse(solver.canFinish(1, new int[][]{{0, 0}}));
        assertFalse(solver.canFinish(3, new int[][]{{1, 1}}));

        // 6. Linear chain forward: 0 -> 1 -> 2 -> 3
        assertTrue(solver.canFinish(4, new int[][]{{1, 0}, {2, 1}, {3, 2}}));

        // 7. Reverse linear chain: 3 -> 2 -> 1 -> 0 (worst-case round order for naive peeling)
        assertTrue(solver.canFinish(4, new int[][]{{0, 1}, {1, 2}, {2, 3}}));

        // 8. Diamond DAG: 0 -> 1, 0 -> 2, 1 -> 3, 2 -> 3
        //    Verifies DFS 3-color logic does NOT treat revisiting a safe node as a cycle
        assertTrue(solver.canFinish(4, new int[][]{{1, 0}, {2, 0}, {3, 1}, {3, 2}}));

        // 9. Longer cycle: 0 -> 1 -> 2 -> 3 -> 0
        assertFalse(solver.canFinish(4, new int[][]{{1, 0}, {2, 1}, {3, 2}, {0, 3}}));

        // 10. Disconnected components — all acyclic (including isolated node 4)
        assertTrue(solver.canFinish(5, new int[][]{{1, 0}, {3, 2}}));

        // 11. Disconnected components — one acyclic island + one cycle island (2 -> 3 -> 4 -> 2)
        assertFalse(solver.canFinish(5, new int[][]{{1, 0}, {3, 2}, {4, 3}, {2, 4}}));

        // 12. Lollipop / rho graph: tail leading into a cycle (0 -> 1 -> 2 -> 3 -> 1)
        assertFalse(solver.canFinish(4, new int[][]{{1, 0}, {2, 1}, {3, 2}, {1, 3}}));

        // 13. Cycle with an outgoing leaf branch (0 <-> 1, 1 -> 2)
        assertFalse(solver.canFinish(3, new int[][]{{1, 0}, {0, 1}, {2, 1}}));

        // 14. Fan-in & fan-out DAG (0 unlocks 1, 2, 3; all three unlock 4)
        assertTrue(solver.canFinish(5, new int[][]{
                {1, 0}, {2, 0}, {3, 0},
                {4, 1}, {4, 2}, {4, 3}
        }));
    }

    private void assertDiamondChainMemoization(CourseScheduleSolver solver) {
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
        assertTrue(solver.canFinish(numCourses, edges.toArray(new int[0][])));
    }

    private void assertLargeInputMaxConstraint(CourseScheduleSolver solver) {
        // LeetCode constraints: 1 <= numCourses <= 2000, 0 <= prerequisites.length <= 5000
        int n = 2000;

        // 1. Reverse chain of length 2000 (acyclic, deepest possible dependency chain)
        int[][] reverseChain = new int[n - 1][2];
        for (int i = 0; i < n - 1; i++) {
            reverseChain[i] = new int[]{i, i + 1};
        }
        assertTrue(solver.canFinish(n, reverseChain));

        // 2. Same chain closed into a 2000-node cycle
        int[][] largeCycle = new int[n][2];
        for (int i = 0; i < n - 1; i++) {
            largeCycle[i] = new int[]{i, i + 1};
        }
        largeCycle[n - 1] = new int[]{n - 1, 0}; // set cycle at last node
        assertFalse(solver.canFinish(n, largeCycle));

        // 3. 5000-edge acyclic DAG (edges always point from smaller to larger index)
        int[][] denseDag = new int[5000][2];
        int idx = 0;
        for (int step = 1; idx < 5000; step++) {
            for (int u = 0; u + step < n && idx < 5000; u++) {
                denseDag[idx++] = new int[]{u + step, u};
            }
        }
        assertTrue(solver.canFinish(n, denseDag));
    }

    // ==================== Tests for canFinish (Iterative Peeling, O(V * (V + E))) ====================

    @Test
    void testCanFinishAllScenarios() {
        assertAllScenarios(solution::canFinish);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testCanFinishDiamondChain() {
        assertDiamondChainMemoization(solution::canFinish);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testCanFinishLargeInputMaxConstraint() {
        assertLargeInputMaxConstraint(solution::canFinish);
    }

    // ==================== Tests for canFinish2 (Kahn's Algorithm / BFS, O(V + E)) ====================

    @Test
    void testCanFinish2AllScenarios() {
        assertAllScenarios(solution::canFinish2);
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testCanFinish2DiamondChain() {
        assertDiamondChainMemoization(solution::canFinish2);
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testCanFinish2LargeInputMaxConstraint() {
        assertLargeInputMaxConstraint(solution::canFinish2);
    }

    // ==================== Tests for canFinish3 (DFS 3-Color Cycle Detection, O(V + E)) ====================

    @Test
    void testCanFinish3AllScenarios() {
        assertAllScenarios(solution::canFinish3);
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testCanFinish3DiamondChain() {
        assertDiamondChainMemoization(solution::canFinish3);
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testCanFinish3LargeInputMaxConstraint() {
        assertLargeInputMaxConstraint(solution::canFinish3);
    }
}
