package com.brickintellect.exhibit;

import java.util.Map;

public class WaitingRoom {

    public static class Settings {

        int timeToPlay = 5 * 60 * 1000;

    }

    public static class QueueEntry {
        
    }

    private Settings settings;
    //private Map<String, String> waiters = new Map<String, String>();

    public Settings getSettings()
    {
        return this.settings;
    }

    public Settings setSettings(Settings settings) {
        return this.settings = settings;
    }

    public WaitingRoom(Settings settings) {
    }

}