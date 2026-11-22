package com.leetcode.problems;

import java.util.*;

public class p0210_course_schedule_ii {
    public int[] findOrder(int numCourses, int[][] prerequisites) {
        List<Integer>[] adj = new ArrayList[numCourses];
        for (int i = 0; i<numCourses; i++){
            adj[i] = new ArrayList<>();
        }

        // indegree : number of directed edges (arrows) pointing into that vertex
        int[] indegree = new int[numCourses];

        for(int[] edge : prerequisites) {
            int from = edge[1];
            int to = edge[0];
            adj[from].add(to);
            indegree[to]++;
        }

        // set the start point.
        Queue<Integer> queue = new ArrayDeque<>();
        for (int i = 0; i < numCourses; i++) {
            if(indegree[i] == 0) {
                queue.offer(i);
            }
        }

        int[] order = new int[numCourses];
        int idx = 0;
        // HashSet<Integer> takenCourse = new HashSet<>();

        while (!queue.isEmpty()) {
            int curr = queue.poll();

            order[idx++] = curr;
            // takenCourse.add(curr); indegree[i] == 0 is offered at most once, so takenCourse actually no need and idx should be numCourses when fulfill the demand.

            for(int next : adj[curr]) {
                if(--indegree[next] == 0) {
                    queue.offer(next);
                }
            }
        }

        // return takenCourse.size() == numCourses ? order : new int[0];
        return idx == numCourses ? order : new int[0];
    }

    public int[] findOrder2(int numCourses, int[][] prerequisites) {
        List<Integer>[] adj = new ArrayList[numCourses];
        for (int i = 0; i<numCourses; i++){
            adj[i] = new ArrayList<>();
        }

        // indegree : number of directed edges (arrows) pointing into that vertex
        int[] indegree = new int[numCourses];

        for(int[] edge : prerequisites) {
            int from = edge[1];
            int to = edge[0];
            adj[from].add(to);
            indegree[to]++;
        }

        // set the start point.
        int[] order = new int[numCourses]; // use order as queue
        int poll = 0; // taken pointer in queue
        int offer = 0; // ready to take in queue
        for (int i = 0; i < numCourses; i++) {
            if(indegree[i] == 0) {
                order[offer++] = i; // queue offer
            }
        }

        while (poll < offer) { // still have course ready to take, keep looping
            int curr = order[poll++]; // queue poll

            for(int next : adj[curr]) {
                if(--indegree[next] == 0) {
                    order[offer++] = next; // queue offer
                }
            }
        }

        return poll == numCourses ? order : new int[0];
    }

    public int[] findOrder3(int numCourses, int[][] prerequisites) {
        List<Integer>[] adj = new ArrayList[numCourses];
        for (int i = 0; i<numCourses; i++){
            adj[i] = new ArrayList<>();
        }

        for(int[] edge : prerequisites) {
            adj[edge[1]].add(edge[0]);
        }


        int[] order = new int[numCourses];
        int[] idx = new int[]{numCourses-1}; // record from tail
        // 0 = unvisited, 1 = visiting (on current path), 2 = visited (to avoid use as start node to visit the node already go through)
        byte[] state = new byte[numCourses];

        for (int i = 0; i < numCourses; i++) {
            if(state[i] == 0 && hasCycle(i, adj, state, order, idx)) {
                return new int[0];
            }
        }

        return order;
    }

    private boolean hasCycle(int curr, List<Integer>[] adj, byte[] state, int[] order, int[] idx) {
        if(state[curr] == 1) return true; // cycle detected.
        if(state[curr] == 2) return false; // already finished , no need to check the after nodes again.

        state[curr] = 1;
        for (int next : adj[curr]) {
            if(hasCycle(next, adj, state, order, idx)) return true;
        }

        state[curr] = 2;
        order[idx[0]--] = curr; //  fill from the back (from tail to head), because dfs can from start to end go through whole path and state != 0 avoid reuse the path, so it can work
        // idx[0] just use 1 element array to pass idx pointer between recursive, because java not have function like ref int
        return false;
    }
}
