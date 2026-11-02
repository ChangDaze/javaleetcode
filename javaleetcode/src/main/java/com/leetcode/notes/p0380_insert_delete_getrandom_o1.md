# 380. Insert Delete GetRandom O(1)

### 1. Dynamic Array + Hash Map (`List<Integer>` + `Map<Integer, Integer>`)
- **Core Observation**: No single standard data structure supports `insert`, `remove`, and uniform `getRandom` all in $O(1)$ average time:
  1. **`HashSet` / `HashMap`**: Supports $O(1)$ `insert` and `remove`, but cannot pick a random element in $O(1)$ because hash buckets are sparse and non-contiguous (indexing requires $O(N)$ iteration).
  2. **`ArrayList`**: Supports $O(1)$ `insert` (append at end) and $O(1)$ `getRandom` (via contiguous index `list.get(randomIndex)`), but removing an arbitrary element requires $O(N)$ lookup and $O(N)$ shifting of subsequent elements.
- **Hybrid Solution**: Combine both structures:
  - `List<Integer> list`: Stores the actual elements contiguously to support $O(1)$ random access by index.
  - `Map<Integer, Integer> map`: Maps each `val -> index in list` so we can locate any element's position in $O(1)$.
- **The $O(1)$ Swap-and-Pop Deletion Trick**:
  - Removing from the **middle** of an `ArrayList` is $O(N)$ because elements shift left, invalidating indices in `map`.
  - Removing the **last** element of an `ArrayList` (`list.removeLast()`) is strictly $O(1)$ with no shifting.
  - Therefore, to delete `val` at `swapIndex`:
    ```text
    Before remove(20):
    list: [10, 20, 30, 40]    map: {10: 0, 20: 1, 30: 2, 40: 3}
               ^       ^
           swapIndex  last

    1. Overwrite swapIndex with last element (40):
    list: [10, 40, 30, 40]    map: {10: 0, 20: 1, 30: 2, 40: 1} (updated 40's index to 1)

    2. Pop last element from list & remove 20 from map:
    list: [10, 40, 30]        map: {10: 0, 40: 1, 30: 2}
    ```

### 2. Optimization: Reducing Hash Lookups (`RandomizedSet2`)
- **Insert Order (`map.put` before `list.add`)**:
  - Calling `map.put(val, list.size())` **before** `list.add(val)` directly uses the pre-insertion `list.size()` as the new element's 0-based index, avoiding `list.size() - 1`.
- **Collapsing 4 Hash Lookups into 1–2 in `remove(val)`**:
  - In `RandomizedSet`, calling `containsKey(val)`, `get(val)`, `replace(swapValue, ...)`, and `remove(val)` performs 4 hash table operations.
  - In `RandomizedSet2`, `Integer removeIndex = map.remove(val)` checks existence, retrieves the index, and deletes the entry in a **single** hash lookup:
    ```java
    Integer removeIndex = map.remove(val);
    if (removeIndex == null) {
        return false;
    }
    int lastVal = list.removeLast();
    if (removeIndex < list.size()) { // Only swap if removed element wasn't already the last element
        list.set(removeIndex, lastVal);
        map.put(lastVal, removeIndex);
    }
    ```

### 3. Key Gotchas & Java Details
- **Single-Element / Remove-Last-Element Edge Case**:
  - When `val` is already the last element in `list` (or the **only** element in the set), `swapValue == val`.
  - In `RandomizedSet`: `map.remove(val)` **must** be called **after** `map.replace(swapValue, swapIndex)`. If `map.remove(val)` is called before `map.put(swapValue, swapIndex)`, `val` gets re-inserted into `map`!
  - In `RandomizedSet2`: The guard `if (removeIndex < list.size())` after `list.removeLast()` cleanly skips the swap and `map.put` altogether when removing the last element.
- **Random Number Generation Best Practices**:
  - **Modern Java (`ThreadLocalRandom`)**: `ThreadLocalRandom.current().nextInt(list.size())` requires no field allocation and avoids atomic seed contention.
  - **Classic Interview (`java.util.Random`)**: `private final Random rand = new Random();` stored as a class field (never re-instantiate `new Random()` inside `getRandom()`). In production unit testing, `Random` can be injected via constructor overloading (`new Random(seed)`) for deterministic tests.
  - **Avoid `Math.random()`**: Requires floating-point multiplication and casting `(int) (Math.random() * list.size())`, which is slower and prone to parenthesization bugs.
- **Java Collection APIs Used**:
  - `list.set(index, element)`: Overwrites the element at `index` in $O(1)$.
  - `list.getLast()` / `list.removeLast()`: Java 21 `SequencedCollection` APIs (equivalent to `list.get(list.size() - 1)` and `list.remove(list.size() - 1)` in older Java versions).
  - `map.put(key, val)` vs `map.replace(key, val)`: Both overwrite an existing key's value in $O(1)$, while `replace` only updates if the key already exists.

### 4. Real-World Implementation & Use Cases
- **Where This Pattern Is Used in Production**:
  1. **Random Load Balancer / Service Discovery**: Registering healthy server IPs (`insert`), deregistering unhealthy nodes on heartbeat failure (`remove`), and routing each incoming request to a random healthy server (`getRandom`) in $O(1)$.
  2. **Live Giveaway / Raffle Pool**: Users join (`insert`) or leave (`remove`) a livestream lobby, and the system picks random winners (`getRandom`) instantly.
  3. **Game Engine Entity Management (ECS)**: Storing active game objects contiguously in memory for cache-friendly iteration and random sampling while allowing $O(1)$ despawning via swap-and-pop.
- **Testable Production Implementation (Generic + Dependency Injection)**:
  ```java
  public class ServerPool<T> {
      private final Map<T, Integer> indexMap = new HashMap<>();
      private final List<T> servers = new ArrayList<>();
      private final Random rand;

      // Production constructor
      public ServerPool() {
          this(new Random());
      }

      // Testable constructor: pass `new Random(42L)` in unit tests for 100% deterministic results
      public ServerPool(Random rand) {
          this.rand = rand;
      }

      public boolean register(T server) {
          if (indexMap.containsKey(server)) return false;
          indexMap.put(server, servers.size());
          servers.add(server);
          return true;
      }

      public boolean deregister(T server) {
          Integer idx = indexMap.remove(server);
          if (idx == null) return false;
          T last = servers.removeLast();
          if (idx < servers.size()) {
              servers.set(idx, last);
              indexMap.put(last, idx);
          }
          return true;
      }

      public T pickRandomServer() {
          if (servers.isEmpty()) throw new IllegalStateException("No healthy servers available");
          return servers.get(rand.nextInt(servers.size()));
      }
  }
  ```

### 5. Complexity
- **`RandomizedSet` & `RandomizedSet2`**:
  - **`insert(val)`**: Time: $O(1)$ average (amortized for `ArrayList` resizing); Space: $O(1)$ auxiliary per call.
  - **`remove(val)`**: Time: $O(1)$ average; Space: $O(1)$.
  - **`getRandom()`**: Time: $O(1)$; Space: $O(1)$.
  - **Total Space**: $O(N)$ to store $N$ elements in `list` and `map`.
