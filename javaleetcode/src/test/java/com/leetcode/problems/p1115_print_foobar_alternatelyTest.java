package com.leetcode.problems;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class p1115_print_foobar_alternatelyTest {

    @FunctionalInterface
    private interface InterruptibleConsumer<T> {
        void accept(T t) throws InterruptedException;
    }

    /**
     * Generic helper method to run foo() and bar() concurrently on two threads and collect the output.
     */
    private String executeConcurrently(
            InterruptibleConsumer<Runnable> fooMethod,
            InterruptibleConsumer<Runnable> barMethod,
            boolean startBarFirst) throws InterruptedException {

        // Use StringBuffer for thread-safe appending across two threads
        StringBuffer output = new StringBuffer();
        CountDownLatch barStarted = new CountDownLatch(1);

        Thread threadFoo = new Thread(() -> {
            try {
                if (startBarFirst) {
                    barStarted.await();
                    Thread.sleep(5); // Give threadBar a head start to enter bar() first
                }
                fooMethod.accept(() -> output.append("foo"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("threadFoo was interrupted");
            }
        });

        Thread threadBar = new Thread(() -> {
            try {
                if (startBarFirst) {
                    barStarted.countDown();
                }
                barMethod.accept(() -> output.append("bar"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("threadBar was interrupted");
            }
        });

        threadFoo.start();
        threadBar.start();

        threadFoo.join();
        threadBar.join();

        return output.toString();
    }

    // ==================== Tests for FooBar (Thread.sleep) ====================

    private String runFooBar(int n, boolean startBarFirst) throws InterruptedException {
        p1115_print_foobar_alternately.FooBar fooBar =
                new p1115_print_foobar_alternately.FooBar(n);
        return executeConcurrently(fooBar::foo, fooBar::bar, startBarFirst);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testLeetCodeExample1() throws InterruptedException {
        // Input: n = 1 -> Output: "foobar"
        assertEquals("foobar", runFooBar(1, false));
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testLeetCodeExample2() throws InterruptedException {
        // Input: n = 2 -> Output: "foobarfoobar"
        assertEquals("foobarfoobar", runFooBar(2, false));
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testBarThreadStartsBeforeFooThread() throws InterruptedException {
        // Even if threadBar enters bar() first, "foo" must still be printed before "bar"
        assertEquals("foobar".repeat(5), runFooBar(5, true));
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testMultipleIterations() throws InterruptedException {
        int n = 20;
        assertEquals("foobar".repeat(n), runFooBar(n, false));
    }

    // ==================== Tests for FooBar2 (volatile + Thread.yield) ====================

    private String runFooBar2(int n, boolean startBarFirst) throws InterruptedException {
        p1115_print_foobar_alternately.FooBar2 fooBar2 =
                new p1115_print_foobar_alternately.FooBar2(n);
        return executeConcurrently(fooBar2::foo, fooBar2::bar, startBarFirst);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testFooBar2LeetCodeExample1() throws InterruptedException {
        assertEquals("foobar", runFooBar2(1, false));
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testFooBar2LeetCodeExample2() throws InterruptedException {
        assertEquals("foobarfoobar", runFooBar2(2, false));
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testFooBar2BarThreadStartsBeforeFooThread() throws InterruptedException {
        assertEquals("foobar".repeat(5), runFooBar2(5, true));
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testFooBar2LargeIterations() throws InterruptedException {
        // With Thread.yield() instead of Thread.sleep(1), even n = 1000 finishes in milliseconds!
        int n = 1000;
        assertEquals("foobar".repeat(n), runFooBar2(n, false));
    }

    // ==================== Tests for FooBar3 (synchronized + wait/notifyAll) ====================

    private String runFooBar3(int n, boolean startBarFirst) throws InterruptedException {
        p1115_print_foobar_alternately.FooBar3 fooBar3 =
                new p1115_print_foobar_alternately.FooBar3(n);
        return executeConcurrently(fooBar3::foo, fooBar3::bar, startBarFirst);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testFooBar3LeetCodeExample1() throws InterruptedException {
        assertEquals("foobar", runFooBar3(1, false));
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testFooBar3LeetCodeExample2() throws InterruptedException {
        assertEquals("foobarfoobar", runFooBar3(2, false));
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testFooBar3BarThreadStartsBeforeFooThread() throws InterruptedException {
        assertEquals("foobar".repeat(5), runFooBar3(5, true));
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testFooBar3LargeIterations() throws InterruptedException {
        int n = 1000;
        assertEquals("foobar".repeat(n), runFooBar3(n, false));
    }

    // ==================== Tests for FooBar4 (Semaphore) ====================

    private String runFooBar4(int n, boolean startBarFirst) throws InterruptedException {
        p1115_print_foobar_alternately.FooBar4 fooBar4 =
                new p1115_print_foobar_alternately.FooBar4(n);
        return executeConcurrently(fooBar4::foo, fooBar4::bar, startBarFirst);
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testFooBar4LeetCodeExample1() throws InterruptedException {
        assertEquals("foobar", runFooBar4(1, false));
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testFooBar4LeetCodeExample2() throws InterruptedException {
        assertEquals("foobarfoobar", runFooBar4(2, false));
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testFooBar4BarThreadStartsBeforeFooThread() throws InterruptedException {
        assertEquals("foobar".repeat(5), runFooBar4(5, true));
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testFooBar4LargeIterations() throws InterruptedException {
        int n = 1000;
        assertEquals("foobar".repeat(n), runFooBar4(n, false));
    }
}
