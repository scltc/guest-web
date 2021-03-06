package com.brickintellect.exhibit;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.UUID;

/**
 * Manage a timed queue of playing/awaiting guests.
 */
public class PlaytimeManager implements PlaytimeTimer.IPlayTimerComplete {

    public static class Settings {

        int timeToPlay = 2 * 60 * 1000;
        int timeToPause = 0;
    }

    private Settings settings;
    // private Map<String, String> waiters = new Map<String, String>();

    public Settings getSettings() {
        return this.settings;
    }

    public Settings setSettings(Settings settings) {
        return this.settings = settings;
    }

    public enum PlaytimeState {
        CANCELED, WAITING, PLAYING
    }

    public interface IPlaytimeStatus {
        void playtimeStatus(UUID guest, PlaytimeState state, int timeRemaining);
    }

    private static class QueueEntry {
        UUID guest;
        IPlaytimeStatus observer;

        QueueEntry(UUID guest, IPlaytimeStatus observer) {
            this.guest = guest;
            this.observer = observer;
        }
    }

    private ArrayDeque<QueueEntry> queue = new ArrayDeque<QueueEntry>();

    /**
     * Test if specified guest is currently playing.
     * 
     * @param guest The guest identifier to test.
     * @return true if specified guest is playing, false otherwise.
     */

    public boolean isPlayingNow(UUID guest) {
        synchronized (queue) {
            return guest != null && queue.size() != 0 && queue.getFirst().guest == guest;
        }
    }

    /**
     * Get the playing status of a guest.
     * 
     * @param guest The guest identifier to test.
     * @return -1=playing, 0=unknown (not playing or waiting), +1=waiting.
     */
    public int getStatus(UUID guest) {
        int result = 0;

        synchronized (queue) {
            for (Iterator<QueueEntry> iterator = queue.iterator(); iterator.hasNext();) {

                QueueEntry entry = iterator.next();

                if (entry.guest.equals(guest)) {
                    // Is guest first in queue (currently playing guest)?
                    result = (entry == queue.getFirst()) ? -1 : +1;
                    break;
                }

            }
        }

        return result;
    }

    public int getTime(UUID guest) {

        synchronized (queue) {

            int waitTime = 0;

            if (!queue.isEmpty()) {

                boolean first = true;
                for (Iterator<QueueEntry> iterator = queue.iterator(); iterator.hasNext();) {
                    QueueEntry entry = iterator.next();
                    if (entry.guest.equals(guest)) {
                        break;
                    }
                    if (!first) {
                        waitTime += settings.timeToPlay + settings.timeToPause;
                    }

                }

                waitTime += timer.remaining() + settings.timeToPause;

                if (!first) {
                    waitTime *= -1;
                }
            }
            return waitTime;
        }
    }

    PlaytimeTimer timer = new PlaytimeTimer();

    public void playtimeTimerComplete(UUID guest, boolean aborted) {
        if (!aborted) {
            abandon(guest, false);
        }
    }

    /**
     * Abandon a playtime reservation.
     * 
     * @param guest The guest to abandon.
     */
    public int abandon(UUID guest, boolean silent) {

        synchronized (queue) {

            int waitTime = 0;

            boolean found = false;

            // Traverse the entire queue.
            for (Iterator<QueueEntry> iterator = queue.iterator(); iterator.hasNext();) {

                QueueEntry entry = iterator.next();

                // Is guest first in queue (currently playing guest)?
                boolean first = (entry == queue.getFirst());

                if (entry.guest.equals(guest)) {
                    // Found match.
                    found = true;

                    queue.remove(entry);

                    // Notify that this guest's turn is canceled.
                    if (!silent) {
                        entry.observer.playtimeStatus(entry.guest, PlaytimeState.CANCELED, 0);
                    }

                    if (first) {

                        timer.abort();

                        if (iterator.hasNext()) {
                            entry = iterator.next();
                            timer.play(entry.guest, settings.timeToPlay, this);

                            waitTime += settings.timeToPlay + settings.timeToPause;

                            // Let this guest know that their turn is active now!
                            entry.observer.playtimeStatus(entry.guest, PlaytimeState.PLAYING, settings.timeToPlay);
                        }
                    }
                } else {
                    // Not the matching guest.
                    if (found) {
                        // We have found a match, let downstream players know their adjusted wait time.
                        entry.observer.playtimeStatus(entry.guest, PlaytimeState.WAITING, waitTime);
                    }

                    if (first) {
                        waitTime += timer.remaining();
                    } else {
                        waitTime += settings.timeToPlay + settings.timeToPause;
                    }
                }
            }

            // Return the total time a user appended to the queue would wait.
            return waitTime;
        }
    }

    /**
     * Reserve a playtime.
     * 
     * @param guest    The guest identifier for which to reserve a playtime.
     * @param observer A callback to notify when the guest's state changes.
     */
    public void reserve(UUID guest, IPlaytimeStatus observer) {

        synchronized (queue) {

            // Ensure this guest not already waiting and get current wait time.
            int waitTime = abandon(guest, true);

            // Add guest to wait queue
            queue.add(new QueueEntry(guest, observer));

            if (waitTime == 0) {
                // No waiting! Let this guest know their turn has begun.
                observer.playtimeStatus(guest, PlaytimeState.PLAYING, settings.timeToPlay);

                timer.play(guest, settings.timeToPlay, this);
            } else {
                // Other guests waiting. Let this one know when their turn will be.
                observer.playtimeStatus(guest, PlaytimeState.WAITING, waitTime);
            }
        }
    }

    /**
     * Initialize a new playtime manager.
     * 
     * @param settings
     */
    public PlaytimeManager(Settings settings) {
        this.settings = settings;
    }
}