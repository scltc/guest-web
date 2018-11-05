package com.brickintellect.exhibit;

import java.util.UUID;

// https://stackoverflow.com/questions/1087475/when-does-javas-thread-sleep-throw-interruptedexception
// https://stackoverflow.com/questions/2258066/java-run-a-function-after-a-specific-number-of-seconds

public class PlaytimeTimer {

    public static interface IPlayTimerComplete {
        void playtimeTimerComplete(UUID guest, boolean aborted);
    }

    private Thread waiter = null;
    private long waitEndTime;

    public boolean abort() {
        Thread waiter = this.waiter;

        this.waiter = null;

        if (waiter == null) {
            return false;
        } else {
            waiter.interrupt();
            return true;
        }
    }

    public void play(UUID guest, final int delayInMilliseconds, IPlayTimerComplete callback) {

        // Abort any existing session.
        abort();

        // Start the new session.
        waiter = new Thread(() -> {
            waitEndTime = System.currentTimeMillis() + delayInMilliseconds;

            try {
                Thread.sleep(delayInMilliseconds);
                waiter = null;
                callback.playtimeTimerComplete(guest, false);
            } catch (InterruptedException e) {
                waiter = null;
                callback.playtimeTimerComplete(guest, true);
                Thread.currentThread().interrupt();
            }
        });
    }

    public long remaining() {
        return (waiter == null) ? 0 : Math.max(0, waitEndTime - System.currentTimeMillis());
    }
}
