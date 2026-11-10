# 1226. The Dining Philosophers

### 1. Progressive Evolution of Concurrency Approaches
- **Core Problem**: 5 philosophers (`0` to `4`) sit clockwise around a circular table facing inward, with 5 shared forks between them. Each call to `wantsToEat()` represents **one complete meal** where a philosopher must execute all 5 `Runnable` hooks (`pickLeftFork`, `pickRightFork`, `eat`, `putLeftFork`, `putRightFork`).
- **Key Concurrency Rules**:
  1. **No Race Conditions on Shared Forks**: The `Runnable` hooks only log actions—they do **not** lock forks. Adjacent philosophers share a physical fork (`rightFork = philosopher`, `leftFork = (philosopher + 1) % 5`), so each fork must be protected by a mutex/semaphore (`new Semaphore(1)`), and `.run()` must be called **inside** the acquired lock (`acquire()` $\rightarrow$ `pick...run()` and `put...run()` $\rightarrow$ `release()`).
  2. **No Deadlock (Breaking Circular Wait / Hold-and-Wait)**: If all 5 philosophers simultaneously pick up their left fork first, all 5 forks are taken and every philosopher waits forever for their right fork.
- **Comparison of the Implementations**:

| Implementation | Mechanism | Synchronization Objects | Max Concurrent Eaters | Key Limitation / Advantage |
| :--- | :--- | :--- | :--- | :--- |
| **`DiningPhilosophers`** | `fork0..fork4` (`Semaphore(1)`) + `leftSem(4)` & `rightSem(4)` + `switch` | 7 `Semaphore`s | 2 philosophers | First working version; verbose `switch-case` blocks and redundant `rightSem(4)`. |
| **`DiningPhilosophers2`** | `Semaphore[5] forks` + `Semaphore(4) tableLimit` | 6 `Semaphore`s | 2 philosophers | **Waiter / Admission Pattern**: Indexing (`(p + 1) % 5` and `p`) eliminates `switch`; `tableLimit(4)` ensures at most 4 philosophers compete for 5 forks (Pigeonhole Principle). |
| **`DiningPhilosophers3`** | `Semaphore[5] forks` + Asymmetric Even/Odd Order | 5 `Semaphore`s | 2 philosophers | **Optimal Resource Hierarchy**: No `tableLimit` needed! Even philosophers pick `left` then `right`; odd philosophers pick `right` then `left`, breaking the circular wait. |
| **Bonus: Atomic Pick** | `ReentrantLock[5] forks` + Table Mutex / `synchronized` around picking both | 5 Fork Locks + 1 Table Lock (or `synchronized`) | 2 philosophers (or 1 if whole method is `synchronized`) | **All-or-Nothing Pick**: A philosopher locks a global mutex only while picking up both forks, preventing Hold-and-Wait. |

---

### 2. Deep Dive into Each Approach

#### A. Clockwise Table Geometry & Fork Indexing
- In LeetCode's diagram, philosophers `0, 1, 2, 3, 4` sit **clockwise** around the table facing the center:
  ```text
            Fork 0
       [P4]        [P0]
     Fork 4            Fork 1
       [P3]        [P1]
          \        /
       Fork 3 -- [P2] -- Fork 2
  ```
- Because they face inward:
  - Philosopher `p`'s **left neighbor** is `(p + 1) % 5` $\rightarrow$ **`leftFork = forks[(p + 1) % 5]`**
  - Philosopher `p`'s **right neighbor** is `(p + 4) % 5` $\rightarrow$ **`rightFork = forks[p]`**
- **Critical Ordering of `.run()` vs. Lock Acquisition**:
  - **Picking up**: `fork.acquire()` **must** happen *before* `pick...Fork.run()`.
  - **Putting down**: `put...Fork.run()` **must** happen *before* `fork.release()`. If `release()` is called before `put...Fork.run()`, a neighbor can wake up and log `pick` before the current philosopher logs `put`!

#### B. `DiningPhilosophers` & `DiningPhilosophers2`: Limiting Diners (`Semaphore(4)`)
- **Why Naive `leftFork.acquire()` $\rightarrow$ `rightFork.acquire()` Deadlocks**:
  - If all 5 philosophers grab their left fork simultaneously (`P0->fork1`, `P1->fork2`, `P2->fork3`, `P3->fork4`, `P4->fork0`), 0 forks remain on the table and all 5 block forever on `rightFork.acquire()`.
- **How `tableLimit = new Semaphore(4)` Solves It**:
  - Before touching **any** fork, a philosopher must call `tableLimit.acquire()`.
  - At most **4 philosophers** can hold `tableLimit` permits at once; the 5th philosopher is stopped at the door holding **0 forks**.
  - With 4 philosophers competing for 5 forks, by the **Pigeonhole Principle**, at least 1 philosopher is guaranteed to acquire **both** their left and right forks, eat, and release their forks and `tableLimit` permit.
- **Why `rightSem(4)` Was Redundant in `DiningPhilosophers`**:
  - Since every philosopher acquires their left fork first, `tableLimit(4)` already caps the number of philosophers who can reach the right-fork step to 4.

#### C. `DiningPhilosophers3`: Asymmetric Even/Odd Order ("Shared Enter Key")
- **How It Works Without `tableLimit`**:
  ```java
  if (philosopher % 2 == 0) {
      leftFork.acquire();
      pickLeftFork.run();
      rightFork.acquire();
      pickRightFork.run();
  } else {
      rightFork.acquire();
      pickRightFork.run();
      leftFork.acquire();
      pickLeftFork.run();
  }
  ```
- **The "Contiguous Pair Enter Key" Intuition**:
  - Look at which physical fork each philosopher tries to acquire **first**:
    - **Pair `(P0, P1)`**: `P0` (even, left first) wants **`fork1`**; `P1` (odd, right first) also wants **`fork1`**. Thus **`fork1` is the shared "enter key"** for `{P0, P1}`! Only 1 of them can win `fork1`; the loser blocks while holding **0 forks**, leaving their other fork on the table for neighbors.
    - **Pair `(P2, P3)`**: `P2` (even, left first) wants **`fork3`**; `P3` (odd, right first) also wants **`fork3`**. Thus **`fork3` is the shared "enter key"** for `{P2, P3}`!
    - **`P4` (even)**: wants **`fork0`** first.
  - Because all 5 philosophers only compete for **3 distinct first forks (`fork1`, `fork3`, `fork0`)**, at most **3 philosophers** can hold a first fork simultaneously, guaranteeing no circular wait can ever form!

#### D. Bonus Approach: Atomic Fork Acquisition (`ReentrantLock` / `synchronized`)
- Instead of limiting diners or alternating lock order, we can break **Hold-and-Wait** by requiring a philosopher to acquire a brief "pick-up mutex" while grabbing both forks:
  ```java
  // Inside wantsToEat:
  synchronized (this) {
      leftFork.acquire();
      rightFork.acquire();
  }
  pickLeftFork.run();
  pickRightFork.run();
  eat.run();
  putLeftFork.run();
  putRightFork.run();
  leftFork.release();
  rightFork.release();
  ```
- Because only one thread at a time can be inside `synchronized (this)` acquiring the pair `(leftFork, rightFork)`, no two philosophers can interleave their first fork acquisitions to deadlock, while two non-adjacent philosophers can still execute `eat.run()` in parallel outside the `synchronized` block.

---

### 3. Java vs. C# Concurrency Mapping

| Concept | Java | C# (.NET) | Notes |
| :--- | :--- | :--- | :--- |
| **Binary / Counting Semaphore** | `new Semaphore(1)` / `new Semaphore(4)` | `new SemaphoreSlim(1, 1)` / `new SemaphoreSlim(4, 4)` | Unlike `ReentrantLock` (`lock`), `Semaphore` permits have no thread ownership, though here the same thread acquires and releases. |
| **Explicit Mutex Lock** | `new ReentrantLock()` (`lock()` / `unlock()`) | `new object()` with `Monitor.Enter` / `Monitor.Exit` | In Java, `ReentrantLock` enforces thread ownership (only the holding thread may call `unlock()`). |
| **Action Callbacks** | `Runnable` (`pickLeftFork.run()`) | `Action` (`pickLeftFork()`) | Synchronous callback execution on the current philosopher thread. |

---

### 4. Real-World Implementation & Use Cases
- **Where Deadlock Prevention Patterns From Dining Philosophers Are Used in Production**:
  1. **Database Row Locking (`DiningPhilosophers3` Resource Hierarchy)**: When a bank transfer updates two accounts (`accountA` and `accountB`), if Thread 1 locks `accountA -> accountB` and Thread 2 locks `accountB -> accountA`, they deadlock. Production engines enforce a **total lock ordering** (e.g., always lock `min(idA, idB)` before `max(idA, idB)`), which is the exact generalize-to-$N$ version of `DiningPhilosophers3`.
  2. **Connection Pool / Thread Pool Admission Control (`DiningPhilosophers2` Waiter Pattern)**: When worker tasks each require multiple scarce resources (e.g., 2 GPU memory buffers or 2 DB connections), an outer admission `Semaphore` throttles concurrent active tasks so they cannot exhaust all buffers at 1 buffer per task and deadlock.

---

### 5. Complexity
- **All Implementations (`DiningPhilosophers`, `DiningPhilosophers2`, `DiningPhilosophers3`)**:
  - **Time Complexity**: $O(n)$ — For $n$ meals per philosopher ($5n$ total `wantsToEat` calls), each meal performs a constant number of $O(1)$ semaphore `acquire()` / `release()` operations and 5 `Runnable.run()` calls.
  - **Space Complexity**: $O(1)$ — Uses a fixed number of `Semaphore` instances (5 to 7 semaphores) regardless of the number of meals $n$.
