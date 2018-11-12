package com.brickintellect.exhibit;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIRailroad extends Remote
{
    static final String SERVICE_NAME = "RailroadService";
    
    String call(String value) throws RemoteException;
}