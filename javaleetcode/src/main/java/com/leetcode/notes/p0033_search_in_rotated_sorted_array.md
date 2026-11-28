# 33. Search in Rotated Sorted Array

### 1. Progressive Evolution of Approaches
- **Core Problem**: Given an integer array `nums` of **distinct** values originally sorted in ascending order and possibly rotated at an unknown pivot index $k$, return the index of `target` in $O(\log N)$ time, or `-1` if it is not in `nums`.
- **Key Insight (The "One Cliff" Invariant)**:
  - A naive linear scan takes $O(N)$ time and fails the $O(\log N)$ requirement.
  - Because `nums` was originally sorted and rotated only once, it contains **at most one drop ("cliff")** where `nums[i] > nums[i + 1]`. That cliff divides the array into **two strictly sorted halves** (a larger Left group and a smaller Right group).
  - Whether we find the cliff first (2-pass) or inspect which half is clean at every `mid` (1-pass), we can eliminate half of the remaining search space on every iteration to achieve **$O(\log N)$**.
- **Comparison of the Implementations**:

| Implementation | Mechanism / Technique | Passes | Space / Overhead | Key Limitation / Advantage |
| :--- | :--- | :---: | :--- | :--- |
| **`search`** | 2-Pass Binary Search: Find Pivot + Virtual `%` Index Mapping | 2 | $O(1)$ | Intuitive "unwrap the rotated array virtually" model; runs modulo `%` (`idiv`) on every step and searches all $N$ virtual indices in Pass 2. |
| **`search2`** | 2-Pass Binary Search + **Search Space Pruning** (Pick Sorted Half) | 2 | $O(1)$ | **Cleanest 2-Pass**: Compares `target <= nums[last]` once to discard the useless half, removing `%` completely and reducing Pass 2 to textbook binary search. |
| **`search3`** | 1-Pass **Sorted-Half** Binary Search | **1** | $O(1)$ | **Interview Gold Standard**: Halves total iterations ($\approx \log_2 N$) with direct, readable range checks; must use `nums[l] <= nums[mid]` for 2-element subarrays. |
| **`search4`** | 1-Pass **Virtual $\pm\infty$ Projection** (Group-Based) | **1** | $O(1)$ | Clever reduction to plain binary search by pretending the "wrong side" is $-\infty$ or $+\infty$; less immediately readable and assumes `target` cannot equal `Integer.MIN_VALUE` / `MAX_VALUE`. |

---

### 2. Deep Dive into Each Approach

#### A. `search`: Two-Pass Pivot + Virtual `%` Mapping (and 5 Classic Binary Search Bugs)
- **Idea**:
  1. **Loop 1**: Binary search for the smallest element's index (`rotateDegree`), which is the start of the original unrotated array.
  2. **Loop 2**: Binary search virtual sorted indices `0 .. n - 1`, mapping each virtual `middle` to its physical index via:
     ```java
     int realMiddle = (middle + rotateDegree) % nums.length;
     ```
- **5 Subtle Binary Search Bugs Caught & Fixed**:
  1. **Infinite loop on `l = middle` in Loop 1**: Integer division `(l + r) / 2` floors toward `l`. Whenever `r == l + 1`, `middle == l` (e.g., `l = 3 (7), r = 4 (0)` $\rightarrow$ `middle = 3`). Assigning `l = middle` never moves `l`. Fix: `l = middle + 1` (safe because in the `else` branch `nums[middle] > nums[r]`, so `middle` can never be the minimum).
  2. **Wrong offset direction (`middle - rotateDegree`)**: Subtracting `rotateDegree` produces negative indices (`ArrayIndexOutOfBoundsException`). Converting a *virtual* sorted index to a *real* rotated index shifts **forward** with wrap-around: `(middle + rotateDegree) % nums.length`.
  3. **Returning `rotateDegree` instead of `realMiddle`**: When `nums[realMiddle] == target`, return `realMiddle` (the target's actual index), not the pivot index.
  4. **Infinite loop on `l = middle` / `r = middle` in Loop 2**: Once `nums[realMiddle] == target` is already checked, `middle` is ruled out and must be excluded with `r = middle - 1` and `l = middle + 1`.
  5. **`while (l < r)` skips the final element**: Stopping at `l == r` never inspects the last candidate (e.g., `nums = [1], target = 1` skips the loop entirely and returns `-1`). Always pair `l = mid + 1` / `r = mid - 1` with `while (l <= r)`.
- **Subtle Note on Distinctness & Early Exit in Loop 1**:
  - Even with distinct values, `nums[l] == nums[middle]` **can** occur when `r == l + 1` (since `middle == l`). Loop 1 stays safe because it compares `nums[middle]` against `nums[r]`, and `l < r` guarantees `middle < r`, so `nums[middle] != nums[r]` always holds.
  - `if (nums[l] < nums[r]) break;` is optional for correctness—keeping it gives an $O(1)$ early exit when the array (or current subarray) is already sorted, at the cost of one extra comparison per step.

---

#### B. `search2`: Two-Pass Pivot + Search Space Pruning (Problem Reduction)
- Once Loop 1 finds `rotateDegree`, the array is cleanly split into two already-sorted halves:
  ```text
  index:  0  1  2  3 | 4  5  6
  nums:  [4, 5, 6, 7 | 0, 1, 2]
                       ^     ^
             rotateDegree   last (nums[last] = 2)
  ```
  - Every element on the **Right** (`rotateDegree .. last`) is `<= nums[last]`.
  - Every element on the **Left** (`0 .. rotateDegree - 1`) is `> nums[last]`.
- **Search Space Pruning**: Instead of remapping all $N$ indices with `%`, one `if` statement prunes the impossible half:
  ```java
  int last = nums.length - 1;
  if (target <= nums[last]) {
      l = rotateDegree;
      r = last;
  } else {
      l = 0;
      r = rotateDegree - 1;
  }
  ```
  - **Handles unrotated arrays automatically**: When `rotateDegree == 0` and `target > nums[last]`, `r` becomes `-1`, so `while (l <= r)` exits immediately and returns `-1`.
  - **Eliminates `%` modulo arithmetic**: Loop 2 becomes a pure textbook binary search on `[l, r]` using overflow-safe `middle = l + (r - l) / 2`.
- **How to describe this in an interview**: *"First I binary search for the pivot, then I prune the search space to the single sorted half that can contain `target`, and run a standard binary search."*

---

#### C. `search3`: Single-Pass Sorted-Half Binary Search (Interview Gold Standard)
- **The "One Cliff" Golden Rule**:
  ```text
  index:  0  1  2  3  4  5  6
  nums:  [4, 5, 6, 7, 0, 1, 2]
                   └──┘
               the ONLY cliff
  ```
  When we cut `[l .. r]` at `mid`, the single cliff can fall on **at most one side**. Therefore, **at least one of `[l .. mid]` or `[mid .. r]` is guaranteed to be strictly sorted**.
- **Decision Rule at Every `mid`**:
  1. If `nums[mid] == target`, return `mid`.
  2. Check if the **Left half `[l .. mid]` is sorted** (`nums[l] <= nums[mid]`):
     - If `nums[l] <= target && target < nums[mid]` $\rightarrow$ `target` lies inside the sorted Left half $\rightarrow$ `r = mid - 1`.
     - Else $\rightarrow$ discard Left half $\rightarrow$ `l = mid + 1`.
  3. Else, the **Right half `[mid .. r]` MUST be sorted**:
     - If `nums[mid] < target && target <= nums[r]` $\rightarrow$ `target` lies inside the sorted Right half $\rightarrow$ `l = mid + 1`.
     - Else $\rightarrow$ discard Right half $\rightarrow$ `r = mid - 1`.
- **Trace on `nums = [4, 5, 6, 7, 0, 1, 2], target = 0`**:
  1. `l=0 (4), r=6 (2), mid=3 (7)`: Left `[4..7]` sorted (`4 <= 7`), `0` not in `[4, 7)` $\rightarrow$ `l = 4`.
  2. `l=4 (0), r=6 (2), mid=5 (1)`: Left `[0..1]` sorted (`0 <= 1`), `0` in `[0, 1)` $\rightarrow$ `r = 4`.
  3. `l=4 (0), r=4 (0), mid=4 (0)`: `nums[4] == 0` $\rightarrow$ return `4`.
- **Two Critical Gotchas Caught**:
  1. **Why `nums[l] <= nums[mid]` MUST include `=`**: Consider `nums = [3, 1], target = 1` (`l = 0, r = 1, mid = 0`). Here `l == mid`, so `nums[l] == nums[mid] == 3`.
     - With `<=`: Left `[3]` is treated as sorted, `1` is not in `[3, 3)`, so `l = mid + 1 = 1` $\rightarrow$ finds `1`.
     - With `<`: `3 < 3` is `false` $\rightarrow$ enters the `else` branch assuming `[3, 1]` is sorted (false!), fails `3 < 1 && 1 <= 1`, sets `r = -1`, and wrongly returns `-1`.
  2. **Midpoint formula typo**: Always write `l + (r - l) / 2`, never `l + (l + r) / 2` (which exceeds `r` as soon as `l > 0` and causes `ArrayIndexOutOfBoundsException`).

---

#### D. `search4`: Single-Pass Virtual $\pm\infty$ Projection
- **Mental Model**: Ignore `mid`'s real value when it sits on the **wrong side** of the pivot relative to `target`; pretend the entire wrong side is $-\infty$ or $+\infty$ so the array becomes virtually sorted!
  - **Case A (`target = 1`, on Right side)**:
    ```text
    real nums:     [ 4,  5,  6,  7 | 0,  1,  2]
    pretend nums:  [-∞, -∞, -∞, -∞ | 0,  1,  2]  <-- 100% sorted!
    ```
  - **Case B (`target = 5`, on Left side)**:
    ```text
    real nums:     [4,  5,  6,  7 |  0,  1,  2]
    pretend nums:  [4,  5,  6,  7 | +∞, +∞, +∞]  <-- 100% sorted!
    ```
- **Using `nums[0]` as the Group Divider**:
  - Since the rotated Left group was originally the larger tail, every element in the **Left group** is `>= nums[0]` and every element in the **Right group** is `< nums[0]`.
  - `boolean targetOnRight = target < nums[0];`
  - `boolean midOnRight = nums[mid] < nums[0];`

| `midOnRight` | `targetOnRight` | Same Side? | Effective `val` | Binary Search Action |
| :---: | :---: | :---: | :--- | :--- |
| `false` | `false` | Yes (both Left) | `nums[mid]` | Normal comparison |
| `true` | `true` | Yes (both Right) | `nums[mid]` | Normal comparison |
| `false` | `true` | No (`mid` Left, `target` Right) | `Integer.MIN_VALUE` ($-\infty$) | `val < target` $\rightarrow$ move Right (`l = mid + 1`) |
| `true` | `false` | No (`mid` Right, `target` Left) | `Integer.MAX_VALUE` ($+\infty$) | `val > target` $\rightarrow$ move Left (`r = mid - 1`) |

- **Loop-Invariant Code Motion**: `targetOnRight = target < nums[0]` never changes during the search, so hoisting it above `while (l <= r)` computes it once instead of $\log N$ times (safe because LeetCode guarantees `nums.length >= 1`; add `if (nums.length == 0) return -1;` for general-purpose library code).
- **Sentinel Caveat & Readability**: Using `Integer.MIN_VALUE` / `Integer.MAX_VALUE` works under LeetCode's constraint ($-10^4 \le \text{nums}[i], \text{target} \le 10^4$), or you can widen `val` to `long` (`Long.MIN_VALUE` / `Long.MAX_VALUE`) for arbitrary 32-bit `int`s. Even so, **`search3`** is generally preferred in interviews and production code because its intent is immediately obvious without virtual sentinel tricks.
- **Micro-Optimization Note**: Removing `%` (`search2`/`search3`/`search4`) avoids an expensive integer division (`idiv`) instruction inside the hot binary search loop, and `(l + r) >>> 1` (unsigned right shift) computes an overflow-safe midpoint in a single CPU shift instruction.

---

### 3. Java vs. C# Mapping

| Concept | Java | C# (.NET) | Notes |
| :--- | :--- | :--- | :--- |
| **Overflow-Safe Midpoint** | `l + (r - l) / 2` or `(l + r) >>> 1` | `l + (r - l) / 2` or `(l + r) >>> 1` | Java has always had `>>>` (unsigned right shift); C# added `>>>` in **C# 11 (.NET 7)**. |
| **Built-in Subrange Binary Search** | `Arrays.binarySearch(nums, fromIndex, toIndex, target)` | `Array.BinarySearch(nums, startIndex, length, target)` or `nums.AsSpan(start, length).BinarySearch(target)` | **Gotcha**: Java's 3rd arg is an **exclusive end index** (`toIndex`), whereas C#'s 3rd arg is a **count (`length`)**. Both return a negative bitwise complement (`~insertionPoint`) when not found. |
| **Sentinel Bounds (`±∞`)** | `Integer.MIN_VALUE` / `Integer.MAX_VALUE` | `int.MinValue` / `int.MaxValue` | Same 32-bit signed range $[-2^{31}, 2^{31}-1]$; widen to `long` if inputs can hit `int` limits. |
| **Shared Multi-Method Test Delegate** | `@FunctionalInterface interface RotatedArraySearcher { int search(int[] nums, int target); }` | `Func<int[], int, int>` | C# has built-in generic `Func<...>` delegates for up to 16 parameters without defining a custom interface. |

---

### 4. Real-World Implementation & Use Cases
1. **Circular Ring Buffers for Logs, Sensor Telemetry & Video Frames (Linux `dmesg`, Embedded Systems, Flight Recorders)**:
   - A fixed-size ring buffer avoids $O(N)$ shifting and unbounded memory growth by overwriting index `0` once full:
     ```text
     Events 1..5 arrive:  [1, 2, 3, 4, 5]  (full!)
     Event 6 arrives:     [6, 2, 3, 4, 5]  (overwrites index 0)
     Events 7, 8 arrive:  [6, 7, 8 | 4, 5] <-- physically a rotated sorted array!
     ```
   - Looking up an event by timestamp in $O(\log N)$ without copying or un-rotating the buffer uses this exact algorithm:
     - If the buffer already tracks its `head` (oldest index, e.g., `3`), `head` **is** `rotateDegree` $\rightarrow$ jump straight to **Pass 2 of `search2`** (pruned binary search).
     - If reading a raw memory dump / crash snapshot without knowing `head` $\rightarrow$ run **full `search2` or `search3`**.
2. **Rolling Cyclic Time-Window Indexes (Rate Limiters & Metrics Windows)**:
   - Fixed-slot rolling windows (e.g., storing sorted event timestamps for the last 60 minutes in a fixed array) wrap writes around the array rather than shifting old entries out—allowing $O(\log N)$ timestamp lookups directly in place with zero GC/allocation overhead.

---

### 5. Complexity
- **Time Complexity**:
  - **All 4 Implementations**: **$O(\log N)$** — every iteration discards half of the active interval `[l, r]`.
  - **Constant-Factor Difference**:
    - `search` & `search2` (2-Pass): at most $\approx 2 \log_2 N$ iterations (Pass 1 finds the pivot in $\le \lceil \log_2 N \rceil$ steps; Pass 2 searches in $\le \lceil \log_2 N \rceil$ steps, with `search2` searching a smaller subarray and avoiding `%`).
    - `search3` & `search4` (1-Pass): at most $\lceil \log_2 N \rceil + 1$ iterations in a single loop.
- **Space Complexity**:
  - **All 4 Implementations**: **$O(1)$** auxiliary space — purely iterative with a fixed number of primitive index/boolean variables (`l`, `r`, `mid`).
