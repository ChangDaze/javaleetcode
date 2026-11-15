# 84. Largest Rectangle in Histogram

### 1. Progressive Evolution of Approaches
- **Core Problem**: Given an array of bar heights `heights[]` where each bar has width `1`, find the area of the largest rectangle that can be formed within the histogram.
- **Key Insight (Bounding Each Bar's Full Expansion)**:
  - Every maximal rectangle's height is determined by the **shortest bar** inside it. Therefore, if we treat each bar `i` as that limiting height `heights[i]`, we just need to find how far left and how far right bar `i` can expand before hitting a bar **strictly shorter** than `heights[i]`.
  - A naive scan left and right for every bar takes $O(N^2)$ time. By reusing previously computed spans/boundaries (via **Jump Pointers** or a **Monotonic Stack**), each bar is skipped/popped at most once, reducing total time to **$O(N)$**.
- **Comparison of the Implementations**:

| Implementation | Mechanism / Data Structure | Passes | Space / Overhead | Key Limitation / Advantage |
| :--- | :--- | :---: | :--- | :--- |
| **`largestRectangleArea`** | 2-Pass Monotonic Stack (`Deque<int[]>`) + `leftSpan[]` / `rightSpan[]` | 3 (`L`, `R`, `max`) | $O(N)$ (`2 * int[n]` + $2N$ `int[2]` heap objects) | Directly reuses the **Online Stock Span (`p0901`)** mental model from both directions; easy to understand, but allocates $2N$ small `int[]` objects. |
| **`largestRectangleArea2`** | 2-Pass Jump Pointers (`lessFromLeft[]` & `lessFromRight[]`) | 3 (`L`, `R`, `max`) | $O(N)$ (`2 * int[n]`, **0 heap objects**) | **Most Intuitive & Cache-Friendly**: Eliminates the stack completely by jumping directly via boundary indices (`p = lessFromLeft[p]`). Symmetric and easy to debug. |
| **`largestRectangleArea3`** | 1-Pass Monotonic Increasing Stack (`Deque<Integer>`) + Cleanup Loop | 1 (+ flush) | $O(N)$ (`Deque<Integer>`, $N$ boxed `Integer`s) | **Canonical Single-Pass Stack**: Uses the stack as a "waiting room"—left boundary is `stack.peek()` underneath, right boundary is the index `i` that triggered the pop. |
| **`largestRectangleArea4`** | 1-Pass Primitive Array Stack (`int[] stack`, `top`) + Virtual `0` Sentinel | **1** | $O(N)$ (`int[n + 1]`, **0 heap objects**) | **Fastest Single-Pass**: Replaces `Deque<Integer>` with an `int[]` array + `top` pointer, and uses `currentHeight = 0` at `i == n` to merge the cleanup loop into the main loop. |

---

### 2. Deep Dive into Each Approach

#### A. `largestRectangleArea`: Reusing the Online Stock Span (`p0901`) Mental Model
- For any bar `i`, its rectangle width is `(consecutive bars to the left >= heights[i]) + (consecutive bars to the right >= heights[i]) - 1`:
  ```java
  int area = heights[i] * (leftSpan[i] + rightSpan[i] - 1); // -1 because bar i is counted in both spans
  ```
- **Why Loop 1 (Left-to-Right) and Loop 2 (Right-to-Left) Shouldn't Be Fused into One Two-Pointer Loop**:
  - Although `i` (`0 -> n-1`) and `j = n - 1 - i` (`n-1 -> 0`) can step in the same loop header, they move in **opposite directions**.
  - Fusing them would require **two separate stacks** living in memory at the same time (instead of `stack.clear()`) and would still not have both `leftSpan[k]` and `rightSpan[k]` ready until the second half of the loop, while jumping between opposite ends of `heights[]` (hurting CPU cache locality).

---

#### B. `largestRectangleArea2`: Two-Pass Jump Pointers (No Stack Needed!)
- Unlike Online Stock Span (`p0901`) where prices arrive one by one in a stream, here **the entire `heights[]` array is already in memory**.
- Instead of storing `[height, span]` on a stack, we record the **exclusive blocker index** on each side:
  - `lessFromLeft[i]`: first index `< i` with `heights[p] < heights[i]` (or `-1` if none).
  - `lessFromRight[i]`: first index `> i` with `heights[p] < heights[i]` (or `heights.length` if none).
- **How the Jump Works**:
  ```java
  // Loop 1: Left blocker
  for (int i = 0; i < heights.length; i++) {
      int p = i - 1;
      while (p >= 0 && heights[i] <= heights[p]) {
          p = lessFromLeft[p]; // Teleport over the entire valid span of p!
      }
      lessFromLeft[i] = p;
  }
  ```
  - If `heights[i] <= heights[p]`, then every bar between `lessFromLeft[p]` and `p` was already `>= heights[p] >= heights[i]`, so we can skip all of them in $O(1)$ by jumping `p = lessFromLeft[p]`.
  - Notice that starting at `i = 0` sets `p = -1` (and at `i = n - 1` sets `p = n`), which naturally initializes the outer boundaries without needing extra setup lines.
  - Once both arrays are filled, the width between exclusive walls `(lessFromLeft[i], lessFromRight[i])` is simply `lessFromRight[i] - lessFromLeft[i] - 1`.

---

#### C. `largestRectangleArea3`: Single-Pass Monotonic Increasing Stack & Two Crucial Gotchas
- **The "Waiting Room" Mental Model**:
  - Moving left-to-right, we cannot calculate bar `x`'s area immediately because we haven't seen its **right blocker** yet. So we push index `x` into a monotonic increasing stack to wait.
  - As soon as a shorter bar `heights[i] < heights[stack.peek()]` arrives:
    1. **Right Blocker (`lessFromRight`)**: Current index `i` is the first bar on the right shorter than `heights[popIndex]`.
    2. **Left Blocker (`lessFromLeft`)**: Because the stack is monotonically increasing, the element sitting **directly underneath `popIndex`** in the stack (`stack.peek()` after popping, or `-1` if empty) is the first bar on the left shorter than `heights[popIndex]`.
- **Gotcha 1: Why Width is `(i - left - 1)` Instead of `(i - popIndex)`**:
  - Consider `heights = [5, 4]`:
    - At `i = 0` (`h = 5`), push `0`.
    - At `i = 1` (`h = 4`), `5 > 4`, so index `0` (`h = 5`) is **popped** (`area = 5 * 1 = 5`), and index `1` (`h = 4`) is pushed.
    - When index `1` (`h = 4`) is popped at the end (`right = 2`), if we used `(right - popIndex) = 2 - 1 = 1`, we would get `area = 4` instead of `8`!
    - Why? Because taller bars to the left of `popIndex` (like `5` at index `0`) were **already popped earlier**! Looking at what remains underneath in the stack (`left = stack.isEmpty() ? -1 : stack.peek()`) reveals how far left `popIndex` can actually reach across those already-popped taller bars (`2 - (-1) - 1 = 2`, `area = 8`).
- **Gotcha 2: What Happens with Duplicate Heights like `heights = [4, 4]`?**:
  - With strict `>` (`while (!stack.isEmpty() && heights[stack.peek()] > heights[i])`):
    - Both index `0` (`h = 4`) and index `1` (`h = 4`) stay on the stack (`[0, 1]`).
    - When flushing at the end (`right = 2`):
      - First pop `popIndex = 1`: `left = stack.peek() = 0` $\rightarrow$ `width = 2 - 0 - 1 = 1` (`area = 4`, truncated width!).
      - Next pop `popIndex = 0`: `left = -1` $\rightarrow$ `width = 2 - (-1) - 1 = 2` (`area = 8`, full combined width!).
  - Even though the right duplicate got a truncated width, the **leftmost** duplicate at the bottom of the equal-height group always gets the true full width across all duplicates, so `maxArea` is still guaranteed to be right!

---

#### D. `largestRectangleArea4`: Primitive Array Stack (`int[] stack` + `top`) & Virtual `0` Sentinel
- **Why `int[] stack` Does NOT Correspond 1-to-1 with Index `i`**:
  - Unlike `lessFromLeft[i]` (where index `i` in the array corresponds to bar `i`), `int[] stack` is just a **stack simulator** indexed by `top` (`0, 1, 2, ...`, the current number of active items in the stack).
  - The *values* stored inside `stack[0 .. top]` are the bar indices `i` waiting to be popped:
    ```text
    heights = [2, 1, 5, 6, 2, 3]  (after i = 4)
    stack index (top):   [ 0 ,  1 ]
    value inside stack:  [ 1 ,  4 ]  <-- bar indices stored at current stack depth
    ```
  - We allocate `new int[heights.length + 1]` only because in the worst case (strictly increasing heights), up to `heights.length + 1` indices can sit in the stack before popping.
- **Merging the Two `while` Loops with a Virtual `0`-Height Sentinel**:
  - By running `for (int i = 0; i <= heights.length; i++)` and defining:
    ```java
    int currentHeight = (i == heights.length) ? 0 : heights[i];
    ```
  - When `i == heights.length`, `currentHeight = 0` acts as a ground-level wall at the right end of the histogram, forcing every positive-height bar remaining in the stack to pop inside the main `while (top != -1 && heights[stack[top]] > currentHeight)` loop without needing a second cleanup `while` loop.

---

### 3. Java vs. C# Data Structure Mapping

| Concept | Java | C# (.NET) | Notes |
| :--- | :--- | :--- | :--- |
| **Stack of `[Height, Span]` Pairs** | `Deque<int[]> stack = new ArrayDeque<>()` | `Stack<(int Height, int Span)> stack = new()` | C# `ValueTuple` `(int, int)` is a value-type `struct` stored contiguously inside `Stack<T>` with zero per-element heap allocation. |
| **Stack of Indices** | `Deque<Integer> stack = new ArrayDeque<>()` | `Stack<int> stack = new()` | C# generics specialize over primitive `int` (`Stack<int>`) without boxing, whereas Java `Deque<Integer>` boxes `int` to `Integer` unless replaced by `int[] stack`. |
| **Stack-Allocated Primitive Buffer** | `int[] stack = new int[n + 1]` (heap array) | `Span<int> stack = stackalloc int[n + 1]` | For moderate $N$, C# `stackalloc` allocates the buffer directly on the thread stack with zero GC involvement. |

---

### 4. Real-World Implementation & Use Cases
1. **2D Obstacle-Free Layout & UI / Document Rendering (Maximal Empty Rectangle)**:
   - Finding the largest rectangular region free of obstacles in a 2D grid (such as **LeetCode 85: Maximal Rectangle**, automated warehouse floor planning, or placing floating callouts/ads in a page layout engine) reduces directly to running `largestRectangleArea` row-by-row on vertical histogram heights.
2. **Computer Vision & Image Cropping (Auto-Crop after Rotation)**:
   - When an image or aerial map is rotated, empty triangular corners appear. Finding the largest axis-aligned valid pixel bounding box inside a binary mask uses the exact histogram largest-rectangle pass per scanline.
3. **Capacity & SLA Window Analysis in Time-Series Telemetry**:
   - Given a time-series of available server bandwidth or power capacity $C[t]$, finding the maximum total data transfer $\Delta t \times \min_{t \in \Delta t} C[t]$ achievable at a constant rate over a contiguous time window is mathematically identical to the largest rectangle in a histogram.

---

### 5. Complexity
- **Time Complexity**:
  - **All 4 Implementations**: **$O(N)$** —
    - In `largestRectangleArea` and `largestRectangleArea3`/`4`, each bar is pushed onto the stack once and popped at most once across the entire loop ($2N$ stack operations max).
    - In `largestRectangleArea2`, each jump `p = lessFromLeft[p]` permanently skips over indices whose span has already been resolved, so the inner `while` loop executes at most $N$ times in total across the entire pass.
- **Space Complexity**:
  - **`largestRectangleArea`**: **$O(N)$** — Two `int[n]` span arrays plus a `Deque<int[]>` holding up to $N$ heap-allocated `int[2]` objects.
  - **`largestRectangleArea2`**: **$O(N)$** — Two flat `int[n]` arrays (`lessFromLeft` and `lessFromRight`), zero object boxing.
  - **`largestRectangleArea3`**: **$O(N)$** — One `Deque<Integer>` holding up to $N$ boxed `Integer` references.
  - **`largestRectangleArea4`**: **$O(N)$** — One flat `int[n + 1]` primitive array (`stack`), zero object boxing and smallest memory footprint.
