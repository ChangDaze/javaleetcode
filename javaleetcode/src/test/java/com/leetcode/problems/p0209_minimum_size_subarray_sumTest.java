package com.leetcode.problems;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class p0209_minimum_size_subarray_sumTest {
    @Test
    void testExample1() {
        p0209_minimum_size_subarray_sum solution = new p0209_minimum_size_subarray_sum();

        int target = 7;
        int[] nums = new int[]{2,3,1,2,4,3};
        int expected = 2;

        int actual = solution.minSubArrayLen(target, nums);

        assertEquals(expected, actual);
    }
    @Test
    void testExample2() {
        p0209_minimum_size_subarray_sum solution = new p0209_minimum_size_subarray_sum();

        int target = 4;
        int[] nums = new int[]{1,4,4};
        int expected = 1;

        int actual = solution.minSubArrayLen(target, nums);

        assertEquals(expected, actual);
    }
    @Test
    void testExample3() {
        p0209_minimum_size_subarray_sum solution = new p0209_minimum_size_subarray_sum();

        int target = 11;
        int[] nums = new int[]{1,1,1,1,1,1,1,1};
        int expected = 0;

        int actual = solution.minSubArrayLen(target, nums);

        assertEquals(expected, actual);
    }
    @Test
    void testExample4() {
        p0209_minimum_size_subarray_sum solution = new p0209_minimum_size_subarray_sum();

        int target = 11;
        int[] nums = new int[]{1,2,3,4,5};
        int expected = 3;

        int actual = solution.minSubArrayLen(target, nums);

        assertEquals(expected, actual);
    }
}
