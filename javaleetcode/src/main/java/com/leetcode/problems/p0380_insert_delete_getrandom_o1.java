package com.leetcode.problems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class p0380_insert_delete_getrandom_o1 {
    public static class RandomizedSet {
        //<val, index>
        private HashMap<Integer, Integer> map ;
        private List<Integer> list;

        public RandomizedSet() {
            map = new HashMap<>();
            list = new ArrayList<>();
        }

        public boolean insert(int val) {
            if(map.containsKey(val)) {
                return false;
            }

            list.add(val);
            map.put(val, list.size() - 1);
            return true;
        }

        public boolean remove(int val) {
            if(!map.containsKey(val)) {
                return false;
            }

            // swap the last value to the index, where live the removed value, this method to make the O(1) remove method.
            int swapIndex = map.get(val);
            int swapValue = list.getLast();
            list.set(swapIndex, swapValue);

            // update the swap value map
            map.replace(swapValue, swapIndex);

            // remove the removed value and not used last in list
            map.remove(val);
            list.removeLast();
            return true;
        }

        // it's guaranteed that at least one element exists when this method is called
        public int getRandom() {
            int randomIndex = ThreadLocalRandom.current().nextInt(list.size());
            return list.get(randomIndex);
        }
    }

    public static class RandomizedSet2 {
        //<val, index>
        private final HashMap<Integer, Integer> map = new HashMap<>();
        private final List<Integer> list = new ArrayList<>();

        public boolean insert(int val) {
            if(map.containsKey(val)) {
                return false;
            }

            map.put(val, list.size()); // do this first can don't need use list.size() - 1
            list.add(val);
            return true;
        }

        public boolean remove(int val) {
            // use the return value to reduce the hashmap search times.
            Integer removeIndex = map.remove(val);
            if(removeIndex == null) {
                return false;
            }

            // swap the last value to the index, where live the removed value, this method to make the O(1) remove method.
            // And not used last in list
            int lastVal = list.removeLast();
            if (removeIndex < list.size()) { // only swap if the removed element wasn't already the last element
                list.set(removeIndex, lastVal);
                map.put(lastVal, removeIndex); // put when exist = update map
            }
            return true;
        }

        // it's guaranteed that at least one element exists when this method is called
        public int getRandom() {
            return list.get(ThreadLocalRandom.current().nextInt(list.size()));
        }
    }
}
