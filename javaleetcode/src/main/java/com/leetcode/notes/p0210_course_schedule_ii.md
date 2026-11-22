# 210. Course Schedule II

### 1. Progressive Evolution of Approaches
- **Core Problem**: Given `numCourses` courses labeled `0 .. numCourses - 1` and prerequisite pairs `[target, prereq]` (meaning you must take `prereq` before `target`), return **any valid ordering** to finish all courses, or an empty array `new int[0]` if a cycle makes it impossible.
- **Key Insight (Topological Sort on a DAG)**:
  - Model courses as **vertices ($V$)** and prerequisites as **directed edges ($E$)** pointing `prereq -> target`.
  - A valid schedule exists **if and only if** the graph is a **DAG (Directed Acyclic Graph)**, and any valid schedule is a **topological ordering** of that DAG (where every edge `u -> v` has `u` appearing before `v`).
  - Unlike LC 207 (which only returns `boolean`), LC 210 must record the ordering itself. Both **Kahn's Algorithm (BFS)** and **3-Color DFS** solve this in optimal **$O(V + E)$** time:
    - **BFS** records courses **front-to-back** as their remaining prerequisite count (`indegree`) drops to `0`.
    - **DFS** records courses **back-to-front** (reverse post-order) as each course finishes exploring all its downstream dependents.
- **Comparison of the Implementations**:

| Implementation | Mechanism / Data Structure | Time | Space / Overhead | Key Limitation / Advantage |
| :--- | :--- | :---: | :--- | :--- |
| **`findOrder`** | **Kahn's Algorithm (BFS)** (`adj[]` + `int[] indegree` + `ArrayDeque<Integer>` + `int[] order`) | **$O(V + E)$** | $O(V + E)$ (`List<Integer>[]` + `Queue` + `int[]`) | Clean, textbook iterative topological sort; removing the redundant `HashSet` cuts extra hashing & boxing. |
| **`findOrder2`** | **Fused-Array Kahn's BFS** (`order[]` acts as **both** the BFS queue and result array via `poll`/`offer` pointers) | **$O(V + E)$** | $O(V + E)$ (**no `ArrayDeque`, no BFS boxing**) | **Best iterative choice**: eliminates queue object allocation and `int -> Integer` boxing during traversal while keeping crystal-clear readability. |
| **`findOrder3`** | **3-Color DFS Post-Order** (`adj[]` + `byte[] state` + tail-to-head `order[idx[0]--]`) | **$O(V + E)$** | $O(V + E)$ (`List<Integer>[]` + `byte[]` + $O(V)$ call stack) | **No `indegree[]` array** and **immediate early-exit** on cycles; uses $O(V)$ recursion stack (safe for LC $V \le 2000$, risky for $10^5+$ chains). |
| *(Optional Micro-Optimization — Not Implemented)* | Forward-Star Primitive Graph (`int[] head, to, next`) + fused `order[]` queue | $O(V + E)$ | $O(V + E)$ (**0 boxed `Integer` objects**) | Replaces `List<Integer>[]` with flat primitive arrays for near-zero GC pressure (~1–2 ms on LC), at the expense of interview readability. |

---

### 2. Deep Dive into Each Approach

#### A. `findOrder`: Kahn's BFS & Initial Gotchas Resolved
1. **Java `List<Integer>.toArray()` vs. Primitive `int[]` Trap**:
   - Writing `return takenCourse.size() == numCourses ? order.toArray() : new int[0];` when `order` is a `List<Integer>` fails to compile:
     - `order.toArray()` returns `Object[]`, **not** `int[]`.
     - Even `order.toArray(new Integer[0])` returns `Integer[]`, and Java will **not** auto-unbox `Integer[]` into primitive `int[]`.
   - Three valid ways to handle it:
     - **Stream one-liner**: `order.stream().mapToInt(Integer::intValue).toArray()` (clean, but incurs Stream + unboxing overhead).
     - **Manual copy loop**: allocate `int[] res = new int[numCourses]` and copy `order.get(i)` (faster, no Stream overhead).
     - **Direct `int[]` allocation (Best)**: since the final size is known up front (`numCourses`), write directly into `int[] order = new int[numCourses]` using `order[idx++] = curr;` — zero boxing and zero second copy.
2. **Queue Initialization Bug (`for (int node : indegree)` vs. Index Loop)**:
   - Enhanced-for `for (int node : indegree)` iterates over the **in-degree counts** (e.g., `0, 1, 2`), *not* the course IDs, and enqueues every slot regardless of value.
   - **Fix**: iterate by course index `i` and only seed nodes whose `indegree[i] == 0`:
     ```java
     for (int i = 0; i < numCourses; i++) {
         if (indegree[i] == 0) {
             queue.offer(i);
         }
     }
     ```
3. **Why `HashSet<Integer> takenCourse` Is Redundant**:
   - Initially, every course with `indegree[i] == 0` is enqueued **at most once**.
   - Inside BFS, a neighbor `next` is enqueued only on the single transition `--indegree[next] == 0` (`1 -> 0`), which can also happen **at most once** per course.
   - Therefore, **no course can ever enter the queue twice**, guaranteeing `idx == takenCourse.size()`. Removing `takenCourse` eliminates $O(V)$ hash table overhead and simplifies the return to `return idx == numCourses ? order : new int[0];`.

---

#### B. `findOrder2`: Using `order[]` as the BFS Queue Itself
- **Key Observation**: Every course popped from `queue` in `findOrder` is immediately appended to `order[]` in the exact same FIFO sequence. Maintaining both an `ArrayDeque<Integer>` and an `int[] order` stores the same numbers twice.
- **Two-Pointer Array Queue (`poll` & `offer`)**:
  ```text
  order[]:  [  c0  |  c1  |  c2  |  c3  |  c4  |  _  |  _  ]
                          ^                    ^
                         poll                offer

  [0 .. poll - 1]         -> already taken & neighbors relaxed
  [poll .. offer - 1]     -> currently waiting in the BFS queue
  [offer .. numCourses-1] -> not yet unlocked (indegree > 0)
  ```
- **Operations**:
  - **Offer**: `order[offer++] = node;`
  - **Poll**: `int curr = order[poll++];`
  - **Loop condition**: `while (poll < offer)` (equivalent to `!queue.isEmpty()`)
  - **Completion check**: When the loop terminates, `poll == offer`, so `return poll == numCourses ? order : new int[0];` verifies all courses were scheduled with no cycle left behind.

---

#### C. `findOrder3`: 3-Color DFS Post-Order & the `idx[0]` Idiom
1. **Why Post-Order Produces *Reverse* Topological Order**:
   - With edges `prereq -> target`, `hasCycle(curr)` recursively finishes **every course that depends on `curr`** before executing `order[idx[0]--] = curr`.
   - That means sink courses (no dependents left) finish **first**, and root prerequisites finish **last**.
   - **Tail-to-Head Trick**: Starting `idx` at `numCourses - 1` and filling `order[idx[0]--] = curr` reverses post-order on the fly with zero extra memory or reversal pass.
   - *(Alternative)*: If you store edges in reverse (`target -> prereq`), DFS visits prerequisites *before* `curr`, so post-order fills `order` front-to-back (`order[idx[0]++] = curr`).
2. **Trace on Out-of-Order Starting Node (`1 -> 0`, Outer Loop Starts at `i = 0`)**:
   ```text
   Graph: 1 -> 0    (prerequisite 1 must come before 0)
   Initial: state = [0, 0], order = [_, _], idx = [1]

   1. Outer loop i = 0 (state[0] == 0):
      - state[0] = 1 -> adj[0] is empty -> state[0] = 2
      - order[1] = 0, idx[0] becomes 0

   2. Outer loop i = 1 (state[1] == 0):
      - state[1] = 1 -> visits neighbor 0 -> state[0] == 2 (safe, returns false immediately!)
      - state[1] = 2
      - order[0] = 1, idx[0] becomes -1

   Result: order = [1, 0]  (valid: 1 appears before 0!)
   ```
3. **Why `int[] idx = new int[]{numCourses - 1}` (`idx[0]--`) Instead of `int idx`?**
   - Java is strictly **pass-by-value** and has no `ref int` parameter modifier.
   - Passing a primitive `int idx` gives every recursive stack frame its own **local copy**. When a child call runs `idx--`, the parent's `idx` stays unchanged, causing subsequent writes to overwrite the same slot in `order[]`.
   - Wrapping the index in a 1-element array (`int[] idx`) passes a copy of the **array reference** pointing to the same heap slot, so `idx[0]--` is visible across all recursive frames.
   - **Trade-off vs. Instance Field (`private int idx`)**:
     - `int[] idx`: Keeps `findOrder3` completely **stateless and thread-safe** (safe to invoke concurrently on a shared solver instance), at the cost of `[0]` syntax.
     - `private int idx`: Cleaner syntax (`order[idx--] = curr`), common in LeetCode, but introduces mutable object state.
4. **BFS (`findOrder2`) vs. DFS (`findOrder3`)**:
   - On graphs with branching (e.g., Diamond DAG `0 -> {1, 2} -> 3`), both return valid—but potentially **different**—orderings (e.g., BFS `[0, 1, 2, 3]` vs. DFS `[0, 2, 1, 3]`).
   - Prefer **Kahn's BFS (`findOrder2`)** in production when graph depth can be large (iterative, no `StackOverflowError`, natural level-by-level parallelism).
   - Prefer **3-Color DFS (`findOrder3`)** when you want immediate cycle early-exit or need to reconstruct the exact cycle path from the recursion stack.

---

#### D. Testing Topological Orderings & Low-Level Optimization
- **Why Unit Tests Use `assertValidOrder` Instead of Only `assertArrayEquals`**:
  - A DAG can have exponentially many valid topological orderings, and BFS vs. DFS naturally pick different ones.
  - Instead of locking tests to one traversal's output, `assertValidOrder` verifies the **mathematical contract**:
    1. `order.length == numCourses`
    2. Every course `0 .. numCourses - 1` appears **exactly once** (tracking `pos[course] = index`)
    3. For every edge `[target, prereq]`, `pos[prereq] < pos[target]`
  - For chains with a unique topological order (e.g., `3 -> 2 -> 1 -> 0`), tests assert *both* `assertValidOrder` and exact `assertArrayEquals`.
- **Optional Forward-Star Optimization**:
  - Pairing `findOrder2`'s fused `order[]` queue with a flat primitive adjacency list (`int[] head`, `int[] to`, `int[] next`) eliminates the remaining `List<Integer>[]` object allocations and edge boxing.

---

### 3. Java vs. C# Mapping

| Concept | Java | C# (.NET) | Notes |
| :--- | :--- | :--- | :--- |
| **`List` of ints to `int[]`** | `list.stream().mapToInt(Integer::intValue).toArray()` | `list.ToArray()` | Java `List<Integer>.toArray()` returns `Object[]`; C# `List<int>` is reified and converts directly to `int[]`. |
| **Pass Mutable `int` to Recursion** | `int[] idx = { n - 1 };` (`idx[0]--`) | `ref int idx` (`idx--`) | Java lacks `ref` parameters; a 1-element `int[]` (or an instance field) is the standard workaround. |
| **Fused Array Queue** | `int[] order` + `poll`/`offer` | `int[] order` + `poll`/`offer` | Works identically in both; bigger speedup in Java because `ArrayDeque<Integer>` boxes `int`, whereas C# `Queue<int>` does not. |
| **Empty Array Return** | `new int[0]` | `Array.Empty<int>()` or `[]` (C# 12) | C# `Array.Empty<int>()` returns a cached singleton with zero allocation. |
| **Adjacency List Array** | `List<Integer>[] adj = new ArrayList[n];` | `List<int>[] adj = new List<int>[n];` | C# supports generic array creation natively without unchecked warnings. |

---

### 4. Real-World Implementation & Use Cases
1. **Infrastructure-as-Code & Cloud Resource Provisioning (Terraform, Pulumi, CloudFormation)**:
   - Cloud resources form a dependency DAG (e.g., `VPC -> Subnets -> Security Groups -> RDS Database -> Application Service`). The planner runs topological sort to emit the exact creation order—and simply **reverses** that topological order during `terraform destroy`!
2. **Compiler Instruction Scheduling & Build Execution Plans (LLVM, Bazel, Gradle)**:
   - Beyond checking for cycles (LC 207), compilers must emit a concrete linear sequence of machine instructions respecting data-dependency edges (`write register -> read register`), and build tools generate concrete task execution orders (with Kahn's wave-by-wave `indegree == 0` sets running in parallel across CPU cores).
3. **Database Schema Migrations & Service Bootstrapping (Flyway, Kubernetes Init Dependencies)**:
   - Tables linked by foreign keys (e.g., `users -> orders -> order_items`) must be created, seeded, or backed up in topological order to avoid foreign-key constraint violations (and truncated/dropped in reverse topological order).

---

### 5. Complexity
Let $V = \text{numCourses}$ (vertices) and $E = \text{prerequisites.length}$ (directed edges).

- **Time Complexity**:
  - **`findOrder` (Kahn's BFS)**: **$O(V + E)$** — initializing `adj[]` takes $O(V)$, building edges & `indegree[]` takes $O(E)$, seeding `queue` takes $O(V)$, and each vertex is polled at most once while each edge is relaxed at most once ($\le V + E$).
  - **`findOrder2` (Fused-Array Kahn's BFS)**: **$O(V + E)$** — identical asymptotic work to `findOrder`, with lower constant factors due to zero `ArrayDeque` / `Integer` boxing overhead and better CPU cache locality.
  - **`findOrder3` (3-Color DFS Post-Order)**: **$O(V + E)$** — each node transitions `0 -> 1 -> 2` at most once ($O(V)$) and its adjacency list is scanned at most once across the entire run ($\sum \deg^+(u) = E$).
- **Space Complexity**:
  - **`findOrder`**: **$O(V + E)$** — `adj[]` takes $O(V + E)$, plus $O(V)$ each for `indegree[]`, `ArrayDeque<Integer>`, and `order[]`.
  - **`findOrder2`**: **$O(V + E)$** — `adj[]` takes $O(V + E)$, plus $O(V)$ for `indegree[]` and the fused `order[]` (saves the separate $O(V)$ boxed `ArrayDeque`).
  - **`findOrder3`**: **$O(V + E)$** — `adj[]` takes $O(V + E)$, plus $O(V)$ for `byte[] state`, `order[]`, and up to $O(V)$ recursion call stack depth on a linear chain.
