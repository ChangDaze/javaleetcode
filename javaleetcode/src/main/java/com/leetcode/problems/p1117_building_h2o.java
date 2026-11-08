package com.leetcode.problems;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

public class p1117_building_h2o {
    public static class H2O {

        private boolean isOxygenTurn = false;
        private int hydrogenCount = 0;

        public H2O() {

        }

        public void hydrogen(Runnable releaseHydrogen) throws InterruptedException {
            synchronized (this)
            {
                while (isOxygenTurn)
                {
                    this.wait();
                }
                // releaseHydrogen.run() outputs "H". Do not change or remove this line.
                releaseHydrogen.run();

                hydrogenCount++; // synchronized Guarantees Both Atomicity and Visibility

                if(hydrogenCount == 2) {
                    isOxygenTurn = true;
                }
                this.notifyAll();
            }
        }

        public void oxygen(Runnable releaseOxygen) throws InterruptedException {
            synchronized (this)
            {
                while (!isOxygenTurn)
                {
                    this.wait();
                }
                // releaseOxygen.run() outputs "O". Do not change or remove this line.
                releaseOxygen.run();

                isOxygenTurn = false;
                hydrogenCount = 0;

                this.notifyAll();
            }

        }
    }

    public static class H2O_2 {

        // "HHO", "HOH", and "OHH" are all valid answers for 1 molecule
        private int hydrogenCount = 2;
        private int oxygenCount = 1;

        public H2O_2() {

        }

        public void hydrogen(Runnable releaseHydrogen) throws InterruptedException {
            synchronized (this)
            {
                while (hydrogenCount == 0)
                {
                    this.wait();
                }
                // releaseHydrogen.run() outputs "H". Do not change or remove this line.
                releaseHydrogen.run();

                hydrogenCount--; // synchronized Guarantees Both Atomicity and Visibility
                callNextBatch();
            }
        }

        public void oxygen(Runnable releaseOxygen) throws InterruptedException {
            synchronized (this)
            {
                while (oxygenCount == 0)
                {
                    this.wait();
                }
                // releaseOxygen.run() outputs "O". Do not change or remove this line.
                releaseOxygen.run();

                oxygenCount--;
                callNextBatch();
            }

        }

        private void callNextBatch(){
            if (oxygenCount == 0 && hydrogenCount == 0)
            {
                hydrogenCount = 2;
                oxygenCount = 1;
                this.notifyAll();
            }
        }
    }

    public static class H2O_3 {

        private final Semaphore hydrogenSem = new Semaphore(2);
        private final Semaphore oxygenSem = new Semaphore(0); // blocked at first

        public H2O_3() {

        }

        public void hydrogen(Runnable releaseHydrogen) throws InterruptedException {
            hydrogenSem.acquire();
            // releaseHydrogen.run() outputs "H". Do not change or remove this line.
            releaseHydrogen.run();
            oxygenSem.release();
        }

        public void oxygen(Runnable releaseOxygen) throws InterruptedException {
            oxygenSem.acquire(2); // after hydrogen run two times, then it can run, need two permits
            // releaseOxygen.run() outputs "O". Do not change or remove this line.
            releaseOxygen.run();
            hydrogenSem.release(2); // next batch

            // use math combination to let concurrency run in order
        }
    }

    public static class H2O_4 {

        private final Semaphore hydrogenSem = new Semaphore(2);
        private final Semaphore oxygenSem = new Semaphore(1); // blocked at first

        // like the problem give, use barrier to control batch
        // also like use math combination but let concurrency run in group and Semaphore don't need release at each run end.
        // but Semaphore need control each group's upper limit, barrier can't identify which Semaphore need run how many times. (so must Semaphore + CyclicBarrier work together)
        private final CyclicBarrier barrier= new CyclicBarrier(3, this::callNextBatch);
        //barrier that will trip when the given number of threads are waiting, and which will execute the action when the barrier is tripped, performed by the last thread entering the barrier.

        public H2O_4() {

        }

        public void hydrogen(Runnable releaseHydrogen) throws InterruptedException {
            hydrogenSem.acquire();
            // releaseHydrogen.run() outputs "H". Do not change or remove this line.
            releaseHydrogen.run();
            try {
                barrier.await(); // block until barrier collects three calls.
            } catch (BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
        }

        public void oxygen(Runnable releaseOxygen) throws InterruptedException {
            oxygenSem.acquire();
            // releaseOxygen.run() outputs "O". Do not change or remove this line.
            releaseOxygen.run();
            try {
                barrier.await(); // block until barrier collects three calls.
            } catch (BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
        }

        private void callNextBatch() {
            hydrogenSem.release(2);
            oxygenSem.release(1);
        }
    }
}
