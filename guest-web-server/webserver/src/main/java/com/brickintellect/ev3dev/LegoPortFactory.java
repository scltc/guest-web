package com.brickintellect.ev3dev;

import org.ev3dev.exception.InvalidPortException;
import org.ev3dev.hardware.ports.LegoPort;

public final class LegoPortFactory {

    public static LegoPort create(char port) throws InvalidPortException {
        if (!HWInformation.isHardware()) {
            System.out.println("LegoPort.create(" + port + ")");
            return null;
        } else {
            switch (port) {
            case '1':
                return new LegoPort(LegoPort.INPUT_1);
            case '2':
                return new LegoPort(LegoPort.INPUT_2);
            case '3':
                return new LegoPort(LegoPort.INPUT_3);
            case '4':
                return new LegoPort(LegoPort.INPUT_4);
            case 'A':
                return new LegoPort(LegoPort.OUTPUT_A);
            case 'B':
                return new LegoPort(LegoPort.OUTPUT_B);
            case 'C':
                return new LegoPort(LegoPort.OUTPUT_C);
            case 'D':
                return new LegoPort(LegoPort.OUTPUT_D);
            default:
                throw new InvalidPortException(String.valueOf(port));
            }
        }
    }

    public static LegoPort createInput(char port) throws InvalidPortException {
        if (port < '0' || port > '4') {
            throw new InvalidPortException(String.valueOf(port));
        }

        return create(port);
    }

    public static LegoPort createOutput(char port) throws InvalidPortException {
        port = Character.toUpperCase(port);

        if (port < 'A' || port > 'D') {
            throw new InvalidPortException(String.valueOf(port));
        }

        return create(port);
    }

    public static LegoPort createOutput(char port, String mode) throws InvalidPortException {
        LegoPort result = createOutput(port);
        if (result != null) {
            result.setMode(mode);
        }
        return result;
    }

    public static LegoPort createDCMotor(char port) {
        return createOutput(port, "dc-motor");
    }

    public static LegoPort createDCMotor(String port) {
        if (port == null || port.length() == 0) {
            throw new InvalidPortException(port);
        }

        return createOutput(port.charAt(port.length() - 1), "dc-motor");
    }
}