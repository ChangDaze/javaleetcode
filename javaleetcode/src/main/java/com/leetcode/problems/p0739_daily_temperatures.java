package com.leetcode.problems;

import java.util.ArrayDeque;
import java.util.Deque;

public class p0739_daily_temperatures {
    public int[] dailyTemperatures(int[] temperatures) {
        int[] result = new int[temperatures.length];
        Deque<int[]> stack = new ArrayDeque<>();
        for (int i = temperatures.length - 1; i >= 0; i--) {
            while (stack.size() > 0) {
                int[] temperature = stack.peek();

                if(temperature[0] > temperatures[i]) {
                    result[i] = temperature[1] - i;
                    break;
                }

                stack.pop(); // only care about the nearest higher temperature, so lower temperature all pop out at not need to store, and at the end push in current temperature
            }
            stack.push(new int[] {temperatures[i], i});
        }

        return result;
    }

    public int[] dailyTemperatures2(int[] temperatures) {
        int[] result = new int[temperatures.length];
        Deque<Integer> stack = new ArrayDeque<>();
        for (int i = temperatures.length - 1; i >= 0; i--) {
            while (!stack.isEmpty() && temperatures[stack.peek()] <= temperatures[i]) // only store index and do judgement at while clause make code cleaner
            {
                stack.pop(); // only care about the nearest higher temperature, so lower temperature all pop out at not need to store, and at the end push in current temperature
            }

            if(!stack.isEmpty()) { // store the value in need
                result[i] = stack.peek() - i;
            }

            stack.push(i); // stack would be monotonic higher for indexes which earlier push in and keep stay in stack
        }

        return result;
    }

    public int[] dailyTemperatures3(int[] temperatures) {
        int[] result = new int[temperatures.length];
        Deque<Integer> stack = new ArrayDeque<>();
        for (int i = 0; i < temperatures.length; i++) {
            // from left to right
            // only retain value higher than current temperature in the stack (stack would be monotonic higher for indexes which earlier push in and keep stay in stack)
            while (!stack.isEmpty() && temperatures[i] > temperatures[stack.peek()])
            {
                // when meet the higher temperature, filter out and update value for lower temperature
                int prevDay = stack.pop();
                result[prevDay] = i - prevDay;
            }
            stack.push(i);
        }

        return result;
    }

    public int[] dailyTemperatures4(int[] temperatures) {
        int[] result = new int[temperatures.length];
        int hottest = 0;
        for (int i = temperatures.length - 1; i >= 0; i--) {
            if (temperatures[i] >= hottest) {
                hottest = temperatures[i]; // if the hottest, means result[i] = 0
                continue; // and if hottest one not stop at this line (continue to next loop), the code would break, because days = 0 cause infinite loop
            }

            int days = 1; // not hottest, means at least 1 day diff and need assure not hottest
            while (temperatures[i + days] <= temperatures[i]) {
                days += result[i + days]; // because the target not enough, so it use the target's result to jump to next nearest higher temperature, like dp
                // and because backward jump, use nearest higher value (for compare target), so actually they loop same times, but backward jump may faster because it uses pure array
            }
            result[i] = days;
        }
        return result;
    }
}
