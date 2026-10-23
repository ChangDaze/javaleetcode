# 643. Maximum Average Subarray I

### 1. Sliding Window (Fixed Size k)
- **Core Observation**: When shifting the window of size `k` by 1 step, only 2 elements change.
- **O(1) State Transition**:
  `current = current + nums[fast] - nums[slow]`
  Add the incoming element (`fast`), subtract the outgoing element (`slow`). Reduces time from O(n * k) to O(n).

### 2. Maximize Sum, Divide Once
- Since `k` is constant, **maximizing average == maximizing sum**.
- Keep `current` and `max` as `int` throughout the loop.
- Only cast and divide once at the end:
  ```java
  return (double) max / k;
  ```

### 3. Key Gotchas & Java Details
- **Initialization**: Never initialize `max = 0` because values can be negative. Always set `max = current` (sum of the first `k` elements).
- **Two loop variables**: Java allows multiple variables in a `for` header if they share the same type:
  ```java
  for (int fast = k, slow = 0; fast < nums.length; fast++, slow++)
  ```

### 4. Complexity
- **Time**: O(n) — single pass.
- **Space**: O(1) — constant extra space.