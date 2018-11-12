package com.brickintellect.exhibit;

public class HeadTurnerState {
    public int status; // -1=Waiting, 0=Canceled, +1=Playing
    public int direction; // -1=left, +1=right;
    public long timer;
}