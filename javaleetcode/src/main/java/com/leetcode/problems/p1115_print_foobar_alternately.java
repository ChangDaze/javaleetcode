package com.leetcode.problems;

import java.util.concurrent.Semaphore;

public class p1115_print_foobar_alternately {
    public static class FooBar {
        private int n;
        private boolean lock = false;

        public FooBar(int n) {
            this.n = n;
        }

        public void foo(Runnable printFoo) throws InterruptedException {

            for (int i = 0; i < n; i++) {
                while (lock)
                {
                    Thread.sleep(1);
                }

                // printFoo.run() outputs "foo". Do not change or remove this line.
                printFoo.run();

                lock = true;
            }
        }

        public void bar(Runnable printBar) throws InterruptedException {

            for (int i = 0; i < n; i++) {
                while (!lock)
                {
                    Thread.sleep(1);
                }
                // printBar.run() outputs "bar". Do not change or remove this line.
                printBar.run();

                lock = false;
            }
        }
    }

    public static class FooBar2 {
        private int n;
        private volatile boolean lock = false; // add volatile for cross-thread visibility and ordering

        public FooBar2(int n) {
            this.n = n;
        }

        public void foo(Runnable printFoo) throws InterruptedException {
            for (int i = 0; i < n; i++) {
                while (lock)
                {
                    Thread.yield(); // yield cpu time instead of sleeping whole 1ms
                }

                // printFoo.run() outputs "foo". Do not change or remove this line.
                printFoo.run();

                lock = true;
            }
        }

        public void bar(Runnable printBar) throws InterruptedException {

            for (int i = 0; i < n; i++) {
                while (!lock)
                {
                    Thread.yield();
                }
                // printBar.run() outputs "bar". Do not change or remove this line.
                printBar.run();

                lock = false;
            }
        }
    }

    public static class FooBar3 {
        private int n;
        private boolean isBarTurn = false;

        public FooBar3(int n) {
            this.n = n;
        }

        public void foo(Runnable printFoo) throws InterruptedException {
            for (int i = 0; i < n; i++) {
                synchronized (this) { // synchronized means lock, only one thread can be inside synchronized (this) at a time
                    while (isBarTurn) { // while not if makes it re-check the condition before proceeding
                        this.wait();
                        // synchronized (this) means only one thread can HOLD THE KEY (be actively running inside) at a time—and this.wait() gives up the key
                        // even after notifyAll(), it wakes up, it still blocks until other thread return the key, and it grabs the key (even in synchronized)
                    }

                    printFoo.run();

                    isBarTurn = true;
                    this.notifyAll(); // wakes up threads waiting on this. (Since there is only 1 other thread here, this.notify() also works identically. ex: bar caller)
                }
                // Two threads are never running inside synchronized (this) at the same time.
            }
        }

        public void bar(Runnable printBar) throws InterruptedException {
            for (int i = 0; i < n; i++) {
                synchronized (this) {
                    while (!isBarTurn) {
                        this.wait();
                    }

                    printBar.run();

                    isBarTurn = false;
                    this.notifyAll();
                }
            }
        }
    }

    public static class FooBar4 {
        private int n;
        private final Semaphore fooSem = new Semaphore(1); // run first so starts with 1 initial permit
        private final Semaphore barSem = new Semaphore(0); // run later so starts with 0 initial permit (block at first)

        public FooBar4(int n) {
            this.n = n;
        }

        public void foo(Runnable printFoo) throws InterruptedException {
            for (int i = 0; i < n; i++) {
                fooSem.acquire(); // get 1 permit from fooSem (blocks if 0), next loop fooSem be 0, when fooSem.release() not triggered

                printFoo.run();

                barSem.release(); // put 1 permit into barSem (let bar can keep going from blocking)
            }

            // use Semaphore can avoid while loop keep checking and use cpu signal to block and run thread.
        }

        public void bar(Runnable printBar) throws InterruptedException {
            for (int i = 0; i < n; i++) {
                barSem.acquire();

                printBar.run();

                fooSem.release();
            }
        }
    }
}
