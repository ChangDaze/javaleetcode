# 901. Online Stock Span

### 1. Progressive Evolution of Approaches
- **Core Problem**: Design `StockSpanner` where each `next(int price)` call returns the **span** of the stock's price today—defined as the maximum number of consecutive days (starting from today and going backward) for which the stock price was **less than or equal to** today's price (`price_i <= price_today`).
- **Key Insight (Skip / Consolidate Dominated Days)**:
  - A naive backward scan takes $O(N)$ per call ($O(N^2)$ total).
  - However, if yesterday's price `price_{i-1} <= price_today`, then **every consecutive day that was already `<= price_{i-1}` is also transitively `<= price_today`**.
  - Therefore, instead of stepping back `1` day at a time, we can **jump over** (or **pop**) the entire span `span_{i-1}` in a single $O(1)$ step!
- **Comparison of the Implementations**:

| Implementation | Data Structure | Mechanism | Retains Full History? | Key Limitation / Advantage |
| :--- | :--- | :--- | :--- | :--- |
| **`StockSpanner`** | `ArrayList<int[]>` | Jump-Pointer with early returns (`isEmpty()` & `baseIndex < 0`) | Yes ($O(N)$ space) | First working version; all-time highs store `span - 1` (`index`), causing future jumps to take 1 extra hop through index `0`. |
| **`StockSpanner2`** | `ArrayList<int[]>` | Unified Jump-Pointer (`baseIndex -= span`) | Yes ($O(N)$ space) | **Clean Jump Method**: Always stores true `span`; `baseIndex = -1` naturally unifies empty list, all-time highs, and normal cases into `list.size() - baseIndex`. |
| **`StockSpanner3`** | `ArrayDeque<int[]>` | Monotonic Decreasing Stack (`pop` & accumulate `span`) | No ($O(K)$ peaks) | **Standard Monotonic Stack**: Discards dominated days that will never be needed again, reducing memory footprint on long streams. |
| **Bonus: Flat `int[]`** | `int[] prices`, `int[] spans` | Primitive arrays (Jump or Stack) | Configurable | **Zero Per-Call Allocation**: Avoids `new int[]{price, span}` heap object overhead (`24 bytes` per call). |

---

### 2. Deep Dive into Each Approach

#### A. `StockSpanner`: Initial Jump-Pointer & Two Subtle Details
1. **Why IDE Static Analysis Flagged `while (baseIndex >= 0 && ...)` When `list.get(baseIndex)` Was Unchecked**:
   - In the initial draft:
     ```java
     while (baseIndex >= 0 && base[0] <= price) {
         baseIndex -= base[1];
         base = list.get(baseIndex); // throws IndexOutOfBoundsException if baseIndex < 0!
     }
     ```
   - Because `list.get(baseIndex)` was called **before** the next loop condition check, any `baseIndex < 0` would crash on `list.get(baseIndex)` first. The only way execution could reach the `while` condition again was if `baseIndex >= 0` was already `true`.
   - Guarding `if (baseIndex < 0)` before `list.get(baseIndex)` (or moving `list.get(baseIndex)` into the `while` condition after `baseIndex >= 0`) fixes this.

2. **Why `StockSpanner` Still Passes Every Test Despite Storing `list.size()` (i.e., `span - 1`) on All-Time Highs**:
   - Look at the `baseIndex < 0` branch in `StockSpanner`:
     ```java
     if (baseIndex < 0) { // no previous bigger point exists
         list.add(new int[] {price, list.size()}); // evaluated BEFORE add -> stores k instead of k + 1
         return list.size();                       // evaluated AFTER add  -> returns k + 1 (correct span!)
     }
     ```
   - Suppose day `k` (`k > 0`) is a new all-time high (`price_k >=` all previous prices `0 .. k-1`).
     - Before `list.add(...)`, `list.size()` is `k`. So index `k` stores `[price_k, k]` instead of its true span `k + 1`.
     - Effectively, `int[1]` at an all-time high stores the **jump distance back to index `0`** (`k - 0 = k`) rather than the span!
   - **What happens when a future day `m` has `price_m >= price_k` and jumps from index `k`?**
     1. It subtracts `base[1] = k`: `baseIndex = k - k = 0` (landing on index `0` instead of `-1`).
     2. At index `0`, because `price_k` was already an all-time high (`price_0 <= price_k`) and `price_k <= price_m`, by transitivity **`price_0 <= price_m` is guaranteed to be `true`**.
     3. Index `0` always has `[price_0, 1]` (initialized in the `if (list.isEmpty())` branch), so the loop simply runs **one extra iteration** at index `0`, computes `baseIndex = 0 - 1 = -1`, enters `if (baseIndex < 0)`, and returns the exact right span!

---

#### B. `StockSpanner2`: Unifying All Cases with `baseIndex = -1`
- In `StockSpanner2`, we check `baseIndex >= 0` directly in the `while` condition and let all paths converge at a single formula:
  ```java
  public int next(int price) {
      int baseIndex = list.size() - 1;

      while (baseIndex >= 0 && list.get(baseIndex)[0] <= price) {
          baseIndex -= list.get(baseIndex)[1];
      }

      int span = list.size() - baseIndex;
      list.add(new int[] {price, span});
      return span;
  }
  ```
- **Why `int span = list.size() - baseIndex` Works for Every Case Without `if` Branches**:
  1. **Empty list (`day 0`)**: `list.size()` is `0`, so `baseIndex` starts at `-1`. The `while` loop is skipped, and `span = 0 - (-1) = 1`.
  2. **Normal stop (`baseIndex >= 0`)**: `baseIndex` stops at the first previous day `j` where `price_j > price`. The number of consecutive days in `(j, today]` is `todayIndex - j = list.size() - baseIndex`.
  3. **New all-time high (`baseIndex < 0`)**: Because every previous all-time high at index `i` stored its exact span `i + 1`, jumping from `i` computes `baseIndex = i - (i + 1) = -1`. Thus `baseIndex` is **always `-1`** when no larger previous price exists, and `span = list.size() - (-1) = list.size() + 1`.

---

#### C. `StockSpanner3`: Monotonic Decreasing Stack (`ArrayDeque<int[]>`)
- Notice that once day `i` is jumped over by a later day `today` (`price_i <= price_today`), **no future day will ever stop at or land on day `i` again**—`today` is both more recent and has a price `>= price_i`, with its `span` already absorbing day `i`'s span.
- Instead of keeping bypassed elements in a list, `StockSpanner3` **pops** them from a monotonic decreasing stack:
  ```java
  public int next(int price) {
      int span = 1;
      while (!stack.isEmpty() && stack.peek()[0] <= price) {
          span += stack.pop()[1];
      }
      stack.push(new int[]{price, span});
      return span;
  }
  ```
- **Invariant**: The stack always maintains elements in **strictly decreasing order of `price`** from bottom to top.

---

#### D. Jump Method (`StockSpanner2`) vs. Monotonic Stack (`StockSpanner3`) & Primitive Array Note
- **Mathematically Identical Comparisons**:
  - The sequence of indices visited by `baseIndex -= list.get(baseIndex)[1]` in `StockSpanner2` is **1-to-1 identical** to the elements sitting on top of `stack` in `StockSpanner3`.
- **When to Prefer Each**:
  - **Jump Method (`StockSpanner2`)**: Best if the system also needs **historical queries** (e.g., *"What was the price/span on day `d`?"* via `list.get(d)` in $O(1)$) or `undo()`, plus `list.get(baseIndex)` is a pure array read inside the loop.
  - **Monotonic Stack (`StockSpanner3`)**: Best for **long/unbounded streams** where history is not queried, because popped `int[]` entries are garbage-collected and the active stack stays small ($O(K)$ decreasing peaks instead of $O(N)$ total days).
- **Brief Note on Primitive Arrays (Approach 3)**:
  - In Java, `new int[]{price, span}` allocates a `24-byte` array object on every call. If maximum speed / zero GC pressure is needed (e.g., high-frequency trading ticks), replacing `List<int[]>` or `Deque<int[]>` with two flat primitive arrays (`int[] prices` and `int[] spans`) avoids per-call object allocation while keeping the exact same Jump or Stack logic.

---

### 3. Java vs. C# Data Structure Mapping

| Concept | Java | C# (.NET) | Notes |
| :--- | :--- | :--- | :--- |
| **Dynamic Array (Jump Method)** | `List<int[]> list = new ArrayList<>()` | `List<(int Price, int Span)> list = new()` | C# value tuples `(int, int)` are `struct`s stored inline inside `List<T>` without per-element heap allocation! |
| **Last Element Access** | `list.getLast()` (Java 21+) / `list.get(list.size() - 1)` | `list[^1]` | C# index-from-end operator `^1` equals `list[list.Count - 1]`. |
| **Monotonic Stack** | `Deque<int[]> stack = new ArrayDeque<>()` | `Stack<(int Price, int Span)> stack = new()` | In Java, always prefer `ArrayDeque` over legacy synchronized `java.util.Stack`. |

---

### 4. Real-World Implementation & Use Cases
1. **Financial Technical Indicators & Market Microstructure**:
   - Computing real-time *"N-day high breakout duration"* or *"days since last higher close"* across thousands of stock/crypto tickers on streaming tick/candle feeds.
2. **Time-Series Skip Pointers**:
   - `StockSpanner2`'s jump-pointer technique (`baseIndex -= span`) is a classic example of **implicit skip pointers** in append-only time-series logs: you keep the append-only log intact for auditing/historical lookup while embedding jump offsets to answer range/threshold queries in $O(1)$ amortized time.

---

### 5. Complexity
- **Time Complexity**:
  - **Per `next(price)` call**: **$O(1)$ amortized** ($O(N)$ worst-case on a single call if a new all-time high follows $N$ strictly decreasing prices).
  - **Across $N$ calls**: **$O(N)$ total** — Each day is pushed/added once and either popped (`StockSpanner3`) or jumped over (`StockSpanner` / `StockSpanner2`) at most once. (In `StockSpanner`, index `0` may be visited one extra time per all-time high jump, which is still $O(1)$ amortized.)
- **Space Complexity**:
  - **`StockSpanner` & `StockSpanner2`**: **$O(N)$** — Retains all $N$ days in `ArrayList`.
  - **`StockSpanner3`**: **$O(K) \subseteq O(N)$** — Stores only the $K$ strictly decreasing active peaks ($O(1)$ space on increasing prices, $O(N)$ worst-case on strictly decreasing prices).
