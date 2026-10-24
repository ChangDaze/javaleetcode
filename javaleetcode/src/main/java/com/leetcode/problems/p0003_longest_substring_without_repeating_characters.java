package com.leetcode.problems;

import java.util.HashSet;

public class p0003_longest_substring_without_repeating_characters {
    public int lengthOfLongestSubstring(String s) {
        int max = 0;
        HashSet<Character> currentSet = new HashSet<Character>();


        for(int fast = 0, slow = 0; fast < s.length(); fast++){
            while (currentSet.contains(s.charAt(fast))){
                currentSet.remove(s.charAt(slow));
                slow++;
            }
            currentSet.add(s.charAt(fast));
            if(currentSet.size() > max){
                max = currentSet.size();
            }
        }

        return max;
    }

    public int lengthOfLongestSubstring2(String s) {
        int max = 0;
        int[] records = new int[128];

        for(int fast = 0, slow = 0; fast < s.length(); fast++){
            char c = s.charAt(fast);
            slow = Math.max(slow, records[c]); // because only can go forward, so only have options on current slow pointer or the last time same char appear place + 1 (we are calculating fast pointer, so need +1)

            max = Math.max(max, fast - slow + 1);
            records[c] = fast + 1;  // record the last time same char appear place + 1
        }

        return max;
    }
}
