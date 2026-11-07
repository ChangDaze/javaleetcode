# 1115. Print FooBar Alternately

### 1. Progressive Evolution of 4 Concurrency Approaches
- **Core Problem**: Two threads (`threadFoo` and `threadBar`) share a single instance and must strictly alternate printing `"foo"` and `"bar"` $n$ times (`"foobarfoobar..."`), regardless of which thread the OS schedules first.
- **Comparison of the 4 Implementations**:

| Implementation | Mechanism | How Thread Blocks | CPU Usage While Waiting | Key Limitation / Advantage |
| :--- | :--- | :--- | :--- | :--- |
| **`FooBar`** | Plain `boolean` + `Thread.sleep(1)` | Timer sleep (1ms+) | Low, but slow | **Bug**: Missing `volatile` (visibility/reordering risk); **Slow**: $\ge 1\text{ms}$ per turn causes TLE for large $n$. |
| **`FooBar2`** | `volatile boolean` + `Thread.yield()` | Spin-wait + CPU yield | High (Busy-waiting) | Fixes memory visibility & speed, but wastes CPU cycles spinning in `while (lock)`. |
| **`FooBar3`** | `synchronized` + `wait()` / `notifyAll()` | Monitor wait queue | $0\%$ (OS blocked) | Classic intrinsic lock + event-driven wakeup; zero CPU wasted while waiting. |
| **`FooBar4`** | Two `Semaphore`s (`fooSem(1)`, `barSem(0)`) | AQS park (`LockSupport.park`) | $0\%$ (OS blocked) | Cleanest & most idiomatic: no manual `synchronized`, `boolean` flag, or `while` loop needed. |

---

### 2. Deep Dive into Each Approach

#### A. `FooBar` & `FooBar2`: Spin-Waiting (`volatile` + `Thread.yield()`)
- **Why `volatile` is required (`FooBar2`)**:
  - Without `volatile` (`FooBar`), a thread may cache `lock` in a CPU register/L1 cache and never observe updates from the other thread, or the JVM/CPU may reorder `printFoo.run()` and `lock = true`.
  - Declaring `private volatile boolean lock = false;` establishes a **happens-before** relationship:
    1. **Visibility**: Every assignment (`lock = true`) acts like C#'s `Volatile.Write`, immediately flushing to main memory; every read (`while (lock)`) acts like `Volatile.Read`.
    2. **Instruction Ordering**: `printFoo.run()` is guaranteed to complete before `lock = true` becomes visible to the `bar` thread.
- **`volatile boolean` vs. `AtomicBoolean`**:
  - Because `foo` and `bar` strictly take turns writing to `lock` (only one thread writes at a time via direct assignment `lock = true` / `lock = false`), `volatile boolean` is **100% equivalent** to `AtomicBoolean.get()` / `.set()`, without the extra object wrapper overhead.
  - `AtomicBoolean` is only needed when multiple threads race to perform a **read-modify-write** simultaneously (e.g., `compareAndSet`, like C#'s `Interlocked.CompareExchange`).

#### B. `FooBar3`: Intrinsic Monitor (`synchronized` + `wait()` / `notifyAll()`)
- **The "One Key & Side Bedroom (Waiting Room)" Mental Model**:
  - Why isn't `this.wait()` inside `synchronized (this)` a contradiction? Because **`this.wait()` temporarily gives up the lock key!**
  ```text
  1. Bar enters first:
     Grabs the 1 Key -> enters synchronized(this) -> sees !isBarTurn is true.

  2. Bar calls this.wait():
     Atomically DROPS the Key back on the door and sleeps in `this`'s Waiting Room.
     (Now nobody holds the Key!)

  3. Foo enters:
     Grabs the Key -> enters synchronized(this) -> runs printFoo.run() -> sets isBarTurn = true.

  4. Foo calls this.notifyAll():
     Wakes Bar in the Waiting Room (Bar moves to the lock entry queue, waiting for Foo to drop the Key).

  5. Foo exits synchronized(this) `}`:
     Foo drops the Key -> Bar re-acquires the Key, exits this.wait(), re-checks `while (!isBarTurn)`, and prints "bar".
  ```
- **Why `while (isBarTurn)` instead of `if (isBarTurn)`?**:
  - Guards against **spurious wakeups** (OS threads can rarely wake from `wait()` without a signal) and ensures the condition is re-verified after re-acquiring the lock.
- **`Thread.sleep(1)` vs. `this.wait()`**:
  - `Thread.sleep(1)` **keeps holding the lock** while sleeping (calling `sleep()` inside `synchronized` would deadlock the other thread!).
  - `this.wait()` **releases the lock** and sleeps indefinitely ($0\%$ CPU) until notified via `this.notify()` / `this.notifyAll()`.

#### C. `FooBar4`: Permit Relay (`java.util.concurrent.Semaphore`)
- **Two-Jar Ticket Relay**:
  - `fooSem = new Semaphore(1)`: Starts with `1` permit so `foo` can run immediately on turn 0.
  - `barSem = new Semaphore(0)`: Starts with `0` permits so `bar` blocks immediately if it arrives first.
  - **Relay**: `foo` takes from `fooSem` (`acquire()`) $\rightarrow$ prints `"foo"` $\rightarrow$ puts a permit into `barSem` (`release()`). Then `bar` takes from `barSem` $\rightarrow$ prints `"bar"` $\rightarrow$ puts a permit back into `fooSem`.
- **Under the Hood**:
  - `Semaphore` uses CPU atomic CAS (`Compare-And-Swap`) to update the permit counter and OS thread parking (`LockSupport.park()` / `unpark()`) to block and wake threads—encapsulating the state variable, `while` loop, and wait queue internally.

---

### 3. Java vs. C# Concurrency Mapping

| Concept | Java | C# (.NET) | Notes |
| :--- | :--- | :--- | :--- |
| **Volatile Field** | `volatile boolean lock;` | `volatile bool _lock;` or `Volatile.Read` / `Volatile.Write` | Java `volatile` automatically applies acquire-read and release-write semantics to all reads/writes. |
| **Monitor Lock** | `synchronized (this) { ... }` | `lock (this) { ... }` | Mutual exclusion + memory barrier on enter/exit (no `volatile` needed inside). |
| **Monitor Wait / Signal** | `this.wait()` / `this.notifyAll()` | `Monitor.Wait(this)` / `Monitor.PulseAll(this)` | Both release the monitor lock while waiting and re-acquire it before returning. |
| **Semaphore (Signaling)** | `new Semaphore(0)` (`acquire` / `release`) | `new SemaphoreSlim(0, 1)` or `new AutoResetEvent(false)` | Unlike `synchronized`, a `Semaphore` has **no owner thread**—Thread A can `acquire()` and Thread B can `release()`. |

---

### 4. Real-World Implementation & Use Cases
- **Where Alternating / Signaling Patterns Are Used in Production**:
  1. **Producer–Consumer Single-Slot Handoff**: One reader thread decodes a frame into a buffer (`foo`) and signals the renderer thread (`bar`) to consume it before decoding the next frame.
  2. **Ping-Pong Health Check / Lockstep Simulation**: Two worker threads in a simulation engine or hardware driver executing strict request-acknowledge cycles.
  3. **Async-to-Sync Request Bridge**: A caller thread acquires a `Semaphore(0)` to block until a callback thread receives the network response and calls `release()`.

---

### 5. Complexity
- **All 4 Implementations (`FooBar`, `FooBar2`, `FooBar3`, `FooBar4`)**:
  - **Time Complexity**: $O(n)$ — Both `foo` and `bar` execute $n$ iterations, performing $O(1)$ synchronization work per turn.
  - **Space Complexity**: $O(1)$ — Only a constant number of synchronization primitives (`boolean` flag or two `Semaphore` instances) are allocated regardless of $n$.
