package com.leetcode.problems;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class p0643_maximum_average_subarray_iTest {

    @Test
    void testExample1() {
        p0643_maximum_average_subarray_i solution = new p0643_maximum_average_subarray_i();

        int[] nums = {1, 12, -5, -6, 50, 3};
        int k =4;
        double expected = 12.75000;

        double actual = solution.findMaxAverage(nums, k);

        assertEquals(expected, actual, 0.00001);
    }

    @Test
    void testExample2() {
        p0643_maximum_average_subarray_i solution = new p0643_maximum_average_subarray_i();

        int[] nums = {5};
        int k =1;
        double expected = 5.00000;

        double actual = solution.findMaxAverage(nums, k);

        assertEquals(expected, actual, 0.00001);
    }
}