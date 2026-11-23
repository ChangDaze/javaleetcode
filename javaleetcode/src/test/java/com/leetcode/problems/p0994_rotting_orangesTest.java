package com.leetcode.problems;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class p0994_rotting_orangesTest {

    @FunctionalInterface
    private interface RottingOrangesSolver {
        int orangesRotting(int[][] grid);
    }

    private final p0994_rotting_oranges solution = new p0994_rotting_oranges();

    private int[][] copyGrid(int[][] grid) {
        int[][] copy = new int[grid.length][];
        for (int i = 0; i < grid.length; i++) {
            copy[i] = Arrays.copyOf(grid[i], grid[i].length);
        }
        return copy;
    }

    private void assertMinutes(RottingOrangesSolver solver, int expected, int[][] grid) {
        assertEquals(expected, solver.orangesRotting(copyGrid(grid)));
    }

    private void assertAllScenarios(RottingOrangesSolver solver) {
        // 1. LeetCode Example 1: single rotten source floods entire connected grid in 4 minutes
        assertMinutes(solver, 4, new int[][]{
                {2, 1, 1},
                {1, 1, 0},
                {0, 1, 1}
        });

        // 2. LeetCode Example 2: bottom-left fresh orange is isolated and unreachable
        assertMinutes(solver, -1, new int[][]{
                {2, 1, 1},
                {0, 1, 1},
                {1, 0, 1}
        });

        // 3. LeetCode Example 3: no fresh oranges initially -> 0 minutes
        assertMinutes(solver, 0, new int[][]{
                {0, 2}
        });

        // 4. Single-cell boundary cases
        assertMinutes(solver, 0, new int[][]{{0}});
        assertMinutes(solver, -1, new int[][]{{1}});
        assertMinutes(solver, 0, new int[][]{{2}});

        // 5. Uniform grids: all empty, all rotten, all fresh
        assertMinutes(solver, 0, new int[][]{
                {0, 0},
                {0, 0}
        });
        assertMinutes(solver, 0, new int[][]{
                {2, 2},
                {2, 2}
        });
        assertMinutes(solver, -1, new int[][]{
                {1, 1},
                {1, 1}
        });

        // 6. Off-by-one trap: finishes in 1 minute (newly rotted cell still in queue must not add extra minute)
        assertMinutes(solver, 1, new int[][]{{2, 1}});

        // 7. 1D row and 1D column (catches row/column dimension mix-ups)
        assertMinutes(solver, 4, new int[][]{{2, 1, 1, 1, 1}});
        assertMinutes(solver, 3, new int[][]{{1}, {1}, {1}, {2}});

        // 8. Rectangular grids (rows != cols)
        assertMinutes(solver, 5, new int[][]{
                {2, 1, 1, 1, 1},
                {1, 1, 1, 1, 1}
        });
        assertMinutes(solver, 5, new int[][]{
                {1, 1},
                {1, 1},
                {1, 1},
                {1, 1},
                {1, 2}
        });

        // 9. Diagonal neighbors only (verifies strictly 4-directional adjacency)
        assertMinutes(solver, -1, new int[][]{
                {2, 0},
                {0, 1}
        });

        // 10. Multi-source collision on a shared fresh orange (verifies no duplicate enqueue / double counting)
        assertMinutes(solver, 1, new int[][]{
                {2, 1, 2}
        });
        assertMinutes(solver, 1, new int[][]{
                {0, 2, 0},
                {2, 1, 2},
                {0, 2, 0}
        });

        // 11. Multi-source simultaneous flood from opposite corners of a 5x5 grid
        //     Max distance from nearest corner is 4 (e.g. (0,4), (4,0), (2,2))
        assertMinutes(solver, 4, new int[][]{
                {2, 1, 1, 1, 1},
                {1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1},
                {1, 1, 1, 1, 1},
                {1, 1, 1, 1, 2}
        });

        // 12. Disconnected components — both islands have a rotten source (answer = max of both times)
        assertMinutes(solver, 3, new int[][]{
                {2, 1, 0, 2, 1, 1, 1}
        });

        // 13. Disconnected components — one island rots, another fresh island is unreachable
        assertMinutes(solver, -1, new int[][]{
                {2, 1, 0, 1, 1}
        });

        // 14. Winding maze path exercising all 4 directions (right, down, left, up)
        assertMinutes(solver, 16, new int[][]{
                {2, 1, 1, 1, 1},
                {0, 0, 0, 0, 1},
                {1, 1, 1, 0, 1},
                {1, 0, 1, 0, 1},
                {1, 0, 1, 1, 1}
        });
    }

    private void assertMaxConstraintAndLargeGrid(RottingOrangesSolver solver) {
        // 1. LeetCode max constraint (10 x 10): single rotten at top-left, all others fresh -> 9 + 9 = 18
        int[][] maxGrid = new int[10][10];
        for (int[] row : maxGrid) {
            Arrays.fill(row, 1);
        }
        maxGrid[0][0] = 2;
        assertMinutes(solver, 18, maxGrid);

        // 2. LeetCode max constraint (10 x 10) with walled-off bottom-right fresh orange -> -1
        int[][] blockedMaxGrid = copyGrid(maxGrid);
        blockedMaxGrid[8][9] = 0;
        blockedMaxGrid[9][8] = 0;
        assertMinutes(solver, -1, blockedMaxGrid);

        // 3. Large scalability stress test (200 x 200 = 40,000 cells): single rotten at (0, 0) -> 398
        int n = 200;
        int[][] largeGrid = new int[n][n];
        for (int[] row : largeGrid) {
            Arrays.fill(row, 1);
        }
        largeGrid[0][0] = 2;
        assertMinutes(solver, 2 * (n - 1), largeGrid);
    }

    // ==================== Tests for orangesRotting (2 Queues + HashSet<Cell>) ====================

    @Test
    void testOrangesRottingAllScenarios() {
        assertAllScenarios(solution::orangesRotting);
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testOrangesRottingMaxConstraintAndLargeGrid() {
        assertMaxConstraintAndLargeGrid(solution::orangesRotting);
    }

    // ==================== Tests for orangesRotting2 (1 Queue + In-Place Marking + DIRS) ====================

    @Test
    void testOrangesRotting2AllScenarios() {
        assertAllScenarios(solution::orangesRotting2);
    }

    @Test
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void testOrangesRotting2MaxConstraintAndLargeGrid() {
        assertMaxConstraintAndLargeGrid(solution::orangesRotting2);
    }
}
