package com.leetcode.problems;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class p0901_online_stock_span {
    public static class StockSpanner {
        // int[] have two element, first is price, second is the span.
        // span at least 1 because it includes today.
        private final List<int[]> list = new ArrayList<>();

        public StockSpanner() {

        }

        public int next(int price) {

            int[] base;
            int baseIndex;

            // defend empty list.
            if(list.isEmpty()) {
                list.add(new int[] {price, 1});
                return 1;
            }

            base = list.getLast();
            baseIndex = list.size() - 1;

            while(base[0] <= price) { // end at the point which bigger than price
                // find next potential bigger point
                baseIndex -= base[1];
                if(baseIndex < 0) { // find out no next potential bigger point
                    list.add(new int[] {price, list.size()}); // find out in this point the moment highest point store the span - 1 not span
                    return list.size(); // after add list , list.size be the actual span, so for moment highest point int[1] is not span but not moment highest still store span
                    // that is the reason why this method still work.
                }
                base = list.get(baseIndex);
            }

            // find out span
            int span = list.size() - baseIndex;
            list.add(new int[] {price, span});
            return span;
        }
    }

    public static class StockSpanner2 {
        // int[] have two element, first is price, second is the span.
        // span at least 1 because it includes today.
        private final List<int[]> list = new ArrayList<>();

        public StockSpanner2() {

        }

        // compare to StockSpanner, code cleaner a lot.
        public int next(int price) {

            int baseIndex = list.size() - 1;

            while(baseIndex >= 0 && list.get(baseIndex)[0] <= price) { // end at no bigger point or the point which bigger than price
                // find next potential bigger point by span
                baseIndex -= list.get(baseIndex)[1];
            }

            // find out span
            // because when end at baseIndex < 0 (no bigger point) means baseIndex = -1.
            // because like day 0 (index), span = 1. and like day 1 still biggest point, baseInex = last point index (0) - span (1) = -1.
            // so for day 1 span be 2, and also work ok all after biggest point span be current list.size + 1
            // and when these moment biggest point use its span to find next biggest point and find out no biggest point after.
            // would be that point index - its span = -1, so baseIndex < 0 situation, baseIndex definitely be -1
            int span = list.size() - baseIndex;
            list.add(new int[] {price, span});
            return span;
        }
    }

    public static class StockSpanner3 {
        // int[] have two element, first is price, second is the span.
        private final Deque<int[]> stack = new ArrayDeque<>();

        public StockSpanner3() {

        }

        public int next(int price) {
            int span = 1; // span at least 1 because it includes today.

            while(!stack.isEmpty() && stack.peek()[0] <= price) { // end at no bigger point or the point which bigger than price
                // find next potential bigger point by span
                span += stack.pop()[1];
                // why it can pop out ? because in future days if price higher than this input price,
                // also means it higher than this pop out point, so in the future, if new price higher than this input point,
                // also no need to compare to this pop out point again.
            }

            stack.push(new int[]{price, span});
            return span;
        }
    }
}
