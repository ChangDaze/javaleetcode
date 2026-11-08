# 1117. Building H2O

### 1. Progressive Evolution of 4 Concurrency Approaches
- **Core Problem**: Multiple threads call `hydrogen()` and `oxygen()` concurrently. Threads must form complete water molecules ($\text{H}_2\text{O}$) in batches of **3 threads** (2 `H` and 1 `O`). All 3 atoms of the current molecule must finish bonding (`releaseHydrogen.run()` / `releaseOxygen.run()`) **before** any atom of the next molecule can bond.
- **Comparison of the 4 Implementations**:

| Implementation | Mechanism | Intra-Molecule Order | Parallelism Within Molecule | Key Limitation / Advantage |
| :--- | :--- | :--- | :--- | :--- |
| **`H2O`** | `synchronized` + `boolean isOxygenTurn` + `hydrogenCount` | Strict `"HHO"` | Serialized (1 thread at a time) | Simple turn-based state machine: 2 `H` threads run first, flip `isOxygenTurn = true`, then 1 `O` thread runs and resets. |
| **`H2O_2`** | `synchronized` + `hydrogenCount(2)` & `oxygenCount(1)` | Any (`"HHO"`, `"HOH"`, `"OHH"`) | Serialized (1 thread at a time) | Allows `H` and `O` to run in any order within a batch; `callNextBatch()` resets counts and calls `notifyAll()` only when both hit `0`. |
| **`H2O_3`** | Two `Semaphore`s (`hydrogenSem(2)`, `oxygenSem(0)`) | Strict `"HHO"` | 2 `H` threads run in parallel, then 1 `O` | **Fast & lightweight**: No `CyclicBarrier` needed; `O` waits via `oxygenSem.acquire(2)` until both `H` threads finish printing, then opens the next round via `hydrogenSem.release(2)`. |
| **`H2O_4`** | `Semaphore(2)` + `Semaphore(1)` + `CyclicBarrier(3)` | Any (`"HHO"`, `"HOH"`, `"OHH"`) | All 3 threads (`2H + 1O`) run in parallel | **True multi-thread batching**: `Semaphore` caps each batch to 2 `H` and 1 `O`, while `CyclicBarrier(3, this::callNextBatch)` guarantees all 3 finish before releasing permits for the next molecule. |

---

### 2. Deep Dive into Each Approach

#### A. `H2O` & `H2O_2`: Intrinsic Monitor (`synchronized` + `wait()` / `notifyAll()`)
- **`H2O` (Strict Turn-Based State Machine)**:
  - Starts with `isOxygenTurn = false` and `hydrogenCount = 0`.
  - Up to 2 `H` threads enter one by one, run `releaseHydrogen.run()`, and increment `hydrogenCount`. When `hydrogenCount == 2`, it sets `isOxygenTurn = true`.
  - Any subsequent `H` thread blocks in `while (isOxygenTurn) this.wait();`.
  - Once an `O` thread acquires the monitor, it runs `releaseOxygen.run()`, resets `isOxygenTurn = false` and `hydrogenCount = 0`, and calls `this.notifyAll()`.
- **`H2O_2` (Quota-Based Batch Reset)**:
  - Instead of forcing `H` before `O`, each batch starts with a quota: `hydrogenCount = 2` and `oxygenCount = 1`.
  - Whichever thread arrives and has remaining quota (`> 0`) prints immediately and decrements its counter.
  - **Batch Boundary (`callNextBatch()`)**:
    ```java
    private void callNextBatch() {
        if (oxygenCount == 0 && hydrogenCount == 0) {
            hydrogenCount = 2;
            oxygenCount = 1;
            this.notifyAll();
        }
    }
    ```
  - Only the **3rd thread** of the molecule sees both counters at `0`, replenishes the quotas (`2` and `1`), and wakes all waiting threads via `this.notifyAll()`.

#### B. `H2O_3`: Pure `Semaphore` Handshake (`acquire(2)` / `release(2)`)
- **Why Naive `Semaphore(2)` + `Semaphore(1)` Fails Without Coordination**:
  - If `H` and `O` release permits immediately after printing, a 3rd `H` thread from the **next molecule** can acquire a permit and print before the current molecule's `O` thread has even printed (producing `"HHHOOH"`).
- **How `H2O_3` Solves the Round Boundary**:
  ```text
  Round Start: hydrogenSem = 2 permits, oxygenSem = 0 permits

  1. Two H threads acquire 1 permit each from hydrogenSem (now 0).
     (Any 3rd H thread blocks on hydrogenSem.acquire()!)
  2. Each H thread calls releaseHydrogen.run() -> then calls oxygenSem.release(1).
  3. O thread calls oxygenSem.acquire(2):
     Blocks until BOTH H threads have finished releaseHydrogen.run() and released 1 permit each!
  4. O thread calls releaseOxygen.run().
     (At this exact moment, all 3 atoms of the molecule have finished printing!)
  5. O thread calls hydrogenSem.release(2) -> opens the next round!
  ```
- **Key Advantage**: Uses mathematical permit combination (`1 + 1 = 2` permits required by `oxygenSem.acquire(2)`) to act as a built-in barrier without needing `CyclicBarrier`.

#### C. `H2O_4`: `Semaphore` + `CyclicBarrier(3, runnable)`
- **Separating "Admission Control" from "Batch Completion"**:
  1. **Admission Control (`Semaphore`)**:
     - `hydrogenSem = new Semaphore(2)` and `oxygenSem = new Semaphore(1)` allow at most **2 `H` threads and 1 `O` thread** into the current round simultaneously.
  2. **Batch Completion (`CyclicBarrier`)**:
     - All 3 admitted threads call `releaseHydrogen.run()` / `releaseOxygen.run()` **before** calling `barrier.await()`.
     - `new CyclicBarrier(3, this::callNextBatch)` blocks the first 2 threads that finish printing until the 3rd thread also finishes printing and calls `barrier.await()`.
     - When the 3rd thread arrives, the barrier trips and executes `callNextBatch()` (`hydrogenSem.release(2); oxygenSem.release(1);`) **before** unblocking the threads—ensuring the next molecule cannot start until the current molecule is 100% complete.

---

### 3. Java vs. C# Concurrency Mapping

| Concept | Java | C# (.NET) | Notes |
| :--- | :--- | :--- | :--- |
| **Multi-Permit Semaphore** | `oxygenSem.acquire(2);` / `hydrogenSem.release(2);` | `SemaphoreSlim.Wait()` (1 at a time) / `Release(2)` | Java's `Semaphore.acquire(int permits)` atomically acquires multiple permits in one call; C#'s `SemaphoreSlim` only supports `Release(int releaseCount)`, requiring a loop for multi-acquire. |
| **Cyclic Barrier** | `new CyclicBarrier(3, this::callNextBatch)` | `new Barrier(3, b => CallNextBatch())` | Both block $N$ participant threads at a rendezvous point (`await()` in Java / `SignalAndWait()` in C#) and run a post-phase action once per round. |
| **Monitor Wait / Pulse** | `synchronized (this)` + `wait()` / `notifyAll()` | `lock (this)` + `Monitor.Wait(this)` / `Monitor.PulseAll(this)` | Identical monitor semantics for `H2O` and `H2O_2`. |

---

### 4. Real-World Implementation & Use Cases
- **Where Multi-Party Rendezvous & Quota Batching Are Used in Production**:
  1. **Multiplayer Matchmaking / Co-op Lobby (`H2O_4` Pattern)**: A game lobby requires a specific role composition per match (e.g., **1 Tank + 1 Healer + 3 DPS**). Role-specific `Semaphore`s admit the exact role quota, and a `CyclicBarrier(5)` ensures all 5 players are ready before launching the match instance.
  2. **MapReduce / Bulk-Synchronous Parallel (BSP) Stages (`H2O_3` / `H2O_4` Pattern)**: Worker threads process a fixed batch of shards in parallel; an aggregator thread (`oxygen`) waits via `acquire(N)` until all $N$ map workers (`hydrogen`) complete their current epoch before committing the checkpoint and releasing permits for the next epoch.
  3. **Hardware / Sensor Frame Fusion**: Combining 2 stereo camera frames (`H`) with 1 LiDAR scan (`O`) into a single synchronized perception packet before feeding the autonomous driving pipeline.

---

### 5. Complexity
- **All 4 Implementations (`H2O`, `H2O_2`, `H2O_3`, `H2O_4`)**:
  - **Time Complexity**: $O(n)$ — For $n$ water molecules ($3n$ total threads), each thread performs $O(1)$ synchronization operations (`acquire`/`release`, `wait`/`notifyAll`, or `barrier.await()`).
  - **Space Complexity**: $O(1)$ — Only a constant number of counters, `Semaphore`s, and a 3-party `CyclicBarrier` are allocated regardless of the number of molecules $n$.
