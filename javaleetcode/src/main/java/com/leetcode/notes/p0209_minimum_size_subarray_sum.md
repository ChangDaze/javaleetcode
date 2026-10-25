# 209. Minimum Size Subarray Sum

### 1. Dynamic Sliding Window (Variable Size)
- **Core Observation**: All elements are positive ($nums[i] > 0$). Therefore, expanding the window to the right monotonically increases the sum, and shrinking from the left monotonically decreases it.
- **State Transition**:
  - `fast` (or `right`) expands the window to find a valid subarray (`current >= target`).
  - Once valid, `slow` (or `left`) shrinks the window from the left to search for the minimal length until `current < target`.

### 2. Single `while` Loop Template
- **Avoid Redundant Checks**: Do not write duplicate `if (current >= target)` checks before or inside the loop. The `while` loop itself naturally handles both checking and shrinking:
  ```java
  while (current >= target) {
      result = Math.min(result, fast - slow + 1);
      current -= nums[slow++];
  }
  ```
- **Combined Decrement & Shift**: `current -= nums[slow++]` subtracts the outgoing element and moves the left pointer in a single concise step.

### 3. Key Gotchas & Java Details
- **Sentinel Initialization**: Initialize `result = Integer.MAX_VALUE`. At the end, return `result == Integer.MAX_VALUE ? 0 : result` to handle edge cases where the sum of all elements is still less than `target`.
- **`Math.min` vs. `if` Efficiency**: `Math.min(int, int)` is a HotSpot JVM intrinsic (`@IntrinsicCandidate`). It compiles directly into a single branchless CPU instruction (e.g., `cmov` on x86, `csel` on ARM), meaning zero method call overhead and no branch misprediction penalties compared to manual `if` branches.
- **Pointers & Scope**: Both pointers move strictly from left to right. `fast` visits each element once in the `for` loop, and `slow` visits each element at most once across all `while` loop iterations.

### 4. Complexity
- **Time**: O(n) — even with the nested `while` loop, each element is added once and subtracted at most once (at most $2n$ total pointer operations).
- **Space**: O(1) — constant extra space using only pointer and sum variables.
