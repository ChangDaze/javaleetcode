package com.leetcode.problems;

public class p0643_maximum_average_subarray_i {

    public double findMaxAverage(int[] nums, int k) {
        int current = 0;

        for (int i = 0; i < k; i++) {
            current += nums[i];
        }

        int max = current;

        for (int fast = k, slow = 0; fast < nums.length; fast++, slow++) {
            current = current + nums[fast] - nums[slow];
            if (current > max) {
                max = current;
            }
        }

        return (double) max / k;
    }
}
