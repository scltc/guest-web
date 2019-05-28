package com.brickintellect.exhibit;

import java.util.Random;

public class CatchAndThrow implements Runnable {

    public static class Settings {

        public boolean enabled = true;

        public String controller = null;

        public String westPort = "A";
        public int westMinIdle = 1000 * 20;
        public int westMaxIdle = 1000 * 60;

        public String mainPort = "B";
        public int mainRunTime = 1000 * 10;

        public String eastPort = "C";
        public int eastMinIdle = westMinIdle;
        public int eastMaxIdle = westMaxIdle;
    }

    private Settings settings;

    private LatchingRelay westRelay;
    private LatchingRelay mainRelay;
    private LatchingRelay eastRelay;

    private int direction = -1; // -1=west, +1=east

    public int getDirection() {
        return direction;
    }

    private boolean running = false;

    public boolean isRunning() {
        return running;
    }

    private Random random = new Random();

    Thread runner = null;

    private int getIdleTimeout() {
        int minimum = (direction < 0) ? settings.westMinIdle : settings.eastMinIdle;
        int maximum = (direction < 0) ? settings.westMaxIdle : settings.eastMaxIdle;

        return random.nextInt(maximum - minimum) + minimum;
    }

    @Override
    public void run() {
        try {
            while (true) {
                // Allow train time to traverse the mainline.

                System.out.println("Running.");

                running = true;

                Thread.sleep(settings.mainRunTime);

                running = false;

                System.out.println("Before wait.");

                do {
                    synchronized (runner) {
                        runner.wait(getIdleTimeout());
                    }
                } while (!settings.enabled);

                System.out.println("After wait.");

                if (settings.enabled) {
                    if (direction < 0) {
                        // Set the mainline direction to west.
                        mainRelay.set(LatchingRelay.RESET);

                        // Enable the west track end to get train to the mainline.
                        westRelay.set(LatchingRelay.LATCH);
                        Thread.sleep(settings.mainRunTime / 2);
                        westRelay.set(LatchingRelay.RESET);
                        // Train will be at the east end.
                        direction = +1;
                    } else if (direction > 0) {
                        // Set the mainline direction to east.
                        mainRelay.set(LatchingRelay.LATCH);

                        // Enable the east track end to get train to the mainline.
                        eastRelay.set(LatchingRelay.LATCH);
                        Thread.sleep(settings.mainRunTime / 2);
                        eastRelay.set(LatchingRelay.RESET);
                        // Train will be at the west end.
                        direction = -1;
                    }
                }
            }
        } catch (InterruptedException exception) {
            System.out.println(exception);
        }
    }

    public CatchAndThrow(Settings settings) {

        this.settings = settings;

        westRelay = new LatchingRelay(settings.westPort);
        mainRelay = new LatchingRelay(settings.mainPort);
        eastRelay = new LatchingRelay(settings.eastPort);

        runner = new Thread(this);
        runner.start();
    }

    public CatchAndThrow() {
    }

    public int go() {
        System.out.println("CatchAndThrow.go() {");
        synchronized (runner) {
            runner.notify();
        }
        System.out.println("CatchAndThrow.go() }");
        return direction;

    }
}
