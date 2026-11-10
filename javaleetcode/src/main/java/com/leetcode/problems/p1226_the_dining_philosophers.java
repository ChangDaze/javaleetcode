package com.leetcode.problems;

import java.util.concurrent.Semaphore;

public class p1226_the_dining_philosophers {
    public static class DiningPhilosophers {
        // lock for each fork, to avoid picked up by more than one person.
        // fork0 means right fork for person 0
        private final Semaphore fork0 = new Semaphore(1);
        private final Semaphore fork1 = new Semaphore(1);
        private final Semaphore fork2 = new Semaphore(1);
        private final Semaphore fork3 = new Semaphore(1);
        private final Semaphore fork4 = new Semaphore(1);
        // avoid five persons pick up same side fork simultaneously
        private final Semaphore leftSem = new Semaphore(4);
        private final Semaphore rightSem = new Semaphore(4);

        public DiningPhilosophers() {

        }

        // call the run() method of any runnable to execute its code
        public void wantsToEat(int philosopher,
                               Runnable pickLeftFork,
                               Runnable pickRightFork,
                               Runnable eat,
                               Runnable putLeftFork,
                               Runnable putRightFork) throws InterruptedException {
            // pick up left fork then right fork
            switch (philosopher){
                case 0:
                    leftSem.acquire(); // need to check limit before actually pick up the fork
                    fork1.acquire(); // draw the person and fork around the table to check the actions' order and action on which fork
                    pickLeftFork.run();
                    rightSem.acquire();
                    fork0.acquire();
                    pickRightFork.run();
                    break;
                case 1:
                    leftSem.acquire();
                    fork2.acquire();
                    pickLeftFork.run();
                    rightSem.acquire();
                    fork1.acquire();
                    pickRightFork.run();
                    break;
                case 2:
                    leftSem.acquire();
                    fork3.acquire();
                    pickLeftFork.run();
                    rightSem.acquire();
                    fork2.acquire();
                    pickRightFork.run();
                    break;
                case 3:
                    leftSem.acquire();
                    fork4.acquire();
                    pickLeftFork.run();
                    rightSem.acquire();
                    fork3.acquire();
                    pickRightFork.run();
                    break;
                case 4:
                    leftSem.acquire();
                    fork0.acquire();
                    pickLeftFork.run();
                    rightSem.acquire();
                    fork4.acquire();
                    pickRightFork.run();
                    break;
                default:
                    throw new RuntimeException("not exist philosopher");
            }

            eat.run();

            // put down left fork then right fork
            switch (philosopher){
                case 0:
                    putLeftFork.run();
                    fork1.release();
                    leftSem.release(); // put down the fork before release limit
                    putRightFork.run();
                    fork0.release();
                    rightSem.release();
                    break;
                case 1:
                    putLeftFork.run();
                    fork2.release();
                    leftSem.release();
                    putRightFork.run();
                    fork1.release();
                    rightSem.release();
                    break;
                case 2:
                    putLeftFork.run();
                    fork3.release();
                    leftSem.release();
                    putRightFork.run();
                    fork2.release();
                    rightSem.release();
                    break;
                case 3:
                    putLeftFork.run();
                    fork4.release();
                    leftSem.release();
                    putRightFork.run();
                    fork3.release();
                    rightSem.release();
                    break;
                case 4:
                    putLeftFork.run();
                    fork0.release();
                    leftSem.release();
                    putRightFork.run();
                    fork4.release();
                    rightSem.release();
                    break;
                default:
                    throw new RuntimeException("not exist philosopher");
            }
        }
    }

    public static class DiningPhilosophers2 {
        // rightForkIndex = philosopher
        // leftForkIndex = (philosopher + 1) % 5
        // sit face table and clock wise
        private final Semaphore[] forks = new Semaphore[5];
        // because every philosopher picks up their left fork first, at most 4 philosophers can even reach the step of picking up a right fork, so only need leftSem or, tableLimit
        private final Semaphore tableLimit = new Semaphore(4);

        public DiningPhilosophers2() {
            for (int i = 0; i < 5; i++) {
                forks[i] = new Semaphore(1);
            }
        }

        // call the run() method of any runnable to execute its code
        public void wantsToEat(int philosopher,
                               Runnable pickLeftFork,
                               Runnable pickRightFork,
                               Runnable eat,
                               Runnable putLeftFork,
                               Runnable putRightFork) throws InterruptedException {
            Semaphore leftFork = forks[(philosopher + 1) % 5];
            Semaphore rightFork = forks[philosopher];

            // 1. not consider one fork at a time, limit the number of philosophers goes on table
            tableLimit.acquire();

            // 2. pick up left fork, then right fork for the question need
            leftFork.acquire(); // still need to avoid the race condition on same fork for one left fork as other's right fork
            pickLeftFork.run();
            rightFork.acquire();
            pickRightFork.run();

            // 3. eat
            eat.run();

            // 4. put down left fork, then right fork
            putLeftFork.run();
            leftFork.release();
            putRightFork.run();
            rightFork.release();

            // 5. leave the table only after both forks are back on the table
            tableLimit.release();
        }
    }

    public static class DiningPhilosophers3 {
        // rightForkIndex = philosopher
        // leftForkIndex = (philosopher + 1) % 5
        // sit face table and clock wise
        private final Semaphore[] forks = new Semaphore[5];

        public DiningPhilosophers3() {
            for (int i = 0; i < 5; i++) {
                forks[i] = new Semaphore(1);
            }
        }

        // call the run() method of any runnable to execute its code
        public void wantsToEat(int philosopher,
                               Runnable pickLeftFork,
                               Runnable pickRightFork,
                               Runnable eat,
                               Runnable putLeftFork,
                               Runnable putRightFork) throws InterruptedException {
            Semaphore leftFork = forks[(philosopher + 1) % 5];
            Semaphore rightFork = forks[philosopher];

            /*
             so the concept to avoid the deadlock in this condition,
             if one hold the enter key before grabbing other resources,
              the key is for every group of continuous  two person,
              you have key means you can start,
              if not means leave resources for others,
               and important point is in each group can only one person working,
               so even group member overlap, would not cause deadlock,
                because working person can not overlap.
             * */

            // asymmetric order to break circular wait
            // whoever loses their 1st fork holds 0 forks, leaving their other fork free for neighbors
            if (philosopher % 2 == 0) { // 0,2,4 left first
                leftFork.acquire();
                pickLeftFork.run();
                rightFork.acquire();
                pickRightFork.run();
            } else { //1,3 right first
                rightFork.acquire();
                pickRightFork.run();
                leftFork.acquire();
                pickLeftFork.run();
            }

            eat.run();

            // put down order can a same, because our check point is entry key
            putLeftFork.run();
            leftFork.release();
            putRightFork.run();
            rightFork.release();
        }
    }
}
