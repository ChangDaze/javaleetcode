# 146. LRU Cache

### 1. Custom Doubly Linked List + Hash Map (`Map<Integer, Node>`)
- **Core Observation**: An LRU (Least Recently Used) cache requires two operations, both strictly in $O(1)$ average time:
  1. Instant key lookup and value retrieval -> `HashMap` provides $O(1)$.
  2. Instant eviction of the least recently used element, and instant promotion of an accessed element to most recently used -> `Doubly Linked List`.
- **Why Queue & Singly Linked List Fail**:
  - **Queue (`ArrayDeque`)**: Only supports $O(1)$ operations at the ends. When a key in the middle is accessed via `get()` or `put()`, pulling it out to refresh access order requires scanning the queue $\to O(N)$, causing TLE.
  - **Singly Linked List**: Even if `map.get(key)` locates the node in $O(1)$, deleting a node requires updating its predecessor (`prev.next = curr.next`). Without a `.prev` pointer, finding `prev` requires traversing from the head $\to O(N)$.
  - **Doubly Linked List**: Each node stores both `prev` and `next`. Splicing out a node and moving it to the head takes strictly $O(1)$ with no searching needed.
- **Dummy Sentinel Nodes (`head` & `tail`)**:
  - Use pseudo-nodes `head` (boundary for MRU - Most Recently Used) and `tail` (boundary for LRU - Least Recently Used):
    ```text
    [head] <---> [Node 1 (MRU)] <---> [Node 2] <---> [Node 3 (LRU)] <---> [tail]
    ```
  - Eliminates edge-case checks (`head == null`, `node == head`, `node == tail`). Node insertion and deletion become standard 4-pointer reassignments without null checks.
- **Node Stores Both `key` and `value`**:
  - When the cache exceeds capacity, `removeTail()` pops the LRU node.
  - Storing `key` inside `Node` allows the cache to know which key to delete from the `map` in $O(1)$ (`map.remove(lru.key)`).

### 2. Built-in Solution: `java.util.LinkedHashMap`
- **Native Hash Table + Doubly Linked List**: Java's standard library already implements this exact hybrid structure in `LinkedHashMap`.
- **Constructor Parameters (`super(capacity, 0.75f, true)`)**:
  - **`initialCapacity`**: Initial number of hash bucket slots allocated.
  - **`loadFactor` (`0.75f`)**: Threshold ratio before resizing ($\text{threshold} = \text{capacity} \times \text{loadFactor}$).
    - `0.75` (75%) provides the optimal mathematical balance between memory overhead and hash collision avoidance (Poisson distribution).
    - The `f` denotes a 32-bit `float` literal (preventing a double-to-float lossy conversion compiler error).
  - **`accessOrder` (`true`)**: Orders entries by access order (LRU mode: least-recently accessed to most-recently accessed). Default `false` orders entries by insertion order (FIFO).
- **Automatic Eviction Hook**:
  - Overriding `removeEldestEntry(Map.Entry eldest)` returning `size() > capacity` instructs `put()` to automatically evict the least recently accessed entry whenever capacity is exceeded.

### 3. Key Gotchas & Java Details
- **Primitive `int` vs. `Integer` (NullPointerTrap)**:
  - Generic collections require reference types (`Map<Integer, Node>`).
  - `_map.get(key)` returns `null` for missing keys. Assigning it directly to a primitive `int` (`int val = _map.get(key)`) triggers automatic unboxing (`null.intValue()`), throwing `NullPointerException`. Either check for `null` or use `getOrDefault(key, -1)`.
- **`put` Early Return on Existing Keys**:
  - Updating an existing key must **never** trigger eviction, because the count of distinct keys does not grow.
  - Always return early when updating:
    ```java
    if (node != null) {
        node.value = value;
        moveToHead(node);
        return; // Prevents allocating duplicate nodes and erroneous eviction
    }
    ```
- **`get` Counts as an Access**:
  - In LRU, "Used" applies to both reads and writes. Every `get(key)` call must promote the accessed node to the head (`moveToHead(node)`). Omitting this results in frequently-read keys being mistakenly evicted.
- **Encapsulation & Field Access in Inner Classes**:
  - `Node` is a private internal helper struct. In Java, an outer class has full direct access to the private and package-private fields of its nested classes (`node.value = value`), avoiding unnecessary getter/setter boilerplate.

### 4. Complexity
- **Custom Doubly Linked List + HashMap (`LRUCache`)**:
  - **`get(key)`**: Time: $O(1)$ average; Space: $O(1)$.
  - **`put(key, value)`**: Time: $O(1)$ average; Space: $O(1)$ auxiliary per call ($O(\text{capacity})$ total space).
- **LinkedHashMap (`LRUCache2`)**:
  - **`get(key)`**: Time: $O(1)$ average; Space: $O(1)$.
  - **`put(key, value)`**: Time: $O(1)$ average; Space: $O(1)$ auxiliary per call ($O(\text{capacity})$ total space).
