package com.brickintellect.exhibit;

import java.util.UUID;

import com.brickintellect.exhibit.HeadTurner;
import com.brickintellect.exhibit.PlaytimeManager.PlaytimeState;

public class HeadTurnerPlayController implements PlaytimeManager.IPlaytimeStatus {

    public interface IStateChangeWatcher {
        void headsStateChanged(UUID guest, HeadTurnerState state);
    }

    private HeadTurner turner;
    private final PlaytimeManager headsManager = new PlaytimeManager(new PlaytimeManager.Settings());
    private IStateChangeWatcher stateChangeWatcher;

    @Override
    public void playtimeStatus(UUID guest, PlaytimeState state, int timeRemaining) {
        HeadTurnerState request = new HeadTurnerState();
        if (state == PlaytimeManager.PlaytimeState.WAITING) {
            request.status = -1;
        } else if (state == PlaytimeManager.PlaytimeState.CANCELED) {
            request.status = 0;
        } else {
            request.status = +1;
        }
        request.direction = turner.getDirection();
        request.timer = timeRemaining;
        try {
            stateChangeWatcher.headsStateChanged(guest, request);
        } catch (Throwable ignored) {
        }
    }

    public HeadTurnerState headsAbandon(UUID guest) {
        headsManager.abandon(guest, false);
        HeadTurnerState result = new HeadTurnerState();
        result.status = 0;
        result.direction = 0;
        result.timer = 0;
        return result;
    }

    public HeadTurnerState headsOperate(UUID guest, int direction) {
        HeadTurnerState result = new HeadTurnerState();

        result.timer = headsManager.getTime(guest);
        if (result.timer < 0) {
            result.direction = turner.getDirection();
            result.status = -1;
        } else if (result.timer > 0) {
            result.direction = turner.setDirection(direction);
            result.status = +1;
        } else {
            result.direction = turner.getDirection();
            result.status = 0;
        }
 
        return result;
    }

    public HeadTurnerState headsReserve(UUID guest) {
        System.out.println("headsReserve");

        headsManager.reserve(guest, this);

        HeadTurnerState result = new HeadTurnerState();
        result.status = -1;
        result.direction = turner.getDirection();
        result.timer = 1000 * 20;

        return result;
    }

    public HeadTurnerPlayController(HeadTurner.Settings settings, IStateChangeWatcher watcher) {
        turner = new HeadTurner(settings);
        stateChangeWatcher = watcher;
    }
}