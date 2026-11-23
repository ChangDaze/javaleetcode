# 994. Rotting Oranges

### 1. Progressive Evolution of Approaches
- **Core Problem**: Given an $M \times N$ grid where `0` = empty cell, `1` = fresh orange, and `2` = rotten orange, every minute any fresh orange 4-directionally adjacent to a rotten orange becomes rotten. Return the **minimum number of minutes** until no fresh orange remains, or `-1` if at least one fresh orange can never be reached.
- **Key Insight (Multi-Source BFS on an Unweighted Grid)**:
  - Each cell is a vertex in an unweighted grid graph with up to 4 edges (up, down, left, right).
  - Because **all initially rotten oranges spread simultaneously**, we seed **every** `2` into the queue at minute `0` before starting BFS (rather than running separate traversals per source).
  - BFS naturally explores in expanding concentric waves (distance `0`, `1`, `2`, ...), guaranteeing that the **first time** any fresh orange is reached is its exact minimum rotting time.
- **Comparison of the Implementations**:

| Implementation | Paradigm / Data Structure | Time | Space / Overhead | Key Limitation / Advantage |
| :--- | :--- | :---: | :--- | :--- |
| **`orangesRotting`** | **Multi-Source BFS** (2 `ArrayDeque<Cell>` queues + `turn` flag + `HashSet<Cell>` + 4 direction branches) | **$O(M \cdot N)$** | $O(M \cdot N)$ (high constant factor: `HashSet` nodes + 4 eager `Cell` allocations per poll) | Good first working solution; **does not mutate** the input `grid`, but duplicates loop logic across `queue1`/`queue2`. |
| **`orangesRotting2`** | **Canonical Multi-Source BFS** (1 `ArrayDeque<Cell>` + `size` wave snapshot + in-place `grid = 2` + `freshCount` + `dirs` loop) | **$O(M \cdot N)$** | $O(M \cdot N)$ (low constant factor: no `HashSet`, lazy `Cell` allocation) | **Interview gold standard**: cuts code in half, eliminates hashing overhead, and cleanly prevents off-by-one time bugs. *(Mutates `grid`; use `boolean[M][N]` if read-only input is required.)* |
| *(Option A — Constraint-Driven Alternative, Not Implemented)* | **In-Place Timestamp Wavefront**: brute-force **loop the entire grid again and again until no cell changes** (stamping `2 -> 3 -> 4...`, no queue) | $O((M \cdot N)^2)$ | **$O(1)$** (**zero allocations**) | Different paradigm (iterative full-grid sweep). Despite worse Big-O, **runs very fast on small grids** (LC $M, N \le 10$) thanks to L1 CPU cache locality, linear prefetching, and zero GC overhead. |
| *(Option B — Optional Micro-Optimization, Not Implemented)* | **Flat Primitive `int[]` Queue BFS** (`int[M * N]` + `head`/`tail`/`waveEnd` + `r * cols + c` packing) | **$O(M \cdot N)$** | $O(M \cdot N)$ (**0 heap allocations per cell**) | Same BFS paradigm as `orangesRotting2`, replacing `ArrayDeque<Cell>` with a primitive array for peak CPU cache locality and zero GC pressure. |

---

### 2. Deep Dive into Each Approach

#### A. Java `record` as a C#-Style Value Tuple & Hash Key
- **Why `int[]` Fails in `HashSet` / `HashMap`**:
  - Java arrays inherit `Object.equals()` and `Object.hashCode()` (**reference equality**), so `new int[]{1, 2}` and `new int[]{1, 2}` are treated as **different keys** in a `HashSet<int[]>`.
- **Why Local `record Cell(int r, int c) {}` Solves It (Java 16+)**:
  - Declaring `record Cell(int r, int c) {}` directly inside a method acts like a named C# value tuple `(int r, int c)`.
  - The compiler automatically generates:
    1. Immutable `final` fields (`r`, `c`) so hash codes cannot mutate after insertion.
    2. Value-based `equals()`, `hashCode()`, and `toString()` from all components.
    3. Accessor methods `cell.r()` and `cell.c()` (no `get` prefix).
  - **Prefer dedicated primitive records over generic `Pair<Integer, Integer>`**: Java generics cannot hold primitives, so `record Cell(int r, int c) {}` avoids boxing every `int` coordinate.
  - **Accessors vs. direct fields**: Inside the same enclosing class, Java nestmate access allows `cell.r`, but `cell.r()` is more idiomatic and stays valid if the record is ever moved out.
  - **Gotcha**: Auto-generated `equals()`/`hashCode()` is only value-safe when components are primitives, `String`, or other immutable records. If a record wraps an array (e.g., `record State(int[] arr) {}`), array fields still compare by reference.

---

#### B. `orangesRotting` -> `orangesRotting2`: Refining Multi-Source BFS
Four targeted cleanups transform `orangesRotting` (~90 lines) into `orangesRotting2` (~45 lines) within the exact same BFS paradigm:

1. **Two Queues + `turn` Flag -> One Queue with FIFO Wave Snapshot (`int size = queue.size()`)**:
   - Because a FIFO queue strictly separates older items (current minute) from newly appended items (next minute), capturing `int size = queue.size()` at the top of each round lets a single queue process **exactly one minute's wave**:
     ```java
     int size = queue.size();
     for (int i = 0; i < size; i++) {
         Cell rottenCell = queue.poll();
         // Any newly rotted neighbor offered here waits for the next minute!
     }
     ```
2. **`HashSet<Cell> rottens` + `orangeCount` -> In-Place Marking + `freshCount`**:
   - Setting `grid[newRow][newColumn] = 2` **at enqueue time** (not dequeue time!) guarantees a fresh orange can never be enqueued twice—even when two rotten waves collide on it simultaneously (e.g., `[2, 1, 2]`).
   - Collapsing `rottenCount` + `orangeCount` into a single `freshCount` counter removes the `HashSet` entirely.
   - *Test isolation note*: Because `orangesRotting2` mutates `grid` in place, unit tests pass a deep copy (`copyGrid`) so shared test fixtures are never corrupted between runs.
3. **4 Copy-Pasted Direction Blocks -> `dirs` Array Loop**:
   - In `orangesRotting`, `up`, `down`, `left`, `right` eagerly allocated 4 `Cell` objects per popped orange—even for empty or out-of-bounds cells.
   - Iterating `int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}` eliminates duplicated boundary checks and only allocates `new Cell(newRow, newColumn)` when a valid fresh orange actually rots.
4. **Preventing the Classic BFS Off-by-One Bug (`while (freshCount != 0 && !queue.isEmpty())`)**:
   - A common BFS mistake is writing `while (!queue.isEmpty()) { time++; ... }`: after the final fresh orange rots, it is still enqueued, so one extra empty wave runs and increments `time` by `+1` too much (requiring `time - 1` hacks that break when `freshCount` starts at `0`).
   - Checking `freshCount != 0` in the `while` guard solves **every** boundary case uniformly:
     - `[[0]]` or `[[2]]`: `freshCount == 0` initially -> loop never runs -> returns `0`.
     - `[[2, 1]]`: minute `1` rots `(0, 1)` and drops `freshCount` to `0` -> loop exits immediately without draining `(0, 1)` -> returns `1`.
     - `[[1]]` or isolated fresh orange: queue drains while `freshCount > 0` -> returns `-1`.

---

#### C. Option A (Constraint-Driven Alternative Paradigm): In-Place Timestamp Wavefront ($O(1)$ Space)
> **Instant Mental Model**: Just **brute-force loop over the whole grid again and again until a full pass makes zero changes** (`progressed == false`), stamping `2 -> 3 -> 4...` so newly rotted cells wait for the next pass. No queue, $O(1)$ extra space—and **because of small grid constraints ($M, N \le 10$) + CPU-friendly linear memory scans**, it can actually run **very fast** in practice despite $O((M \cdot N)^2)$ Big-O!

Unlike `orangesRotting` and `orangesRotting2` (which are **frontier-driven graph traversals** tracking only active cells in a queue), this is a **different paradigm**: an **iterative full-grid sweep / cellular automaton** (like Bellman-Ford or Conway's Game of Life) that keeps repeating passes until reaching a fixed point.

- **Why It Can Run Very Fast in Practice (Constraints + CPU Architecture)**:
  1. **Tight problem constraints**: On LeetCode 994, $1 \le M, N \le 10$ (at most 100 `int`s = ~400 bytes—the entire grid fits inside a single **L1 CPU cache**!).
  2. **Sequential memory access & hardware prefetching**: Scanning `grid[r][c]` row-by-row hits contiguous memory with predictable branches, whereas `ArrayDeque<Cell>` chases heap pointers to `Cell` objects.
  3. **Zero heap allocations / zero GC pressure**: No `Queue`, no `Cell` records, no `HashSet` nodes.
- **Why Naive In-Place (`grid[nr][nc] = 2`) Fails**:
  - Scanning top-to-bottom, left-to-right and writing `2` immediately causes the loop to visit that newly rotted cell later in the **same pass**, letting the rot "teleport" across the whole grid in 1 minute.
- **The Timestamp Trick**:
  - Encode the minute directly in `grid`: `2` = rotten at minute `0`, `3` = minute `1`, `4` = minute `2`, ..., `t` = minute `t - 2`.
  - During pass `timestamp`, **only** expand cells where `grid[r][c] == timestamp`, stamping fresh (`1`) neighbors as `timestamp + 1`. Because `timestamp + 1 != timestamp`, newly rotted cells are automatically skipped until the next pass!
  - Reset `boolean progressed = false` at the start of each pass and flip `progressed = true` whenever at least one `1` is stamped to `timestamp + 1`. Stop when `!progressed`.
- **Step-by-Step Trace (`[[2,1,1],[1,1,0],[0,1,1]]`)**:
  ```text
  Initial (t = 2)    After pass t=2     After pass t=3     After pass t=4     After pass t=5
    2  1  1            2  3  1            2  3  4            2  3  4            2  3  4
    1  1  0    --->    3  1  0    --->    3  4  0    --->    3  4  0    --->    3  4  0
    0  1  1            0  1  1            0  1  1            0  5  1            0  5  6
                    (progressed=true)  (progressed=true)  (progressed=true)  (progressed=true)
                         t -> 3             t -> 4             t -> 5             t -> 6

  Pass t = 6: cell (2,2) has no fresh neighbors -> progressed = false -> stop!
  Final scan: no '1' left -> return timestamp - 2 = 6 - 2 = 4.
  ```
- **When to Use vs. Avoid**:
  - **Use** when an interviewer explicitly requires **$O(1)$ auxiliary space** (mutating `grid` allowed) or when inputs are guaranteed tiny.
  - **Avoid** on large grids: a single winding snake path of length $M \cdot N$ requires $M \cdot N$ full scans -> $O((M \cdot N)^2)$ (e.g., $10^{12}$ ops on a $1000 \times 1000$ grid vs. $10^6$ for BFS).

---

#### D. Option B (Optional Micro-Optimization): Flat Primitive `int[]` Queue
This is **not** a new algorithm—it is the exact same Multi-Source BFS as `orangesRotting2`, optimizing away `ArrayDeque` and `Cell` object allocations:
- Every cell enters the queue **at most once**, so a fixed `int[] queue = new int[rows * cols]` with `head` / `tail` pointers never overflows or resizes.
- Pack `(r, c)` into a single primitive `int`:
  - **Enqueue**: `queue[tail++] = r * cols + c;`
  - **Dequeue**: `int id = queue[head++]; int r = id / cols, c = id % cols;`
  - **Wave boundary**: `int waveEnd = tail; while (head < waveEnd) { ... }`
- Useful in high-throughput / low-GC Java code; in interviews, `ArrayDeque<Cell>` (`orangesRotting2`) is usually preferred for readability.

---

### 3. Java vs. C# Mapping

| Concept | Java | C# (.NET) | Notes |
| :--- | :--- | :--- | :--- |
| **Lightweight Coordinate Tuple** | `record Cell(int r, int c) {}` (local record, Java 16+) | `(int r, int c)` (`ValueTuple<int, int>`) | C# tuples are anonymous stack value types; Java `record` requires a short type declaration and allocates a heap object (until Project Valhalla). |
| **Component Access** | `cell.r()`, `cell.c()` | `cell.r`, `cell.c` | Java records generate method accessors (no `get` prefix); C# tuples expose public fields. |
| **Deconstruction** | `if (p instanceof Cell(int r, int c))` *(Java 21+)* | `var (r, c) = p;` | C# supports direct tuple deconstruction anywhere; Java uses record patterns in `instanceof`/`switch`. |
| **Key in `HashSet` / `HashMap`** | `record Cell(int r, int c)` works; `int[]` **fails** (reference equality) | `(int r, int c)` works (`ValueTuple` overrides `Equals`/`GetHashCode`) | Both Java `record` and C# `ValueTuple` give automatic value equality—watch out for array fields inside a Java `record`. |
| **FIFO Queue** | `Queue<Cell> q = new ArrayDeque<>()` | `Queue<(int r, int c)> q = new()` | `ArrayDeque` is backed by a circular array; C# `Queue<(int, int)>` stores value tuples inline with **zero per-cell heap allocation**. |
| **Deep Copy 2D Array** | Loop `Arrays.copyOf(grid[i], grid[i].length)` | `int[,]` `.Clone()` or jagged array row-copy loop | Java `grid.clone()` only shallow-copies row references; each inner row must be copied to isolate in-place mutations. |

---

### 4. Real-World Implementation & Use Cases
1. **Game AI & Robotics Pathfinding (Flow Fields / Dijkstra Maps)**:
   - In RTS games (e.g., StarCraft, Factorio) or warehouse robotics, instead of running A* separately for thousands of units, the engine seeds **all goals or hazards** simultaneously into a Multi-Source BFS to compute a single distance/flow field over the entire tile grid in one $O(M \cdot N)$ pass.
2. **Network Gossip, Alert Propagation & Disaster Simulation (Wildfire / Epidemic Modeling)**:
   - Modeling spread from multiple simultaneous ignition points (wildfires on GIS rasters, malware/patch gossip across mesh nodes) uses wave-by-wave Multi-Source BFS to compute worst-case convergence time (`time`) and detect isolated/partitioned regions (`-1`).
3. **Computer Vision & Image Processing (Distance Transform & Watershed Segmentation)**:
   - Multi-source flood-fill from foreground/seed pixels computes Manhattan distance maps (distance of every pixel to the nearest boundary) and grows labeled regions evenly at constant speed to segment overlapping objects.

---

### 5. Complexity
Let $M = \text{grid.length}$ (rows) and $N = \text{grid[0].length}$ (columns), so total cells $V = M \cdot N$.

- **Time Complexity**:
  - **`orangesRotting`**: **$O(M \cdot N)$** — initial scan visits all $M \cdot N$ cells; each cell enters and leaves a queue at most once and checks 4 neighbors with $O(1)$ average `HashSet` lookups.
  - **`orangesRotting2`**: **$O(M \cdot N)$** — same asymptotic bound ($1$ initial scan + each cell enqueued/dequeued at most once $\times 4$ constant-time array checks), with much smaller constant factors than `orangesRotting`.
  - *(Option A — In-Place Timestamp Wavefront)*: **$O((M \cdot N)^2)$** worst case — up to $M \cdot N$ passes on a snake-shaped path, each scanning all $M \cdot N$ cells.
  - *(Option B — Flat `int[]` Queue)*: **$O(M \cdot N)$** — identical to `orangesRotting2`, lowest constant factor.
- **Space Complexity**:
  - **`orangesRotting`**: **$O(M \cdot N)$** — worst case all cells are oranges in `rottens` (`HashSet`) and up to $O(M \cdot N)$ cells across `queue1` / `queue2`.
  - **`orangesRotting2`**: **$O(M \cdot N)$** — worst case (e.g., grid full of rotten oranges) `queue` holds up to $M \cdot N$ `Cell`s; no `HashSet` needed because visited state is stored in place in `grid`.
  - *(Option A — In-Place Timestamp Wavefront)*: **$O(1)$** auxiliary space (mutates `grid` in place).
  - *(Option B — Flat `int[]` Queue)*: **$O(M \cdot N)$** — single contiguous `int[M * N]` array ($4MN$ bytes).
