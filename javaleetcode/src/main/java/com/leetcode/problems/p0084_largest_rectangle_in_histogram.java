package com.leetcode.problems;

import java.util.ArrayDeque;
import java.util.Deque;

public class p0084_largest_rectangle_in_histogram {
    public int largestRectangleArea(int[] heights) {
        // [0] = height, [1] = span
        Deque<int[]> stack = new ArrayDeque<>();
        int[] leftSpan = new int[heights.length];
        int[] rightSpan = new int[heights.length];
        // loop 1, collect leftSpan
        for (int i = 0; i < heights.length; i++) {
            int span = 1;
            while (!stack.isEmpty() && heights[i] <= stack.peek()[0]){
                span += stack.pop()[1];
            }
            stack.push(new int[]{heights[i], span});
            leftSpan[i] = span;
        }
        // loop 2, collect rightSpan
        stack.clear();
        for (int i = heights.length - 1; i >= 0; i--) {
            int span = 1;
            while (!stack.isEmpty() && heights[i] <= stack.peek()[0]){
                span += stack.pop()[1];
            }
            stack.push(new int[]{heights[i], span});
            rightSpan[i] = span;
        }
        // look like loop 1 and loop 2 can run at the same time because they use two pointer at the same loop.
        // loop 3, collect max area
        int maxArea = 0;
        for (int i = 0; i < heights.length; i++) {
            int area = heights[i] * (leftSpan[i]+rightSpan[i]-1);
            if (area > maxArea) {
                maxArea = area;
            }
        }

        return maxArea;
    }

    public int largestRectangleArea2(int[] heights) {
        int[] lessFromLeft = new int[heights.length];
        int[] lessFromRight = new int[heights.length];
        // loop 1, collect record from left
        for (int i = 0; i < heights.length; i++) {
            int p = i - 1; // first compare with first left
            while (p >= 0 && heights[i] <= heights[p]){ // compare to pointer value
                p = lessFromLeft[p]; // find out can keep find next, use p to jump to next
            }
            lessFromLeft[i] = p; // record the ending point
        }
        // loop 2, collect record from right
        for (int i = heights.length - 1; i >= 0; i--) {
            int p = i + 1; // first compare with first right
            while (p < heights.length && heights[i] <= heights[p]){ // compare to pointer value
                p = lessFromRight[p]; // find out can keep find next, use p to jump to next
            }
            lessFromRight[i] = p; // record the ending point
        }
        // look like loop 1 and loop 2 can run at the same time because they use two pointer at the same loop.
        // loop 3, collect max area
        int maxArea = 0;
        for (int i = 0; i < heights.length; i++) {
            int area = (lessFromRight[i] - lessFromLeft[i] - 1) * heights[i]; // must -1 because the boundary is excluded.
            if (area > maxArea) {
                maxArea = area;
            }
        }

        return maxArea;
    }

    public int largestRectangleArea3(int[] heights) {
        // store index
        Deque<Integer> stack = new ArrayDeque<>();

        int maxArea = 0;

        for(int i = 0; i<heights.length; i++){
            // check peek value , decide peek value encounter the limit then pop out.
            // if value <= current height means peek value can extend to next index to check.
            // peek <= current height also means, it is the left boundary for current height and put into stack to find right boundary.
            while (!stack.isEmpty() && heights[stack.peek()] > heights[i])
            {
                int popIndex = stack.pop();
                int left = stack.isEmpty() ? -1 : stack.peek(); // the left boundary, stack index which peek must <= pop.
                int area = heights[popIndex] * (i - left - 1); // -1 for exclude the boundary
                if(maxArea < area){
                    maxArea = area;
                }
            }
            stack.push(i);
        }

        // some value extend to the end so need check them all
        while (!stack.isEmpty()) {
            int popIndex = stack.pop();
            int left = stack.isEmpty() ? -1 : stack.peek(); // the left boundary, stack index which peek must <= pop.
            int area = heights[popIndex] * (heights.length - left - 1); // -1 for exclude the boundary
            if(maxArea < area){
                maxArea = area;
            }
        }

        return maxArea;
    }

    public int largestRectangleArea4(int[] heights) {
        // use pointer to simulate the stack.
        // store index
        int[] stack = new int[heights.length+1]; // +1 for the wrap up together in loop, but int[] stack is not correspond to index i, just avoid all same height, it just a limited stack simulator.
        int top = -1; // -1 means the stack is empty

        int maxArea = 0;

        for(int i = 0; i<=heights.length; i++){
            // check peek value , decide peek value encounter the limit then pop out.
            // if value <= current height means peek value can extend to next index to check.
            // peek <= current height also means, it is the left boundary for current height and put into stack to find right boundary.
            int currentHeight = (i==heights.length) ? 0 : heights[i]; // use the virtual height to force wrap up the stack.
            while (top != -1 && heights[stack[top]] > currentHeight) // use currentHeight avoid out of index when wrap up
            {
                int popIndex = stack[top--];
                int left = top == -1 ? -1 : stack[top]; // the left boundary, stack index which peek must <= pop.
                int area = heights[popIndex] * (i - left - 1); // -1 for exclude the boundary
                if(maxArea < area){
                    maxArea = area;
                }
            }
            stack[++top] = i;
        }

        return maxArea;
    }
}
