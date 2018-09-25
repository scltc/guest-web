package com.brickintellect.ev3dev;

// import org.ev3dev.exception.InvalidPortException;
// import org.ev3dev.hardware.motors.DCMotor;

import com.brickintellect.ev3dev.LegoPortFactory;

public class PortInformation
{
    public void show(char port)
    {
        String[] modes = LegoPortFactory.create(port).getModes();
        for (String mode : modes) {
          System.out.println(mode);
        }
        System.out.println("---");
    }
}