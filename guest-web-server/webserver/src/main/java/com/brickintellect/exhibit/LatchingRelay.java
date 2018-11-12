package com.brickintellect.exhibit;

import org.ev3dev.hardware.ports.LegoPort;
import org.ev3dev.hardware.motors.DCMotor;

import com.brickintellect.ev3dev.HWInformation;
import com.brickintellect.ev3dev.LegoPortFactory;

public class LatchingRelay {

    public static final boolean RESET = false;
    public static final boolean LATCH = !RESET;

    // It seems as though running the motor repeatedly over (perhaps) only
    // part of its rotation would not be good. We run it a little longer
    // in one direction than the other, to ensure it rotates completely.

    private static final int LatchTime = 100;
    private static final int ResetTime = 120;

    private DCMotor motor;
    private boolean state;
    private boolean invert;

    private LatchingRelay(LegoPort port, boolean invert, boolean state) {
        if (port == null) {
            System.out.println("LatchingRelay(" + port + ", " + invert + ", " + state + ")");
            motor = null;
        } else {
            motor = new DCMotor(port);
            motor.setStopAction("brake");
            motor.stop();
        }

        this.invert = invert;

        set(state);
    }

    public LatchingRelay(char port, boolean invert, boolean state) {
        this(LegoPortFactory.createDCMotor(port), invert, state);
    }

    public LatchingRelay(String port, boolean invert, boolean state) {
        this(LegoPortFactory.createDCMotor(port), invert, state);
    }

    public LatchingRelay(char port, boolean invert) {
        this(port, invert, RESET);
    }

    public LatchingRelay(String port, boolean invert) {
        this(port, invert, RESET);
    }

    public LatchingRelay(char port) {
        this(port, false, RESET);
    }

    public LatchingRelay(String port) {
        this(port, false, RESET);
    }

    public synchronized void set(boolean state) {

        this.state = state;

        if (motor == null) {
            System.out.println("LatchingRelay.set(" + state + ")");
        } else {
            if ((invert ^ state) == LATCH) {
                motor.setTime_SP(LatchTime);
                motor.setDutyCycleSP(+100);
            } else {
                motor.setTime_SP(ResetTime);
                motor.setDutyCycleSP(-100);
            }

            motor.runTimed();
        }
    }

    public boolean get() {
        return state;
    }

    public void test(int repetitions) {
        try {
            for (int i = 0; i < repetitions; ++i) {
                boolean latch = (i % 2 == 0) ? LatchingRelay.RESET : LatchingRelay.LATCH;
                set(latch);
                System.out.println((latch == LatchingRelay.RESET) ? "reset" : "latch");
                try {
                    Thread.sleep(1000 * 2);
                } catch (InterruptedException ignored) {
                }
            }
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }
}
