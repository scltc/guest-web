package com.brickintellect.exhibit;

import org.ev3dev.hardware.motors.DCMotor;

import com.brickintellect.ev3dev.HWInformation;
import com.brickintellect.ev3dev.LegoPortFactory;

public class HeadTurner {

    public static class Settings {

        public int index;

        public boolean enabled = true;

        public String controller = null;

        public String port = "D";
        public int leftDutyCycle = +33;
        public int rightDutyCycle = -33;
        public int motorRunTime = 160;

        public Settings(int index) {
            this.index = index;
        }

        public Settings() {
            this(0);
        }        
    }

    private Settings settings;

    public Settings getSettings() {
        return this.settings;
    }

    public Settings setSettings(Settings settings) {
        return this.settings = settings;
    }

    final private DCMotor motor;

    public HeadTurner(final Settings settings) {

        if (!HWInformation.isHardware()) {
            System.out.println("HeadTurner()");
            motor = null;
        } else {
            motor = new DCMotor(LegoPortFactory.createDCMotor(settings.port));
            motor.setStopAction("coast");
            motor.stop();
        }

        this.settings = settings;

        setDirection(-1);
    }

    public static final int DIRECTION_LEFT = -1;
    public static final int DIRECTION_RIGHT = +1;
    public static final int DIRECTION_TOGGLE = 0;

    private int direction;

    public int getDirection() {
        return this.direction;
    }

    public int setDirection(int direction) {

        if (direction == 0) {
            direction = this.direction * -1;
        }

        if (motor == null) {
            System.out.println("HeadTurner.setDirection(" + direction + ")");
        } else {
            if (direction < 0) {
                motor.setDutyCycleSP(settings.leftDutyCycle);
            } else {
                motor.setDutyCycleSP(settings.rightDutyCycle);
            }

            motor.setTime_SP(settings.motorRunTime);
            motor.runTimed();
        }

        return this.direction = direction;
    }
}