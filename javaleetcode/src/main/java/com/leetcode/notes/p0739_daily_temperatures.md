# 739. Daily Temperatures

### 1. Progressive Evolution of Monotonic Stack & Jump Approaches
- **Core Problem**: Given an array of daily `temperatures`, return an array `result` where `result[i]` is the number of days you have to wait after the `i`-th day to get a **strictly warmer** temperature (`temperatures[j] > temperatures[i]` with minimal `j > i`), or `0` if no such future day exists.
- **Key Algorithmic Insight**:
  1. **Why Brute Force is $O(n^2)$**: Scanning forward day-by-day for each `i` repeatedly checks the same cooler days.
  2. **Monotonic Stack Pruning ($O(n)$)**: Any day that is cooler than or equal to a more recent day is "shadowed" and can never be the next warmer day for earlier days—so we can permanently discard (`pop`) it.
  3. **Implicit Stack via Jump Pointers ($O(1)$ Space)**: When iterating right-to-left, `result[]` already stores the jump distance to the next warmer day for all future indices `j > i`, allowing us to jump directly over cooler valleys without a stack.
- **Comparison of the Implementations**:

| Implementation | Direction | Data Structure | Extra Space | Key Limitation / Advantage |
| :--- | :--- | :--- | :--- | :--- |
| **`dailyTemperatures`** | Right $\rightarrow$ Left | `Deque<int[]>` (`{temp, index}`) | $O(n)$ | First working version; allocates $n$ `int[2]` tuple objects on the heap and uses an inner `if` + `break`. |
| **`dailyTemperatures2`** | Right $\rightarrow$ Left | `Deque<Integer>` (`index` only) | $O(n)$ | **Index-Only Optimization**: Eliminates `int[2]` array allocations because `temperatures[stack.peek()]` gives the temperature in $O(1)$. |
| **`dailyTemperatures3`** | Left $\rightarrow$ Right | `Deque<Integer>` (`index` only) | $O(n)$ | **Fewer Branches**: Stack holds unresolved days; popping and assigning `result[prevDay] = i - prevDay` happen in a single `while` condition with no extra `if`. |
| **`dailyTemperatures4`** | Right $\rightarrow$ Left | None (`result[]` jump pointers) | **$O(1)$** | **Optimal Backward Jump**: Reuses `result[]` as an implicit monotonic stack with path compression; zero heap/boxing overhead, 5x–10x faster on CPU. |

---

### 2. Deep Dive into Each Approach

#### A. `dailyTemperatures` & `dailyTemperatures2`: Right-to-Left Monotonic Stack
- **How It Works**:
  - Iterate backward from the last day (`i = n - 1` down to `0`).
  - The stack maintains future days in **strictly increasing order of temperature** from top to bottom.
  - For day `i`, any future day on top of the stack with `temperatures[top] <= temperatures[i]` is useless (day `i` is both **earlier** and **at least as warm**, so earlier days `< i` will always prefer day `i` over `top`) $\rightarrow$ `stack.pop()`.
- **Why Storing Only the Index (`dailyTemperatures2`) is Better than `int[]` (`dailyTemperatures`)**:
  - Given index `idx`, `temperatures[idx]` is a direct $O(1)$ array lookup.
  - Moving `temperatures[stack.peek()] <= temperatures[i]` directly into the `while` condition removes the inner `if (...) break;` block:
    ```java
    while (!stack.isEmpty() && temperatures[stack.peek()] <= temperatures[i]) {
        stack.pop();
    }
    if (!stack.isEmpty()) {
        result[i] = stack.peek() - i;
    }
    stack.push(i);
    ```

#### B. `dailyTemperatures3`: Left-to-Right Monotonic Stack ("Fewer Branches")
- **Why Right-to-Left Needs Two Conditional Checks**:
  - In Right-to-Left, **popping** (discarding cooler future days) and **recording the answer** (peeking at the first warmer day) are two separate events, requiring both a `while` condition and a separate `if (!stack.isEmpty())` check.
- **Why Left-to-Right Needs Only One Check**:
  - Going Left-to-Right (`i = 0` to `n - 1`), the stack holds indices of past days **still waiting** for a warmer day (monotonically decreasing temperatures).
  - The moment today (`temperatures[i]`) is warmer than `temperatures[stack.peek()]`, **popping and recording the answer are the exact same event**:
    ```java
    while (!stack.isEmpty() && temperatures[i] > temperatures[stack.peek()]) {
        int prevDay = stack.pop();
        result[prevDay] = i - prevDay;
    }
    stack.push(i);
    ```
  - Any days still left in the stack at the end have no warmer future day and naturally remain `0` in `result[]`.

#### C. `dailyTemperatures4`: Backward Jump with $O(1)$ Extra Space
- **How It Works Without a Stack**:
  - Iterate backward (`i = n - 1` down to `0`) while tracking `hottest` seen so far from the right.
  - **Guard Case (`temperatures[i] >= hottest`)**: If today is at least as warm as the hottest future day, no warmer day exists (`result[i]` stays `0`). Updating `hottest = temperatures[i]` and calling `continue` is **mandatory**—otherwise, hitting a future day with `result[...] == 0` inside the `while` loop would add `0` to `days` and cause an infinite loop!
  - **Jump Loop**: Start at `days = 1` (tomorrow, `i + 1`). While `temperatures[i + days] <= temperatures[i]`, jump directly to the next warmer day after `i + days` via:
    ```java
    days += result[i + days];
    ```
- **Visual Trace of Jumping (`[75, 73, 69, 71, 76]`)**:
  ```text
  Index:        0     1     2     3     4
  Temp:       [75,   73,   69,   71,   76]
                            │     ▲     ▲
                            └─────┘     │
                          +result[2]    │
                                  └─────┘
                                +result[3]
                      │                 ▲
                      └─────────────────┘
                    result[1]=3 (builds bridge over 2 & 3!)
  ```
  1. At `i = 1` (`73°`): checks index `2` (`69° <= 73°`) $\rightarrow$ jumps by `result[2]` to index `3` (`71° <= 73°`) $\rightarrow$ jumps by `result[3]` to index `4` (`76° > 73°`). Sets **`result[1] = 3`**.
  2. At `i = 0` (`75°`): checks index `1` (`73° <= 75°`) $\rightarrow$ jumps by `result[1]` (`+3`) **straight from index `1` to index `4` (`76°`)**, completely bypassing indices `2` and `3`!

#### D. Deep Insight: Is Backward Jump "Dynamic Programming" or "Monotonic Stack"?
- **Informally (DP Intuition)**: It looks like 1D bottom-up DP because it solves `result[i]` from right to left by reusing already-computed answers `result[i + days]`.
- **Strictly (Implicit Monotonic Stack + Path Compression)**:
  - Textbook DP optimizes a choice (`min`/`max`/`sum`) via a recurrence formula. Here, there is no choice—`result[j]` is a **next-warmer pointer**.
  - Notice that when `dailyTemperatures2` is at index `i`, the elements sitting in `stack` are `[i + 1, (i + 1) + result[i + 1], ...]`.
  - Thus, `days += result[i + days]` walks the **exact same chain of peaks** as `stack.pop()`, and setting `result[i] = days` performs **Path Compression** (building a shortcut bridge over all cooler days so no earlier index ever visits them again).
- **Why Backward Jump is 5x–10x Faster in Practice**:
  - **Identical step count**: Because every jumped-over index is bridged by the new `result[i]`, each index is jumped over **at most once** across the entire algorithm (total `while` steps $\le n$, identical to total `stack.pop()` calls).
  - **Zero object overhead & L1 cache locality**: Avoids `ArrayDeque` allocation and `Integer` autoboxing/unboxing, operating purely on contiguous primitive `int[]` arrays.

---

### 3. Java vs. C# Tuple & Stack Mapping

When storing pairs like `(temperature, index)` in a stack or collection, Java and C# differ significantly:

| Concept | C# (.NET) | Java | Works as `HashSet` / `HashMap` Key? | Notes |
| :--- | :--- | :--- | :--- | :--- |
| **Stack Type** | `Stack<T>` | `Deque<T> = new ArrayDeque<>()` | N/A | Never use legacy `java.util.Stack` (synchronized `Vector` overhead); always use `ArrayDeque`. |
| **Named Value Tuple** | `(int temp, int idx)` (`ValueTuple`) | `record Pair(int temp, int idx) {}` | **Yes** (value-based `equals` & `hashCode`) | Java 16+ `record` can be declared locally inside a method and auto-generates accessors, `equals`, and `hashCode`. |
| **Built-in Pair Class** | `Tuple<int, int>` / `KeyValuePair<K, V>` | `Map.Entry<Integer, Integer>` (`Map.entry(a, b)`) | **Yes** | Standard `java.util` 2-tuple, but boxes primitives into `Integer`. |
| **Primitive 2-Element Array** | `int[] { a, b }` | `new int[] { a, b }` | **NO!** (uses reference/memory equality!) | Fast for `Deque` / `PriorityQueue`, but **never** use `int[]` as a `HashSet` or `HashMap` key in Java! |

---

### 4. Real-World Implementation & Use Cases
1. **Stock Trading Backtesting ("How many days until a trade becomes profitable?")**:
   - **Scenario**: Replace `temperatures` with daily stock prices: `prices = [73, 74, 75, 71, 69, 72, 76, 73]`.
   - Suppose a trader buys on Day `2` at `\$75` and sets a **Limit Sell Order** to sell as soon as the price goes strictly above `\$75`.
   - On Days `3, 4, 5` (`\$71, \$69, \$72`), the trader is underwater (losing money) and must wait. On Day `6` (`\$76`), the order triggers (`6 - 2 = 4` days wait).
   - When a trading platform backtests a strategy across 100,000 historical price ticks, it runs `dailyTemperatures3` (or `dailyTemperatures4`) to compute the **holding time to profit for every possible buy day in one $O(n)$ pass**:
     - Left-to-Right stack (`dailyTemperatures3`) = **active limit orders still waiting in the order book**. When today's price `prices[i]` rises, all waiting orders below `prices[i]` are popped and filled at once!
2. **Server Monitoring & SLA Recovery Time (Grafana / Datadog Time-Series)**:
   - **Scenario**: A server logs memory usage or error rate every minute. When a spike happens at minute `i`, engineers want to know: *"How many minutes did it take for the metric to drop below `metric[i]` (recover)?"*
   - Using the exact same algorithm (just flipping `>` to `<`), monitoring dashboards compute recovery durations across millions of data points in milliseconds ($O(n)$) instead of freezing with nested loops ($O(n^2)$).
3. **2D Terrain / Building Line-of-Sight in Games**:
   - **Scenario**: Imagine buildings of heights `[73, 74, 75, 71, 69, 72, 76, 73]` in a 2D side-view game.
   - If a player stands on the roof of building `2` (height `75`) looking right, shorter buildings (`71, 69, 72`) do not block their view of the sky—the **first taller building (`76` at index `6`)** is where their horizontal line-of-sight hits a wall (`distance = 4`).

---

### 5. Complexity
- **`dailyTemperatures`, `dailyTemperatures2`, `dailyTemperatures3` (Monotonic Stack)**:
  - **Time Complexity**: $O(n)$ — Each index `0..n-1` is pushed onto the stack once and popped at most once ($\le 2n$ stack operations).
  - **Space Complexity**: $O(n)$ — The `ArrayDeque` holds up to $n$ elements in the worst case (e.g., strictly decreasing temperatures).
- **`dailyTemperatures4` (Backward Jump)**:
  - **Time Complexity**: $O(n)$ — Each index is jumped over via `days += result[i + days]` at most once before being bypassed by path compression ($\le n$ total inner-loop jumps).
  - **Space Complexity**: $O(1)$ auxiliary space — Uses no extra data structures beyond the required output array `result[]`.
