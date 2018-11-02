package com.brickintellect.exhibit;

import java.util.ArrayDeque;
import java.util.Date;
import java.util.Iterator;
import java.util.Timer;
import java.util.UUID;

/**
 * Manage a timed queue of awaiting players.
 */
public class PlaytimeManager {

    public enum PlaytimeState {
        CANCELED, WAITING, PLAYING
    }

    public interface IPlaytimeStatus {
        void playtimeStatus(UUID player, PlaytimeState state, int timeRemaining);
    }

    public static class Settings {

        int timeToPlay = 5 * 60 * 1000;
        int timeToPause = 0;
    }

    public static class QueueEntry {
        UUID player;
        IPlaytimeStatus observer;

        QueueEntry(UUID player, IPlaytimeStatus observer) {
            this.player = player;
            this.observer = observer;
        }
    }

    private static class PlaytimeTimer {

        private Runnable waiter = null;
        private long waitEndTime;

        public void after(final int delayInMilliseconds)
        {
            waitEndTime = System.currentTimeMillis() + delayInMilliseconds;

            long timeToSleep = waitStarted + 

            try {
                Thread.sleep(delayInMilliseconds);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public long remaining() {
            return (waiter == null)
             ? 0
             : Math.max(0,  )
        }
    }

    // https://stackoverflow.com/questions/1087475/when-does-javas-thread-sleep-throw-interruptedexception
    // https://stackoverflow.com/questions/2258066/java-run-a-function-after-a-specific-number-of-seconds

    public class JavaUtil {
        public static void postDelayed(final Runnable runnable, final long delayMillis) {
            final long requested = System.currentTimeMillis();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            long leftToSleep = requested + delayMillis - System.currentTimeMillis();
                            if (leftToSleep > 0) {
                                Thread.sleep(leftToSleep);
                            }
                            break;
                        } catch (InterruptedException ignored) {
                        }
                    }
                    runnable.run();
                }
            }).start();
        }
    }

    private ArrayDeque<QueueEntry> queue = new ArrayDeque<QueueEntry>();

    private Settings settings;
    // private Map<String, String> waiters = new Map<String, String>();

    public Settings getSettings() {
        return this.settings;
    }

    public Settings setSettings(Settings settings) {
        return this.settings = settings;
    }

    public boolean isPlayingNow(UUID player) {
        synchronized (queue) {
            return player != null && queue.size() != 0 && queue.getFirst().player == player;
        }
    }

    /**
     * Abandon a playtime reservation.
     */
    public int abandon(UUID player) {

        synchronized (queue) {

            int waitTime = 0;

            boolean abandoned = false;

            for (Iterator<QueueEntry> iterator = queue.iterator(); iterator.hasNext();) {

                QueueEntry entry = iterator.next();

                if (!abandoned) {
                    // No player abandoned yet, keep checking for match.
                    if (entry.player.equals(player)) {
                        // Notify the player that their turn is canceled.
                        entry.observer.playtimeStatus(entry.player, PlaytimeState.CANCELED, 0);
                        queue.remove(entry);
                    } else {
                        waitTime += settings.timeToPlay + settings.timeToPause;
                    }
                } else {
                    // A player has been abandoned, notify those later in the queue.

                    if (waitTime == 0) {
                        // Let the first player know they are good to go now!
                        entry.observer.playtimeStatus(entry.player, PlaytimeState.PLAYING, settings.timeToPlay);
                    } else {
                        // Let other players know how long their wait will be.
                        entry.observer.playtimeStatus(entry.player, PlaytimeState.WAITING, waitTime);
                    }

                    waitTime += settings.timeToPlay + settings.timeToPause;
                }
            }

            // Return the total time the next user would wait.
            return waitTime;
        }
    }

    /**
     * Reserve a playtime.
     */
    public void reserve(UUID player, IPlaytimeStatus observer) {

        synchronized (queue) {

            // Ensure this player not already waiting and get current wait time.
            int waitTime = abandon(player);

            // Add player to wait queue
            queue.add(new QueueEntry(player, observer));

            if (waitTime == 0) {
                // No waiting! Let this player know.
                observer.playtimeStatus(player, PlaytimeState.PLAYING, settings.timeToPlay);
            } else {
                // Other players waiting, let this one know when their turn will be.
                observer.playtimeStatus(player, PlaytimeState.WAITING, waitTime);
            }
        }
    }

    /**
     * Create a play time manager.
     * 
     * @param settings
     */
    public PlaytimeManager(Settings settings) {
    }

}