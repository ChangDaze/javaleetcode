package com.leetcode.problems;

public class p0209_minimum_size_subarray_sum {
    public int minSubArrayLen(int target, int[] nums) {
        int result = Integer.MAX_VALUE;
        int windowSum = 0;
        int left = 0;

        for (int right = 0; right < nums.length; right++) {
            windowSum += nums[right];

            // because only have positive num.
            // when meet the requirement, continuously record valid window length, then shrink from the left
            while (windowSum >= target) {
                result = Math.min(result, right - left + 1);
                windowSum -= nums[left++];
            }
        }

        return result == Integer.MAX_VALUE ? 0 : result;
    }
}
