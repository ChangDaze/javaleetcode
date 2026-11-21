# 207. Course Schedule

### 1. Progressive Evolution of Approaches
- **Core Problem**: Given `numCourses` courses labeled `0 .. numCourses - 1` and prerequisite pairs `[target, prereq]` (meaning you must take `prereq` before `target`), determine whether it is possible to finish all courses.
- **Key Insight (Directed Graph / DAG Detection)**:
  - Model courses as **vertices ($V$)** and prerequisite rules as **directed edges ($E$)** pointing `prereq -> target`.
  - All courses can be finished **if and only if** the directed graph is **cycle-free** (a **DAG** — Directed Acyclic Graph).
  - Peeling courses with zero remaining prerequisites works, but scanning *all* courses on every unlock takes $O(V \cdot (V + E))$. Flipping edge storage to forward adjacency lists (`adj[prereq] -> targets`) plus either an **in-degree queue (Kahn's BFS)** or **3-color DFS** touches each vertex and edge a constant number of times, reducing total time to **$O(V + E)$**.
- **Comparison of the Implementations**:

| Implementation | Mechanism / Data Structure | Time | Space / Overhead | Key Limitation / Advantage |
| :--- | :--- | :---: | :--- | :--- |
| **`canFinish`** | Iterative Peeling (`records[target] -> prereqs` + `HashSet<Integer> checked`) | $O(V \cdot (V + E))$ | $O(V + E)$ (`List<Integer>[]` + `HashSet`) | Intuitive first derivation of topological peeling, but stores edges backwards (`target -> prereq`) and linearly scans all lists per unlocked course. |
| **`canFinish2`** | **Kahn's Algorithm (BFS)** (`adj[prereq] -> targets` + `int[] indegree` + `ArrayDeque`) | **$O(V + E)$** | $O(V + E)$ (`List<Integer>[]` + `int[]` + `Queue`) | **Iterative & Production-Safe**: No recursion stack overflow risk; directly extends to **LC 210 (Course Schedule II)** to output a valid course order. |
| **`canFinish3`** | **3-Color DFS Cycle Detection** (`adj[prereq] -> targets` + `byte[] state`) | **$O(V + E)$** | $O(V + E)$ (`List<Integer>[]` + `byte[]` + $O(V)$ call stack) | **Early-Exit**: Returns `false` the instant any back-edge (cycle) is discovered, without peeling all acyclic nodes first. |
| *(Optional Extension of `canFinish2` — Not Implemented)* | Forward-Star Primitive Graph (`int[] head, next, to`) + `int[] queue` | $O(V + E)$ | $O(V + E)$ (**0 boxed `Integer` objects**) | Low-level micro-optimization of `canFinish2` (saved for future practice): replaces `List<Integer>[]` and `ArrayDeque<Integer>` with flat `int[]` arrays to avoid heap/boxing overhead, at the cost of readability. |

---

### 2. Deep Dive into Each Approach

#### A. `canFinish`: Iterative Peeling, Java Gotchas & Why It's $O(V \cdot (V + E))$ (Not $O(V^3)$)
1. **Java Generic Array Gotcha (`List<Integer>[]`)**:
   - Java forbids `new List<Integer>[numCourses]` or `new ArrayList<Integer>[numCourses]` because arrays verify their element type at runtime while generics are erased.
   - Workaround: allocate a raw array `List<Integer>[] records = new ArrayList[numCourses]` and initialize every slot in a loop (`records[i] = new ArrayList<>()`), or use the strictly type-safe `List<List<Integer>>`.
2. **`List<Integer>.remove(int)` vs. `remove(Object)` Trap**:
   - `List<Integer>` has two overloads: `remove(int index)` and `remove(Object o)`.
   - Calling `records[j].remove(i)` with a primitive `int i` removes by **index**, causing `IndexOutOfBoundsException` or deleting the wrong course.
   - **Fix**: Box to `Integer` via `records[j].remove(Integer.valueOf(i))` (or `(Integer) i`).
   - *Bonus cleanup*: `remove(Object)` already returns `boolean`, so checking `if (records[j].contains(i))` before `remove(...)` scans `records[j]` twice unnecessarily.
3. **Why One Pass Over `records[]` Costs $O(V + E)$ ("Boxes & Balls" Mental Model)**:
   - Think of `records[0 .. V-1]` as **`V` boxes**, and the total prerequisites across all lists combined as **`E` balls** distributed among those boxes.
   - Looping `for (int j = 0; j < V; j++)` and scanning each `records[j]` opens all `V` boxes (`V` steps) and inspects all `E` balls once (`E` steps) $\rightarrow$ **$O(V + E)$**, *not* $O(V \cdot E)$.
   - Both terms stay in $O(V + E)$ because either can dominate (e.g., $V = 1000, E = 0 \Rightarrow V$ dominates; $V = 10, E = 45 \Rightarrow E$ dominates).
4. **Why 3 Nested Loops Here Don't Automatically Multiply to $O(V^3)$**:
   - Outer `while` + `for (i)` can run up to $V$ rounds on a reverse chain (`0 -> 1 -> ... -> V-1`), performing $O(V^2)$ condition checks.
   - However, `checked.add(i)` acts as a **one-time gate**: each course `i` enters the `for (j)` inner loop **at most once across the entire execution** (at most $V$ times total, *not* $V^2$ times).
   - Total work = $O(V^2) + V \cdot O(V + E) = O(V \cdot (V + E))$.
   - *(On sparse graphs where $E \approx V$, this is $O(V^2)$; it only degrades to $O(V^3)$ on dense graphs where $E \approx V^2$.)*

---

#### B. `canFinish2`: Kahn's Algorithm (BFS) — Direct 1-to-1 Refinement of `canFinish`
- **Graph Terminology**:
  - **`adj`** (*adjacency list*): `adj[u]` stores all direct neighbors reachable from `u`.
  - **`edge`**: Each `[target, prereq]` pair represents a directed arrow `prereq -> target` (*"finishing `prereq` unlocks `target`"*).
- **Direct Mapping from `canFinish` to `canFinish2`**:

| `canFinish` (Iterative Peeling) | `canFinish2` (Kahn's BFS) | Why It's Faster |
| :--- | :--- | :--- |
| `records[target]` (list of prereqs) | `adj[prereq]` (dependents) + `int[] indegree` | We only need *how many* prerequisites remain, not which ones. |
| `records[j].remove(Integer.valueOf(i))` | `--indegree[next]` | $O(1)$ integer decrement instead of scanning all $V$ lists. |
| `while (true)` + `for (i)` + `checked` | `Queue<Integer> queue = new ArrayDeque<>()` | A course's `indegree` hits `0` at most once, so `checked` is unnecessary. |
| Final `isEmpty()` scan | `return taken == numCourses;` | Counts taken courses on the fly; cycle nodes never reach `indegree == 0`. |

- **Counting the Exact Passes (`3V + 2E` $\rightarrow$ $O(V + E)$)**:
  1. Initialize `adj[0..V-1]`: $V$
  2. Populate `adj` & `indegree` from `prerequisites`: $E$
  3. Seed `queue` with `indegree[i] == 0`: $V$
  4. BFS loop (each node polled $\le 1$ time, each edge relaxed $\le 1$ time): $\le V + E$
  - Total $\le 3V + 2E = O(V + E)$ after dropping constant factors.

---

#### C. `canFinish3`: 3-Color DFS Cycle Detection
- **Why 3 States Instead of `boolean[] visited`?**
  In a **directed** graph, hitting a previously visited node does *not* automatically mean there is a cycle:
  ```text
  Diamond DAG (VALID -> true):         Cycle (INVALID -> false):
          0                                   0
         / \                                 / ^
        v   v                               v   \
        1   2                               1 -> 2
         \ /
          v
          3
  ```
  - `0` (**Unvisited**): Never explored yet.
  - `1` (**Visiting / On Current Path**): Currently active on the DFS recursion stack. Hitting a `1` means we found a **back-edge $\rightarrow$ cycle!**
  - `2` (**Visited / Proven Safe**): Fully explored; no cycle is reachable from this node.

- **The Two Jobs of State `2` (Memoization)**:
  1. **Outer Loop (`if (state[i] == 0 && ...)`)**: Avoids starting a fresh DFS from nodes already verified in earlier traversals.
  2. **Inside `hasCycle` (`if (state[curr] == 2) return false;`)**: Stops immediately when *another* DFS branch merges into an already-verified node.
  - *Why #2 is critical*: On a chain of 30 diamonds (`assertDiamondChainMemoization`, 91 nodes), there are $2^{30}$ ($\approx 1$ billion) distinct paths from start to end. Without state `2`, resetting visited nodes back to `0` re-walks the tail exponentially ($O(2^V)$ TLE). With state `2`, every node transitions `0 -> 1 -> 2` at most once ($O(V + E)$).

- **Kahn's BFS (`canFinish2`) vs. 3-Color DFS (`canFinish3`)**:

| Criterion | Kahn's BFS (`canFinish2`) | 3-Color DFS (`canFinish3`) |
| :--- | :--- | :--- |
| **Time / Space** | $O(V + E)$ / $O(V + E)$ | $O(V + E)$ / $O(V + E)$ |
| **Early Exit on Cycle** | Usually peels acyclic nodes first | **Immediate exit** on first back-edge |
| **Stack Overflow Risk** | **None** (iterative queue) | $O(V)$ recursion depth (safe for LC $V \le 2000$, risky for $10^5+$) |
| **Ordering Output (LC 210)** | Natural (order popped from queue) | Requires reversing postorder |

---

#### D. Testing Idiom & Optional Future Micro-Optimization
- **Testing Idiom (`edges.toArray(new int[0][])`)**:
  - `new int[0][]` is a real 0-row `int[][]` (valid in Java because 2D arrays are jagged arrays where only the outer dimension is required).
  - Because Java generics erase `int[]` at runtime, `List.toArray(T[] a)` uses `a`'s runtime class (`int[][].class`) as a **type token**. When `a.length < size`, the JDK intentionally ignores `a` and allocates a right-sized `int[size][]` for convenience (and thread safety on concurrent collections).
  - Java 11+ provides an equivalent built-in array constructor reference: `edges.toArray(int[][]::new)`.
- **Future Extension of `canFinish2` — Forward-Star Primitive Graph *(Not Implemented Yet)***:
  - Same $O(V + E)$ Kahn's BFS algorithm as `canFinish2`, but replaces `List<Integer>[] adj` with three flat `int[]` arrays acting as array-backed linked lists (`head[u]` = first edge from `u`, `to[e]` = target node, `next[e]` = next edge from `u`, iterated via `for (int e = head[u]; e != -1; e = next[e])`) and replaces `Queue<Integer>` with `int[] queue` + `front`/`back` pointers.
  - Intentionally not implemented in `p0207_course_schedule.java` since it is a low-level zero-boxing micro-optimization rather than a core graph concept; kept here as a reference for future practice.

---

### 3. Java vs. C# Mapping

| Concept | Java | C# (.NET) | Notes |
| :--- | :--- | :--- | :--- |
| **Adjacency List Array** | `List<Integer>[] adj = new ArrayList[n];` | `List<int>[] adj = new List<int>[n];` | C# has **reified generics** (preserved at runtime), so generic arrays compile cleanly without unchecked warnings. |
| **Remove Value vs. Index** | `list.remove(Integer.valueOf(i))` vs. `list.remove(i)` | `list.Remove(i)` vs. `list.RemoveAt(i)` | C# separates value removal (`Remove`) and index removal (`RemoveAt`) into distinct method names, eliminating Java's overload trap. |
| **BFS Queue** | `Queue<Integer> q = new ArrayDeque<>();` (`offer`/`poll`) | `Queue<int> q = new();` (`Enqueue`/`Dequeue`) | C# `Queue<int>` stores unboxed primitive `int`s directly; Java `ArrayDeque<Integer>` boxes each `int`. |
| **3-Color State Array** | `byte[] state = new byte[n];` | `byte[] state = new byte[n];` | Both default-initialize to `0` (`unvisited`); 1 byte per node keeps cache footprint minimal. |
| **Convert `List<int[]>` to `int[][]`** | `edges.toArray(new int[0][])` or `edges.toArray(int[][]::new)` | `edges.ToArray()` | C# needs no dummy type-token array because `List<T>` retains `T` at runtime. |

---

### 4. Real-World Implementation & Use Cases
1. **Build Systems & Package Managers (Maven, Gradle, npm, Cargo, Bazel)**:
   - Software modules and third-party libraries declare dependency graphs. Before compiling or installing, the resolver runs Kahn's algorithm or 3-color DFS to detect **circular dependencies** (and in Maven/Gradle, reports the exact cycle path) and compute a valid parallelizable build order.
2. **Spreadsheet Formula Recalculation Engines (Excel / Google Sheets)**:
   - Cell `C1 = A1 + B1` defines directed dependencies `A1 -> C1` and `B1 -> C1`. Whenever a cell edits, the engine validates the dependency graph for **circular references** (cycles) and recalculates dirty cells in topological order.
3. **Workflow & Data Pipeline Orchestrators (Apache Airflow, Prefect, CI/CD DAGs)**:
   - ETL tasks, microservice startup sequences, and database schema migrations are modeled as DAGs. Kahn's algorithm naturally schedules every task whose upstream dependencies (`indegree == 0`) have completed, enabling maximum parallel execution.

---

### 5. Complexity
Let $V = \text{numCourses}$ (vertices) and $E = \text{prerequisites.length}$ (edges).

- **Time Complexity**:
  - **`canFinish`**: **$O(V \cdot (V + E))$** — up to $V$ ready courses are unlocked; each unlock scans all $V$ lists containing up to $E$ total entries ($O(V + E)$ per unlock), plus up to $O(V^2)$ outer-loop checks on a reverse chain.
  - **`canFinish2` (Kahn's BFS)**: **$O(V + E)$** — building `adj` and `indegree` takes $O(V + E)$, seeding the queue takes $O(V)$, and during BFS each vertex is enqueued/dequeued at most once ($\le V$) and each directed edge is decremented at most once ($\le E$), totaling $\le 3V + 2E$.
  - **`canFinish3` (3-Color DFS)**: **$O(V + E)$** — every vertex transitions `0 -> 1 -> 2` at most once ($V$), so each vertex's adjacency list is iterated at most once across the entire run (sum of all list lengths = $E$).
- **Space Complexity**:
  - **`canFinish`**: **$O(V + E)$** for `records[]` + $O(V)$ for `HashSet<Integer> checked`.
  - **`canFinish2`**: **$O(V + E)$** for `adj[]` + $O(V)$ for `indegree[]` and `ArrayDeque` queue.
  - **`canFinish3`**: **$O(V + E)$** for `adj[]` + $O(V)$ for `byte[] state` + up to $O(V)$ DFS recursion stack depth on a linear chain.
