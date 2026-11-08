package com.leetcode.problems;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class p1117_building_h2oTest {

    @FunctionalInterface
    private interface InterruptibleConsumer<T> {
        void accept(T t) throws InterruptedException;
    }

    /**
     * Generic helper method to run hydrogen() and oxygen() concurrently based on the input water string.
     * Each character in 'water' spawns a dedicated thread calling either hydrogen() or oxygen().
     */
    private String executeConcurrently(
            String water,
            InterruptibleConsumer<Runnable> hydrogenMethod,
            InterruptibleConsumer<Runnable> oxygenMethod) throws InterruptedException {

        StringBuffer output = new StringBuffer();
        CountDownLatch startSignal = new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>(water.length());

        for (char c : water.toCharArray()) {
            if (c == 'H') {
                threads.add(new Thread(() -> {
                    try {
                        startSignal.await();
                        hydrogenMethod.accept(() -> output.append('H'));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        fail("Hydrogen thread was interrupted");
                    }
                }));
            } else if (c == 'O') {
                threads.add(new Thread(() -> {
                    try {
                        startSignal.await();
                        oxygenMethod.accept(() -> output.append('O'));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        fail("Oxygen thread was interrupted");
                    }
                }));
            } else {
                throw new IllegalArgumentException("Invalid atom character: " + c);
            }
        }

        for (Thread t : threads) {
            t.start();
        }
        // Release all threads at the same time to maximize concurrency contention
        startSignal.countDown();

        for (Thread t : threads) {
            t.join();
        }

        return output.toString();
    }

    /**
     * Generic helper method to test when 2 Oxygen threads start before 4 Hydrogen threads.
     */
    private String executeOxygenBeforeHydrogen(
            InterruptibleConsumer<Runnable> hydrogenMethod,
            InterruptibleConsumer<Runnable> oxygenMethod) throws InterruptedException {

        StringBuffer output = new StringBuffer();
        CountDownLatch oxygenStarted = new CountDownLatch(2);
        List<Thread> threads = new ArrayList<>();

        // Start 2 Oxygen threads first
        for (int i = 0; i < 2; i++) {
            threads.add(new Thread(() -> {
                try {
                    oxygenStarted.countDown();
                    oxygenMethod.accept(() -> output.append('O'));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    fail("Oxygen thread was interrupted");
                }
            }));
        }

        // Start 4 Hydrogen threads after Oxygen threads have already entered oxygen()
        for (int i = 0; i < 4; i++) {
            threads.add(new Thread(() -> {
                try {
                    oxygenStarted.await();
                    Thread.sleep(10);
                    hydrogenMethod.accept(() -> output.append('H'));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    fail("Hydrogen thread was interrupted");
                }
            }));
        }

        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }

        return output.toString();
    }

    /**
     * Validates that the output string consists of valid H2O molecules:
     * every consecutive group of 3 characters must contain exactly two 'H's and one 'O'.
     */
    private void assertValidWaterMolecules(String expectedInput, String actualOutput) {
        assertNotNull(actualOutput);
        assertEquals(expectedInput.length(), actualOutput.length(),
                "Output length must match input length");
        assertEquals(0, actualOutput.length() % 3,
                "Output length must be a multiple of 3");

        for (int i = 0; i < actualOutput.length(); i += 3) {
            String molecule = actualOutput.substring(i, i + 3);
            int hCount = 0;
            int oCount = 0;
            for (char c : molecule.toCharArray()) {
                if (c == 'H') hCount++;
                else if (c == 'O') oCount++;
            }
            assertEquals(2, hCount,
                    "Molecule at index " + (i / 3) + " (\"" + molecule + "\") must have 2 H atoms, full output: " + actualOutput);
            assertEquals(1, oCount,
                    "Molecule at index " + (i / 3) + " (\"" + molecule + "\") must have 1 O atom, full output: " + actualOutput);
        }
    }

    // ==================== Tests for H2O (synchronized + strict HHO turn) ====================

    private String runH2O(String water) throws InterruptedException {
        p1117_building_h2o.H2O h2o = new p1117_building_h2o.H2O();
        return executeConcurrently(water, h2o::hydrogen, h2o::oxygen);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testLeetCodeExample1() throws InterruptedException {
        String water = "HOH";
        String result = runH2O(water);
        assertValidWaterMolecules(water, result);
        assertEquals("HHO", result);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testLeetCodeExample2() throws InterruptedException {
        String water = "OOHHHH";
        String result = runH2O(water);
        assertValidWaterMolecules(water, result);
        assertEquals("HHOHHO", result);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testOxygenThreadsStartBeforeHydrogenThreads() throws InterruptedException {
        p1117_building_h2o.H2O h2o = new p1117_building_h2o.H2O();
        String result = executeOxygenBeforeHydrogen(h2o::hydrogen, h2o::oxygen);
        assertValidWaterMolecules("OOHHHH", result);
        assertEquals("HHOHHO", result);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testAllHydrogensBeforeAllOxygens() throws InterruptedException {
        String water = "HHHHHHHHHHOOOOO"; // 5 molecules
        String result = runH2O(water);
        assertValidWaterMolecules(water, result);
        assertEquals("HHO".repeat(5), result);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testMultipleMoleculesMaxConstraint() throws InterruptedException {
        int n = 20;
        String water = "O".repeat(n) + "H".repeat(2 * n);
        String result = runH2O(water);
        assertValidWaterMolecules(water, result);
        assertEquals("HHO".repeat(n), result);
    }

    // ==================== Tests for H2O_2 (synchronized + batch count reset) ====================

    private String runH2O_2(String water) throws InterruptedException {
        p1117_building_h2o.H2O_2 h2o = new p1117_building_h2o.H2O_2();
        return executeConcurrently(water, h2o::hydrogen, h2o::oxygen);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testH2O_2LeetCodeExample1() throws InterruptedException {
        String water = "HOH";
        String result = runH2O_2(water);
        assertValidWaterMolecules(water, result);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testH2O_2LeetCodeExample2() throws InterruptedException {
        String water = "OOHHHH";
        String result = runH2O_2(water);
        assertValidWaterMolecules(water, result);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testH2O_2OxygenThreadsStartBeforeHydrogenThreads() throws InterruptedException {
        p1117_building_h2o.H2O_2 h2o = new p1117_building_h2o.H2O_2();
        String result = executeOxygenBeforeHydrogen(h2o::hydrogen, h2o::oxygen);
        assertValidWaterMolecules("OOHHHH", result);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testH2O_2AllHydrogensBeforeAllOxygens() throws InterruptedException {
        String water = "HHHHHHHHHHOOOOO";
        String result = runH2O_2(water);
        assertValidWaterMolecules(water, result);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testH2O_2MultipleMoleculesMaxConstraint() throws InterruptedException {
        int n = 20;
        String water = "O".repeat(n) + "H".repeat(2 * n);
        String result = runH2O_2(water);
        assertValidWaterMolecules(water, result);
    }

    // ==================== Tests for H2O_3 (Semaphore handshake, strict HHO order) ====================

    private String runH2O_3(String water) throws InterruptedException {
        p1117_building_h2o.H2O_3 h2o = new p1117_building_h2o.H2O_3();
        return executeConcurrently(water, h2o::hydrogen, h2o::oxygen);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testH2O_3LeetCodeExample1() throws InterruptedException {
        String water = "HOH";
        String result = runH2O_3(water);
        assertValidWaterMolecules(water, result);
        assertEquals("HHO", result);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testH2O_3LeetCodeExample2() throws InterruptedException {
        String water = "OOHHHH";
        String result = runH2O_3(water);
        assertValidWaterMolecules(water, result);
        assertEquals("HHOHHO", result);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testH2O_3OxygenThreadsStartBeforeHydrogenThreads() throws InterruptedException {
        p1117_building_h2o.H2O_3 h2o = new p1117_building_h2o.H2O_3();
        String result = executeOxygenBeforeHydrogen(h2o::hydrogen, h2o::oxygen);
        assertValidWaterMolecules("OOHHHH", result);
        assertEquals("HHOHHO", result);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testH2O_3AllHydrogensBeforeAllOxygens() throws InterruptedException {
        String water = "HHHHHHHHHHOOOOO";
        String result = runH2O_3(water);
        assertValidWaterMolecules(water, result);
        assertEquals("HHO".repeat(5), result);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testH2O_3MultipleMoleculesMaxConstraint() throws InterruptedException {
        int n = 20;
        String water = "O".repeat(n) + "H".repeat(2 * n);
        String result = runH2O_3(water);
        assertValidWaterMolecules(water, result);
        assertEquals("HHO".repeat(n), result);
    }

    // ==================== Tests for H2O_4 (Semaphore + CyclicBarrier) ====================

    private String runH2O_4(String water) throws InterruptedException {
        p1117_building_h2o.H2O_4 h2o = new p1117_building_h2o.H2O_4();
        return executeConcurrently(water, h2o::hydrogen, h2o::oxygen);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testH2O_4LeetCodeExample1() throws InterruptedException {
        String water = "HOH";
        String result = runH2O_4(water);
        assertValidWaterMolecules(water, result);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testH2O_4LeetCodeExample2() throws InterruptedException {
        String water = "OOHHHH";
        String result = runH2O_4(water);
        assertValidWaterMolecules(water, result);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testH2O_4OxygenThreadsStartBeforeHydrogenThreads() throws InterruptedException {
        p1117_building_h2o.H2O_4 h2o = new p1117_building_h2o.H2O_4();
        String result = executeOxygenBeforeHydrogen(h2o::hydrogen, h2o::oxygen);
        assertValidWaterMolecules("OOHHHH", result);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testH2O_4AllHydrogensBeforeAllOxygens() throws InterruptedException {
        String water = "HHHHHHHHHHOOOOO";
        String result = runH2O_4(water);
        assertValidWaterMolecules(water, result);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testH2O_4MultipleMoleculesMaxConstraint() throws InterruptedException {
        int n = 20;
        String water = "O".repeat(n) + "H".repeat(2 * n);
        String result = runH2O_4(water);
        assertValidWaterMolecules(water, result);
    }
}
