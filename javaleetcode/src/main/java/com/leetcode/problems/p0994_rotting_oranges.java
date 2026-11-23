package com.leetcode.problems;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class p0994_rotting_oranges {
    public int orangesRotting(int[][] grid) {
        // 0 : empty cell, 1 : fresh orange, 2 : rotten orange.
        record Cell(int r, int c) {}

        // BFS flood
        Queue<Cell> queue1 = new ArrayDeque<>();
        Queue<Cell> queue2 = new ArrayDeque<>();
        Set<Cell> rottens = new HashSet<>();
        int orangeCount = 0;
        // add all rotten into queue
        for (int i = 0 ; i < grid.length; i++) {
            for (int j = 0 ; j < grid[0].length; j++){
                if(grid[i][j] == 2) {
                    Cell cell = new Cell(i, j);
                    queue1.offer(cell);
                    rottens.add(cell);
                }

                if(grid[i][j] != 0) {
                    orangeCount++;
                }
            }
        }

        // start flood
        int time = 0;
        int turn = 0;
        while (rottens.size() != orangeCount && (turn == 0 ? (!queue1.isEmpty()) : (!queue2.isEmpty()))){
            time++;

            if(turn == 0) {
                while (!queue1.isEmpty()){
                    Cell cell = queue1.poll();
                    Cell up = new Cell(cell.r - 1, cell.c);
                    Cell down = new Cell(cell.r + 1, cell.c);
                    Cell left = new Cell(cell.r, cell.c - 1);
                    Cell right = new Cell(cell.r, cell.c + 1);

                    if(up.r >= 0 && grid[up.r][up.c] == 1 && !rottens.contains(up)) {
                        queue2.add(up);
                        rottens.add(up);
                    }

                    if(down.r < grid.length && grid[down.r][down.c] == 1 && !rottens.contains(down)) {
                        queue2.add(down);
                        rottens.add(down);
                    }

                    if(left.c >= 0 && grid[left.r][left.c] == 1 && !rottens.contains(left)) {
                        queue2.add(left);
                        rottens.add(left);
                    }

                    if(right.c < grid[0].length && grid[right.r][right.c] == 1 && !rottens.contains(right)) {
                        queue2.add(right);
                        rottens.add(right);
                    }
                }
                turn = 1;
            }
            else {
                while (!queue2.isEmpty()){
                    Cell cell = queue2.poll();
                    Cell up = new Cell(cell.r - 1, cell.c);
                    Cell down = new Cell(cell.r + 1, cell.c);
                    Cell left = new Cell(cell.r, cell.c - 1);
                    Cell right = new Cell(cell.r, cell.c + 1);

                    if(up.r >= 0 && grid[up.r][up.c] == 1 && !rottens.contains(up)) {
                        queue1.add(up);
                        rottens.add(up);
                    }

                    if(down.r < grid.length && grid[down.r][down.c] == 1 && !rottens.contains(down)) {
                        queue1.add(down);
                        rottens.add(down);
                    }

                    if(left.c >= 0 && grid[left.r][left.c] == 1 && !rottens.contains(left)) {
                        queue1.add(left);
                        rottens.add(left);
                    }

                    if(right.c < grid[0].length && grid[right.r][right.c] == 1 && !rottens.contains(right)) {
                        queue1.add(right);
                        rottens.add(right);
                    }
                }
                turn = 0;
            }
        }

        return rottens.size() == orangeCount ? time : -1;
    }

    public int orangesRotting2(int[][] grid) {
        // 0 : empty cell, 1 : fresh orange, 2 : rotten orange.
        record Cell(int r, int c) {}
        // BFS flood
        Queue<Cell> queue = new ArrayDeque<>();
        int freshCount = 0;
        int totalRows = grid.length;
        int totalCols = grid[0].length;
        // add all rotten into queue
        for (int i = 0 ; i < totalRows; i++) {
            for (int j = 0; j < totalCols; j++){
                if(grid[i][j] == 2) {
                    Cell cell = new Cell(i, j);
                    queue.offer(cell);

                }

                if(grid[i][j] == 1) {
                    freshCount++;
                }
            }
        }

        // start flood
        int time = 0;
        int[][] dirs = {{-1,0}, {1,0}, {0,-1}, {0,1}};
        while (freshCount != 0 && !queue.isEmpty()){
            time++;
            int size = queue.size(); // use queue FIFO feature to only handle this round rotten.
            for (int i = 0; i < size ; i++){
                Cell rottenCell = queue.poll();
                for(int[] dir : dirs) {
                    int newRow = rottenCell.r() + dir[0];
                    int newColumn = rottenCell.c() + dir[1];
                    if(newRow >= 0 && newRow < totalRows && newColumn >= 0 && newColumn < totalCols && grid[newRow][newColumn] == 1) {
                        grid[newRow][newColumn] = 2; // mark in place to flag it was rotten.
                        freshCount--;
                        queue.offer(new Cell(newRow, newColumn));
                    }
                }
            }
        }

        return freshCount == 0 ? time : -1;
    }
}
