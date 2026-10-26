# 138. Copy List with Random Pointer

### 1. Two-Pass Hash Map (`Map<Node, Node>`)
- **Core Observation**: A single forward pass cannot resolve pointers cleanly because a node's `random` pointer may point to a node that has not yet been cloned or visited.
- **Mapping Strategy**: Use `Map<Node, Node>` to map each original node reference to its newly created clone instance (`original -> clone`).
- **Two-Pass Execution**:
  - **Pass 1 (Node Creation)**: Traverse the list and instantiate a clone for each node (`new Node(curr.val)`), storing `map.put(curr, clone)`.
  - **Pass 2 (Pointer Wiring)**: Traverse the list again to connect both `next` and `random` pointers:
    ```java
    map.get(curr).next = map.get(curr.next);
    map.get(curr).random = map.get(curr.random);
    ```
- **Null Safety in Java `HashMap`**: In Java, `map.get(null)` safely returns `null`. When `curr.next` or `curr.random` is `null`, `map.get(null)` assigns `null` without throwing an exception, completely eliminating the need for `if (curr.random != null)` checks and dummy heads.

### 2. $O(1)$ Space Optimization: Interleaving / Weaving Nodes
- **Eliminate the Hash Map**: Instead of using an auxiliary map, embed the cloned nodes directly into the original list structure so each original node has an $O(1)$ structural reference to its clone.
- **Three-Phase Traversal**:
  1. **Interleave Nodes**: Insert each cloned node immediately after its original counterpart:
     `[A] -> [A'] -> [B] -> [B'] -> [C] -> [C']`
  2. **Wire `random` Pointers**: 
     - `curr.random` gives the target in the original list.
     - `curr.random.next` is that target's clone!
     ```java
     if (curr.random != null) {
         curr.next.random = curr.random.next;
     }
     ```
     *(Setting `curr.next.random = curr.random` would incorrectly point to the original node instead of the clone).*
  3. **Decouple Lists**: Separate the intertwined list into the original list and cloned list, restoring the original list's `next` pointers to avoid side effects.

### 3. Key Gotchas & Java Details
- **Object Reference as Map Key vs. `hashCode`**:
  - `Node` inherits default `hashCode()` and `equals()` from `java.lang.Object`, which rely on **memory reference identity** (`==`).
  - **Separate Chaining**: In `HashMap`, even if two distinct `Node` instances produce the same bucket index, they are chained together safely because `nodeA == nodeB` evaluates to `false`.
  - **Never use `hashCode` as the key (`Map<Integer, Node>`)**: Hash collisions on 32-bit integers would cause distinct nodes to overwrite each other, corrupting the list. Furthermore, `null.hashCode()` throws `NullPointerException`.
- **Coding to Interface**: Prefer `Map<Node, Node> map = new HashMap<>()` over concrete `HashMap` declaration to follow standard Java encapsulation best practices.
- **Preserving Input Integrity**: In the $O(1)$ space interleaving approach, you must properly restore the original list (`curr.next = clone.next`), rather than discarding it, so the caller's input list remains intact.

### 4. Complexity
- **Two-Pass Hash Map (Methods 1 & 2)**:
  - **Time**: $O(N)$ — two linear passes over $N$ nodes.
  - **Space**: $O(N)$ — auxiliary hash map storing $N$ key-value pairs.
- **Interleaving Approach (Method 3)**:
  - **Time**: $O(N)$ — three linear passes over $2N$ nodes.
  - **Space**: $O(1)$ auxiliary space — pointers only; no extra data structures allocated.
