package com.brickintellect.exhibit;

/*
*  State           Entry Switch    Exit Switch     Siding Break    Mainline Break
*
*  Stopped         Mainline        Mainline        Open            Closed
*  Starting        Siding          Siding          Closed          Closed
*  Running         Mainline        Mainline        Open            Closed
*  Stopping        Mainline        Mainline        Open            Open
*  Parking         Siding          Mainline
*/

public class Siding {

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

    public Siding(Settings settings) {

        this.settings = settings;

        entrySwitch = (settings.entryIsActuator) ? new TrackSwitch.Actuator(settings.entrySwitchPort)
                : new TrackSwitch.House(settings.entrySwitchPort);

        exitSwitch = (settings.exitIsActuator) ? new TrackSwitch.Actuator(settings.exitSwitchPort)
                : new TrackSwitch.House(settings.exitSwitchPort);

        sideBreakRelay = new LatchingRelay(settings.sideBreakPort);

        mainBreakRelay = new LatchingRelay(settings.mainBreakPort);
    }

}