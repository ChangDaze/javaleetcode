# 3. Longest Substring Without Repeating Characters

### 1. Variable-Size Sliding Window (HashSet)
- **Core Observation**: Unlike fixed-size sliding windows, the window size dynamically expands and contracts to maintain the invariant: **no duplicate characters inside `[slow, fast]`**.
- **State Transition**:
  - `fast` expands the window to the right and discovers new characters.
  - When `currentSet.contains(c)`, `slow` contracts the window from the left, dropping outgoing characters until the duplicate is removed.
  - Window length is `fast - slow + 1` (or `currentSet.size()`).

### 2. Optimization: Direct Jump via `int[128]`
- **Eliminate Inner Loop**: Instead of shrinking `slow` step-by-step with a `while` loop, store `lastSeenIndex + 1` in an `int[128]` array.
- **One-Way Direction**: `slow` can directly jump past the duplicate's prior position:
  ```java
  slow = Math.max(slow, records[c]);
  max = Math.max(max, fast - slow + 1);
  records[c] = fast + 1; // Store next valid starting index
  ```
  `Math.max` ensures `slow` never jumps backward if the duplicate character occurred before the current `slow` boundary.

### 3. Key Gotchas & Java Details
- **No `HashSet<char>`**: Java generics do not support primitive types. Must use the wrapper class `HashSet<Character>` (autoboxing handles `char` $\leftrightarrow$ `Character`).
- **`String.length()` is $O(1)$**: Implemented as `value.length >> coder()`. It reads array length metadata directly and bit-shifts based on encoding (LATIN1 vs UTF16).
- **`currentSet.size()` is $O(1)$**: Returns the internal integer `size` field of the backing `HashMap`, so using `currentSet.size() > max` has zero performance penalty.
- **Why `int[128]` runs faster**: Direct array lookup eliminates autoboxing, avoids heap object allocations/GC, and fits entirely in the L1 CPU cache.

### 4. Complexity
- **HashSet Approach**:
  - **Time**: O(n) — each character is added and removed at most once (at most $2n$ steps).
  - **Space**: O(min(n, m)) — where $m$ is the charset size ($m \le 128$ for ASCII).
- **`int[128]` Jump Approach**:
  - **Time**: O(n) — strictly single pass ($n$ steps).
  - **Space**: O(1) — constant 128-element array (512 bytes).

### 5. Extension: Non-ASCII & Unicode Support (`HashMap`)
- **Handling Chinese / Emojis**: `int[128]` works only for standard ASCII. For strings containing Chinese or full Unicode characters, an array index lookup causes `ArrayIndexOutOfBoundsException`.
- **Method 2 + `HashMap`**: Combine the single-pass jump optimization with `Map<Character, Integer>`:
  ```java
  Map<Character, Integer> records = new HashMap<>();
  for (int fast = 0, slow = 0; fast < s.length(); fast++) {
      char c = s.charAt(fast);
      slow = Math.max(slow, records.getOrDefault(c, 0));
      max = Math.max(max, fast - slow + 1);
      records.put(c, fast + 1);
  }
  ```
  This preserves the $O(n)$ single-pass jump logic while safely supporting arbitrary character sets.
