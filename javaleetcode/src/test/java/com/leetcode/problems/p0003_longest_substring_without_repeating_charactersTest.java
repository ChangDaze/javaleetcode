package com.leetcode.problems;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class p0003_longest_substring_without_repeating_charactersTest {
    @Test
    void testExample1() {
        p0003_longest_substring_without_repeating_characters solution = new p0003_longest_substring_without_repeating_characters();

        String s = "abcabcbb";
        int expected = 3;

        int actual = solution.lengthOfLongestSubstring(s);

        assertEquals(expected, actual);
    }
    @Test
    void testExample2() {
        p0003_longest_substring_without_repeating_characters solution = new p0003_longest_substring_without_repeating_characters();

        String s = "bbbbb";
        int expected = 1;

        int actual = solution.lengthOfLongestSubstring(s);

        assertEquals(expected, actual);
    }
    @Test
    void testExample3() {
        p0003_longest_substring_without_repeating_characters solution = new p0003_longest_substring_without_repeating_characters();

        String s = "pwwkew";
        int expected = 3;

        int actual = solution.lengthOfLongestSubstring(s);

        assertEquals(expected, actual);
    }

    @Test
    void testExample2_1() {
        p0003_longest_substring_without_repeating_characters solution = new p0003_longest_substring_without_repeating_characters();

        String s = "abcabcbb";
        int expected = 3;

        int actual = solution.lengthOfLongestSubstring2(s);

        assertEquals(expected, actual);
    }
    @Test
    void testExample2_2() {
        p0003_longest_substring_without_repeating_characters solution = new p0003_longest_substring_without_repeating_characters();

        String s = "bbbbb";
        int expected = 1;

        int actual = solution.lengthOfLongestSubstring2(s);

        assertEquals(expected, actual);
    }
    @Test
    void testExample2_3() {
        p0003_longest_substring_without_repeating_characters solution = new p0003_longest_substring_without_repeating_characters();

        String s = "pwwkew";
        int expected = 3;

        int actual = solution.lengthOfLongestSubstring2(s);

        assertEquals(expected, actual);
    }
}
