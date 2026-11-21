package com.leetcode.problems;

import java.util.*;

public class p0207_course_schedule {
    public boolean canFinish(int numCourses, int[][] prerequisites) {
        List<Integer>[] records = new ArrayList[numCourses];

        // initialize
        for (int i = 0 ; i < records.length; i++)
        {
            records[i] = new ArrayList<>();
        }

        // list the prerequisites for each course
        for(int i = 0; i < prerequisites.length; i++){
            int target = prerequisites[i][0];
            int prerequisite = prerequisites[i][1];
            records[target].add(prerequisite);
        }

        // take course use loop

        // collection for checked prerequisite
        HashSet<Integer> checked = new HashSet<>();
        while (true){
            boolean change = false;

            for (int i = 0 ; i < records.length; i++){
                if(records[i].isEmpty() && !checked.contains(i)){
                    for (int j = 0 ; j < records.length; j++)
                    {
                        if(records[j].contains(i)){ // can directly use remove to get true or false
                            records[j].remove(Integer.valueOf(i)); // use primitive int would be seen as index
                            change = true;
                        }
                    }
                    checked.add(i);
                }
            }

            if(!change){
                break;
            }
        }

        // check result
        for (int i = 0 ; i < records.length; i++)
        {
            if(!records[i].isEmpty()){
                return false;
            }
        }

        return true;
    }

    public boolean canFinish2(int numCourses, int[][] prerequisites) {
        // initialize

        // adj is short for adjacency list on graph, like after take this course it unlock which next level courses.
        List<Integer>[] adj = new ArrayList[numCourses];
        for (int i = 0 ; i < numCourses; i++)
        {
            adj[i] = new ArrayList<>();
        }
        int[] indegree = new int[numCourses];

        // edge means the connection between nodes.
        for(int[] edge : prerequisites) {
            int target = edge[0];
            int prereq = edge[1];
            adj[prereq].add(target);
            indegree[target]++;
        }

        // use queue to control if the graph can keep go down
        // like the course can take now but not taken before.
        Queue<Integer> queue= new ArrayDeque<>();
        for (int i = 0; i < numCourses; i++) {
            if(indegree[i] == 0) {
                queue.offer(i);
            }
        }

        int taken = 0; // the courses already take.
        while (!queue.isEmpty()) {
            int curr = queue.poll();
            taken++; // take the course and unlock the adjacency course.
            for (int next : adj[curr]) {
                if (--indegree[next] == 0) { // when the course's prerequisites are all unlocked, put it into queue at the moment.
                    queue.offer(next);
                }
            }
        }

        return taken == numCourses; // check if all course be taken.
    }

    public boolean canFinish3(int numCourses, int[][] prerequisites) {
        // initialize
        List<Integer>[] adj = new ArrayList[numCourses];
        for (int i = 0 ; i < numCourses; i++)
        {
            adj[i] = new ArrayList<>();
        }
        for(int[] edge : prerequisites) {
            adj[edge[1]].add(edge[0]);
        }

        // 0 = unvisited, 1 = visiting (on current path), 2 = visited (to avoid use as start node to visit the node already go through)

        // record the state for each node.
        byte[] state = new byte[numCourses];

        for (int i = 0; i < numCourses; i++) {
            // the check point is all node visited == all courses be taken.
            // and if it has cycle means it doesn't have chance to take all course.
            if(state[i] == 0 && hasCycle(i, adj, state)) {
                return false;
            }
        }
        return true;
    }

    /// go through the whole graph with DFS to check if it has cycle.
    private boolean hasCycle(int curr, List<Integer>[] adj, byte[] state){
        if (state[curr] == 1) return true; // on current path trip, the node be visited means the cycle
        if (state[curr] == 2) return false;

        state[curr] = 1; // use as break flag when recursive.
        for(int next : adj[curr]) { // use adjacent list to go down the graph
            if(hasCycle(next, adj, state)) {
                return true;
            }
        }

        state[curr] = 2; // when finish one of the path in the graph successfully, tag each node on the path as visited.
        return false;
    }
}
