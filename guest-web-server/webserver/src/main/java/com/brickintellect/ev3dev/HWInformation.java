package com.brickintellect.ev3dev;

public class HWInformation {

    public static boolean isHardware() {
        return new java.io.File("/dev/tty_ev3-ports:in1").exists();
        // Alternate implementation:
        // return !System.getProperty("os.name").toLowerCase().startsWith("windows";
    }
}