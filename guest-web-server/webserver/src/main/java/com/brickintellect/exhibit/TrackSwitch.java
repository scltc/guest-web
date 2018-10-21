package com.brickintellect.exhibit;

import org.ev3dev.exception.InvalidPortException;
import org.ev3dev.hardware.motors.DCMotor;

import com.brickintellect.ev3dev.LegoPortFactory;

/***
 * Brick Intellect track switch controller.
 */

public class TrackSwitch {

  /***
   * Set track switch to mainline (straight) direction.
   */
  public static final int MAIN = -1;
  /***
   * Leave track swich direction unchanged.
   */
  public static final int SAME = 0;
  /***
   * Set track switch to siding (left or right) direction.
   */
  public static final int SIDE = +1;

  protected static class DirectionParameters {
    public DirectionParameters(int runTime, int dutyCycle) {
      this.runTime = runTime;
      this.dutyCycle = dutyCycle;
    }

    public int dutyCycle;
    public int runTime;
  }

  private DCMotor motor;
  private DirectionParameters main;
  private DirectionParameters side;

  protected TrackSwitch(char port, boolean invert, DirectionParameters main, DirectionParameters side)
      throws InvalidPortException {

    motor = new DCMotor(LegoPortFactory.createDCMotor(port));

    motor.setStopAction("brake");
    motor.stop();

    if (invert) {
      this.main = side;
      this.side = main;
    } else {
      this.main = main;
      this.side = side;
    }
  }

  public void set(int direction) {
    if (direction != SAME) {
      DirectionParameters parameters = (direction == MAIN) ? main : side;
      motor.setTime_SP(parameters.runTime);
      motor.setDutyCycleSP(parameters.dutyCycle);
      motor.runTimed();
    }
  }
  
  public void test(int repetitions) {
    try {
      for (int i = 0; i < repetitions; ++i) {
        int side = (i % 2 == 0) ? TrackSwitch.MAIN : TrackSwitch.SIDE;
        set(side);
        System.out.println((side == TrackSwitch.MAIN) ? "main" : "side");
        try {
          Thread.sleep(1000 * 2);
        } catch (InterruptedException ignored) {
        }
      }
    } catch (Exception exception) {
      System.out.println(exception.getMessage());
    }
  }

  /**
   * The track switch implemented with a small LEGO linear actuator.
   */
  public static class Actuator extends TrackSwitch {

    // Default switch orientation for right switch
    static final DirectionParameters mainParameters = new DirectionParameters(750, -100);
    static final DirectionParameters sideParameters = new DirectionParameters(750, +100);
    // static final DirectionParameters mainParameters = new
    // DirectionParameters(4000, +34);
    // static final DirectionParameters sideParameters = new
    // DirectionParameters(4000, -34);

    public Actuator(char port, boolean invert) {
      super(port, invert, mainParameters, sideParameters);
    }

    public Actuator(char port) {
      this(port, false);
    }
  }

  /**
   * The track switch implemented in a switch house.
   */
  public static class House extends TrackSwitch {

    private static final DirectionParameters mainParameters = new DirectionParameters(5000, +100);
    private static final DirectionParameters sideParameters = new DirectionParameters(5000, -100);

    public House(char port, boolean invert) {
      super(port, invert, mainParameters, sideParameters);
    }

    public House(char port) {
      this(port, false);
    }
  }
}