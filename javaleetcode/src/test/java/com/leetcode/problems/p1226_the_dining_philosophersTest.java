package com.leetcode.problems;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class p1226_the_dining_philosophersTest {

    @FunctionalInterface
    private interface WantsToEatMethod {
        void wantsToEat(int philosopher,
                        Runnable pickLeftFork,
                        Runnable pickRightFork,
                        Runnable eat,
                        Runnable putLeftFork,
                        Runnable putRightFork) throws InterruptedException;
    }

    /**
     * Runs 5 philosopher threads concurrently, each calling wantsToEat() n times.
     * Records all [philosopher, fork, action] events in a thread-safe list.
     *
     * Encoding (same as LeetCode):
     * - fork:   1 = left fork, 2 = right fork, 0 = eating
     * - action: 1 = pick,      2 = put,        3 = eat
     */
    private List<int[]> executeConcurrently(int n, WantsToEatMethod wantsToEatMethod) throws InterruptedException {
        List<int[]> events = Collections.synchronizedList(new ArrayList<>(n * 5 * 5));
        CountDownLatch startSignal = new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>(5);

        for (int i = 0; i < 5; i++) {
            final int philosopher = i;
            threads.add(new Thread(() -> {
                try {
                    startSignal.await();
                    for (int meal = 0; meal < n; meal++) {
                        wantsToEatMethod.wantsToEat(
                                philosopher,
                                () -> events.add(new int[]{philosopher, 1, 1}), // pickLeftFork
                                () -> events.add(new int[]{philosopher, 2, 1}), // pickRightFork
                                () -> events.add(new int[]{philosopher, 0, 3}), // eat
                                () -> events.add(new int[]{philosopher, 1, 2}), // putLeftFork
                                () -> events.add(new int[]{philosopher, 2, 2})  // putRightFork
                        );
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    fail("Philosopher " + philosopher + " thread was interrupted");
                }
            }, "Philosopher-" + philosopher));
        }

        for (Thread t : threads) {
            t.start();
        }
        // Release all 5 philosophers at the exact same moment to maximize deadlock/race contention
        startSignal.countDown();

        for (Thread t : threads) {
            t.join();
        }

        return events;
    }

    /**
     * Validates that the recorded events obey all Dining Philosophers physical & concurrency rules:
     * 1. Total events = 5 philosophers * n meals * 5 actions per meal = 25 * n.
     * 2. No physical fork (0..4) is ever held by two philosophers at the same time.
     * 3. A philosopher only eats when currently holding BOTH their left and right forks.
     * 4. A philosopher only puts down a fork after having eaten during that hold, and must be holding it.
     * 5. Every philosopher eats exactly n times and finishes with all forks put back on the table.
     */
    private void assertValidDiningLog(int n, List<int[]> events) {
        assertNotNull(events);
        assertEquals(25 * n, events.size(),
                "Expected " + (25 * n) + " total actions (5 philosophers * " + n + " meals * 5 actions), but got " + events.size());

        // forkOwner[f] stores the philosopher currently holding physical fork f, or -1 if on the table.
        // Physical fork mapping around the clockwise circular table (facing inward):
        // Philosopher p's left neighbor is (p + 1) % 5, and right neighbor is (p + 4) % 5.
        // So for philosopher p: left fork = (p + 1) % 5 (shared with p+1's right fork), right fork = p (shared with p-1's left fork).
        int[] forkOwner = new int[5];
        Arrays.fill(forkOwner, -1);

        boolean[] holdingLeft = new boolean[5];
        boolean[] holdingRight = new boolean[5];
        boolean[] hasEatenCurrentMeal = new boolean[5];
        int[] eatCount = new int[5];

        for (int step = 0; step < events.size(); step++) {
            int[] event = events.get(step);
            int p = event[0];
            int forkSide = event[1]; // 1 = left, 2 = right, 0 = eat
            int action = event[2];   // 1 = pick, 2 = put,   3 = eat

            int physicalFork = (forkSide == 1) ? (p + 1) % 5 : p;

            if (action == 1) { // pick fork
                assertEquals(-1, forkOwner[physicalFork],
                        "Step " + step + " " + Arrays.toString(event) + ": Philosopher " + p
                                + " tried to pick physical fork " + physicalFork
                                + ", but it is already held by Philosopher " + forkOwner[physicalFork]);
                forkOwner[physicalFork] = p;

                if (forkSide == 1) {
                    assertFalse(holdingLeft[p], "Step " + step + ": Philosopher " + p + " already holding left fork");
                    holdingLeft[p] = true;
                } else if (forkSide == 2) {
                    assertFalse(holdingRight[p], "Step " + step + ": Philosopher " + p + " already holding right fork");
                    holdingRight[p] = true;
                } else {
                    fail("Invalid fork side for pick: " + forkSide);
                }
            } else if (action == 3) { // eat
                assertEquals(0, forkSide, "Eat action must have forkSide == 0");
                assertTrue(holdingLeft[p] && holdingRight[p],
                        "Step " + step + " " + Arrays.toString(event) + ": Philosopher " + p
                                + " tried to eat without holding both forks (left=" + holdingLeft[p]
                                + ", right=" + holdingRight[p] + ")");
                assertFalse(hasEatenCurrentMeal[p],
                        "Step " + step + ": Philosopher " + p + " ate twice without putting forks down");
                hasEatenCurrentMeal[p] = true;
                eatCount[p]++;
            } else if (action == 2) { // put fork
                assertEquals(p, forkOwner[physicalFork],
                        "Step " + step + " " + Arrays.toString(event) + ": Philosopher " + p
                                + " tried to put down physical fork " + physicalFork
                                + ", but owner is " + forkOwner[physicalFork]);
                assertTrue(hasEatenCurrentMeal[p],
                        "Step " + step + ": Philosopher " + p + " put down a fork before eating");

                forkOwner[physicalFork] = -1;
                if (forkSide == 1) {
                    assertTrue(holdingLeft[p], "Step " + step + ": Philosopher " + p + " not holding left fork");
                    holdingLeft[p] = false;
                } else if (forkSide == 2) {
                    assertTrue(holdingRight[p], "Step " + step + ": Philosopher " + p + " not holding right fork");
                    holdingRight[p] = false;
                } else {
                    fail("Invalid fork side for put: " + forkSide);
                }

                // Once both forks are put down, reset meal state for the next meal
                if (!holdingLeft[p] && !holdingRight[p]) {
                    hasEatenCurrentMeal[p] = false;
                }
            } else {
                fail("Unknown action code: " + action);
            }
        }

        for (int p = 0; p < 5; p++) {
            assertEquals(n, eatCount[p], "Philosopher " + p + " should eat exactly " + n + " times");
            assertFalse(holdingLeft[p], "Philosopher " + p + " still holding left fork at the end");
            assertFalse(holdingRight[p], "Philosopher " + p + " still holding right fork at the end");
        }
        for (int f = 0; f < 5; f++) {
            assertEquals(-1, forkOwner[f], "Physical fork " + f + " was not put back on the table");
        }
    }

    // ==================== Tests for DiningPhilosophers ====================

    private void runDiningPhilosophers(int n) throws InterruptedException {
        p1226_the_dining_philosophers.DiningPhilosophers dp =
                new p1226_the_dining_philosophers.DiningPhilosophers();
        List<int[]> events = executeConcurrently(n, dp::wantsToEat);
        assertValidDiningLog(n, events);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testLeetCodeExample1SingleMealEach() throws InterruptedException {
        runDiningPhilosophers(1);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testMultipleMealsSmall() throws InterruptedException {
        runDiningPhilosophers(5);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testMultipleMealsMaxConstraint() throws InterruptedException {
        // LeetCode constraint: 1 <= n <= 60
        runDiningPhilosophers(60);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testRepeatedStressForDeadlock() throws InterruptedException {
        // Run multiple rounds to catch timing-dependent deadlocks or race conditions
        for (int round = 0; round < 20; round++) {
            runDiningPhilosophers(10);
        }
    }

    // ==================== Tests for DiningPhilosophers2 (Semaphore[] + tableLimit) ====================

    private void runDiningPhilosophers2(int n) throws InterruptedException {
        p1226_the_dining_philosophers.DiningPhilosophers2 dp =
                new p1226_the_dining_philosophers.DiningPhilosophers2();
        List<int[]> events = executeConcurrently(n, dp::wantsToEat);
        assertValidDiningLog(n, events);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testDiningPhilosophers2LeetCodeExample1SingleMealEach() throws InterruptedException {
        runDiningPhilosophers2(1);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testDiningPhilosophers2MultipleMealsSmall() throws InterruptedException {
        runDiningPhilosophers2(5);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testDiningPhilosophers2MultipleMealsMaxConstraint() throws InterruptedException {
        // LeetCode constraint: 1 <= n <= 60
        runDiningPhilosophers2(60);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testDiningPhilosophers2RepeatedStressForDeadlock() throws InterruptedException {
        // Run multiple rounds to catch timing-dependent deadlocks or race conditions
        for (int round = 0; round < 20; round++) {
            runDiningPhilosophers2(10);
        }
    }

    // ==================== Tests for DiningPhilosophers3 (Asymmetric Even/Odd Fork Order) ====================

    private void runDiningPhilosophers3(int n) throws InterruptedException {
        p1226_the_dining_philosophers.DiningPhilosophers3 dp =
                new p1226_the_dining_philosophers.DiningPhilosophers3();
        List<int[]> events = executeConcurrently(n, dp::wantsToEat);
        assertValidDiningLog(n, events);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testDiningPhilosophers3LeetCodeExample1SingleMealEach() throws InterruptedException {
        runDiningPhilosophers3(1);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testDiningPhilosophers3MultipleMealsSmall() throws InterruptedException {
        runDiningPhilosophers3(5);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testDiningPhilosophers3MultipleMealsMaxConstraint() throws InterruptedException {
        // LeetCode constraint: 1 <= n <= 60
        runDiningPhilosophers3(60);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testDiningPhilosophers3RepeatedStressForDeadlock() throws InterruptedException {
        // Run multiple rounds to catch timing-dependent deadlocks or race conditions
        for (int round = 0; round < 20; round++) {
            runDiningPhilosophers3(10);
        }
    }
}
