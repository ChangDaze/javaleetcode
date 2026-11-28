package com.leetcode.problems;

public class p0033_search_in_rotated_sorted_array {
    public int search(int[] nums, int target) {
        // it comes up binary search for O(log n)

        // find smallest
        int l = 0;
        int r = nums.length-1;
        while (l < r){
            // move toward the smaller side,

            // because it is ascending order, so the smaller pointer, and it's left would hide the smallest.
            // and if two edge point compare, nums[l] < nums[r] meanings, l is smallest. because l's left also bigger than l.

            // because value distinct so there would not have nums[l] == nums[r] or == nums[middle] case.

            if(nums[l] < nums[r]){
                break;
            }

            int middle = (l+r)/2;
            if(nums[middle] < nums[r]){ // means smallest in l ~ middle, because middle's right all bigger than middle
                r = middle;
            }else { // nums[middle] > nums[r], means smallest in middle ~ right, because it should be ascending, but not means smallest start point hide in there.
                l = middle+1;
                // middle also bigger so it can also abandon
                // and avoid l = middle cause infinite loop like  ex: l = 3 (7), r = 4 (0) => middle = 3 (7) => so l = middle and pointer l and r never move.
            }
        }

        int rotateDegree = l;
        // find target
        l = 0;
        r = nums.length-1;

        while (l <= r) // this should check equal because if only one value in array, it would ski[ this while and return -1 directly
        {
            int middle = (l+r)/2;
            int realMiddle = (middle + rotateDegree) % nums.length;
            // ex : (0 + rotateDegree) % nums.length = rotateDegree
            //  0 % 7 = 0, 6 % 7 = 6, 7 % 7 = 0 also mean it go through whole array and back to the start point.
            if(nums[realMiddle] == target){
                return realMiddle; //return the index of target
            }

            if(nums[realMiddle] > target){
                r = middle-1; // we have target so we can skip the point we don't need
            }else {
                l = middle+1; // we have target so we can skip the point we don't need
            }
        }


        return -1;
    }

    public int search2(int[] nums, int target) {
        // it comes up binary search for O(log n)

        // find smallest
        int l = 0;
        int r = nums.length-1;
        while (l < r){
            // move toward the smaller side,

            // because it is ascending order, so the smaller pointer, and it's left would hide the smallest.
            // and if two edge point compare, nums[l] < nums[r] meanings, l is smallest. because l's left also bigger than l.

            // because value distinct so there would not have nums[l] == nums[r] or == nums[middle] case.

            if(nums[l] < nums[r]){
                break;
            }

            int middle = (l+r)/2;
            if(nums[middle] < nums[r]){ // means smallest in l ~ middle, because middle's right all bigger than middle
                r = middle;
            }else { // nums[middle] > nums[r], means smallest in middle ~ right, because it should be ascending, but not means smallest start point hide in there.
                l = middle+1;
                // middle also bigger so it can also abandon
                // and avoid l = middle cause infinite loop like  ex: l = 3 (7), r = 4 (0) => middle = 3 (7) => so l = middle and pointer l and r never move.
            }
        }

        int rotateDegree = l;
        // find target

        /*
        "First I search for the pivot,
         then I narrow the range to the one sorted half that can contain target,
          and run a standard binary search."
          = pruning
        * */

        int last = nums.length -1;
        if ( target <= nums[last]) { // target smaller than last, means only need check rotateDegree ~  last
            l = rotateDegree;
            r = last;
        } else { // target bigger than last, means only need check 0 ~  rotateDegree - 1 (-1 for not equal not need to check)
            l = 0;
            r = rotateDegree -1;
        }

        while (l <= r) // this should check equal because if only one value in array, and we don't check equal, it would skip this while loop and return -1 directly
        {
            int middle = l + (r - l) / 2; // avoid int overflow on huge arrays
            // and with pruning we also can run binary search on normal ascending array scope, no need to care rotateDegree anymore
            if(nums[middle] == target){
                return middle; //return the index of target
            }

            if(nums[middle] > target){
                r = middle-1; // we have target so we can skip the point we don't need
            }else {
                l = middle+1; // we have target so we can skip the point we don't need
            }
        }


        return -1;
    }

    public int search3(int[] nums, int target) {
        int l = 0;
        int r = nums.length - 1;

        while ( l <= r) {
            int mid = l + (r - l) / 2; // int with dividend would go closer to smaller part.

            if (nums[mid] == target) { // if lucky, we can return directly.
                return mid;
            }

            // equal to nums[mid] check still need to do, because ignore mid would cause some edge case false.
            // ex: [3, 1], mid is index 0, but not check equal mid than go to else side and set right side is sorted,
            // which means 3 (mid) ~ 1 (r) sorted, which is definitely false.

            if (nums[l] <= nums[mid]) { // means no pivot point , left half is sorted
                if (nums[l] <= target && target < nums[mid]) { // check if target in l ~ mid
                    r = mid - 1; // -1 for mid not equal target
                } else {
                    l = mid + 1; // because left half is sorted and not include target, we can give up all left half directly
                }
            } else { // nums[l] > nums[mid], l ~ mid have pivot point. and also mean right half sorted.
                if (nums[mid] < target && target <= nums[r]) { // if include target, we can retain right part
                    l = mid + 1;
                } else { // if not include target, we also can give up whole right part directly.
                    r = mid - 1;
                }
            }

            // the core concept is find sorted part and judge if retain or give up that sorted part.
        }

        return  -1;
    }

    public int search4(int[] nums, int target) {
        int l = 0;
        int r = nums.length - 1;

        // Splits into two sorted groups around the pivot: the left group and the right group.
        // group is base on "pivot point" not on mid.
        boolean targetOnRight = target < nums[0]; // target belong right group, and it never changes in while loop.


        while ( l <= r) {
            int mid = l + (r - l) / 2;

            // nums[0] be checked point, because any point bigger than nums[0] belong to left and make left sorted.
            // because nums[0] should always be the smallest point in left group.
            boolean midOnRight = nums[mid] < nums[0]; // mid belong right group

            int val;
            if (midOnRight == targetOnRight) // mid and target all on left group or right group
            {
                val = nums[mid];
            } else if (targetOnRight) { // target on right and mid is on left, we should push scope to right, so mid should be small enough
                val = Integer.MIN_VALUE;
            }else { // target on left and mid is on right, we should push scope to left, so mid should be big enough
                val = Integer.MAX_VALUE;
            }

            // Binary search that ignores mid's true value and chooses which way to go based on the representative element from each group.
            if ( val == target) {
                return mid;
            } else if (val > target) { // means target at mid's right we should go right.
                r = mid - 1;
            }else { // means target at mid's left we should go left.
                l = mid + 1;
            }
        }

        return  -1;
    }
}
