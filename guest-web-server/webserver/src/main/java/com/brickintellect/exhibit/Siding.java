package com.brickintellect.exhibit;

/*
*  State           Entry Switch    Exit Switch     Siding Break    Mainline Break
*
*  Stopped         Mainline        Mainline        Open            Shut
*  Starting        Siding          Siding          Shut            Shut
*  Running         Mainline        Mainline        Open            Shut
*  Stopping        Mainline        Mainline        Open            Open
*  Parking         Siding          Mainline        Open            Shut
*/

public class Siding {

    private static class State {
        int entrySwitch;
        int exitSwitch;
        boolean sideBreak;
        boolean mainBreak;

        public State(int entrySwitch, int exitSwitch, boolean sideBreak, boolean mainBreak) {
            this.entrySwitch = entrySwitch;
            this.exitSwitch = exitSwitch;
            this.sideBreak = sideBreak;
            this.mainBreak = mainBreak;
        }
    }

    private static final State[] STATES = {
            // ______ Entry switch ____ Exit switch _____ Side break ________ Main break
            new State(TrackSwitch.MAIN, TrackSwitch.MAIN, LatchingRelay.OPEN, LatchingRelay.SHUT), // Stopped
            new State(TrackSwitch.SIDE, TrackSwitch.SIDE, LatchingRelay.SHUT, LatchingRelay.SHUT), // Starting
            new State(TrackSwitch.MAIN, TrackSwitch.MAIN, LatchingRelay.OPEN, LatchingRelay.SHUT), // Running
            new State(TrackSwitch.MAIN, TrackSwitch.MAIN, LatchingRelay.OPEN, LatchingRelay.OPEN), // Pause
            new State(TrackSwitch.SIDE, TrackSwitch.MAIN, LatchingRelay.OPEN, LatchingRelay.SHUT) // Stopping
    };

    public int[] times = new int[] {
            // Stopped
            0,
            // Starting
            1000 * 2,
            // Running
            0,
            // Pause
            1000 * 20,
            // Stopping
            1000 * 10
            // This comment keeps closing brace below safe from VSCode reformatting...
    };

    public static class Settings {

        public boolean enabled = true;

        public String controller = null;

        public String entrySwitchPort = "A";
        public boolean entryIsActuator = false;

        public String exitSwitchPort = "B";
        public boolean exitIsActuator = false;

        public String sideBreakPort = "C";

        public String mainBreakPort = "D";
    }

    private Settings settings;

    private TrackSwitch entrySwitch;
    private TrackSwitch exitSwitch;
    private LatchingRelay sideBreakRelay;
    private LatchingRelay mainBreakRelay;

    public void setState(State state) {
        entrySwitch.set(state.entrySwitch);
        exitSwitch.set(state.exitSwitch);
        sideBreakRelay.set(state.sideBreak);
        mainBreakRelay.set(state.mainBreak);
    }

    public Siding(final Settings settings) {

        this.settings = settings;

        entrySwitch = (settings.entryIsActuator) ? new TrackSwitch.Actuator(settings.entrySwitchPort)
                : new TrackSwitch.House(settings.entrySwitchPort);

        exitSwitch = (settings.exitIsActuator) ? new TrackSwitch.Actuator(settings.exitSwitchPort)
                : new TrackSwitch.House(settings.exitSwitchPort);

        sideBreakRelay = new LatchingRelay(settings.sideBreakPort);

        mainBreakRelay = new LatchingRelay(settings.mainBreakPort);
    }

}