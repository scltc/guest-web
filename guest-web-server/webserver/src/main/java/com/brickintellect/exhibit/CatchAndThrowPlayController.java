package com.brickintellect.exhibit;

import java.util.UUID;

import com.brickintellect.exhibit.PlaytimeManager.PlaytimeState;

public class CatchAndThrowPlayController implements PlaytimeManager.IPlaytimeStatus {

    public static class CatchAndThrowPlayState {
        public int status; // -1=Waiting, 0=Canceled, +1=Playing
        public int direction; // -1=left, +1=right;
        public long timer;
        public boolean running;
    }

    public interface IStateChangeWatcher {
        void catcherStateChanged(UUID guest, CatchAndThrowPlayState state);
    }

    private CatchAndThrow catcher;
    private final PlaytimeManager playTimeManager = new PlaytimeManager(new PlaytimeManager.Settings());
    private IStateChangeWatcher stateChangeWatcher;

    @Override
    public void playtimeStatus(UUID guest, PlaytimeState state, int timeRemaining) {
        CatchAndThrowPlayState request = new CatchAndThrowPlayState();

        request.direction = catcher.getDirection();
        request.running = catcher.isRunning();
        request.timer = timeRemaining;

        if (state == PlaytimeManager.PlaytimeState.WAITING) {
            request.status = -1;
        } else if (state == PlaytimeManager.PlaytimeState.CANCELED) {
            request.status = 0;
        } else {
            request.status = +1;
        }

        try {
            stateChangeWatcher.catcherStateChanged(guest, request);
        } catch (Throwable ignored) {
        }
    }

    public CatchAndThrowPlayState headsAbandon(UUID guest) {
        playTimeManager.abandon(guest, false);
        CatchAndThrowPlayState result = new CatchAndThrowPlayState();
        result.status = 0;
        result.direction = 0;
        result.timer = 0;
        return result;
    }

    public CatchAndThrowPlayState headsOperate(UUID guest) {
        CatchAndThrowPlayState result = new CatchAndThrowPlayState();

        result.direction = catcher.getDirection();
        result.timer = playTimeManager.getTime(guest);
    
        if (result.timer < 0) {
            // Waiting.
            result.status = -1;
        } else if (result.timer > 0) {
            // Playing.
            result.status = +1;
        } else {
            // Idle.
            result.status = 0;
        }
 
        return result;
    }

    public CatchAndThrowPlayState headsReserve(UUID guest) {
        System.out.println("headsReserve");

        playTimeManager.reserve(guest, this);

        CatchAndThrowPlayState result = new CatchAndThrowPlayState();
        result.status = -1;
        result.direction = catcher.getDirection();
        result.timer = 1000 * 20;

        return result;
    }

    public CatchAndThrowPlayController(CatchAndThrow.Settings settings, IStateChangeWatcher watcher) {
        catcher = new CatchAndThrow(settings);
        stateChangeWatcher = watcher;
    }
}