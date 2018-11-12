package com.brickintellect.exhibit;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class RMIRailroadClient {

    public static RMIRailroad Create(String host) throws MalformedURLException, NotBoundException, RemoteException {

        return (RMIRailroad) Naming.lookup(String.format("//%s/%s", host, RMIRailroad.SERVICE_NAME));
    }

    public static void main(String args[])
    {
        try
        {
            RMIRailroad railroad = Create("192.168.2.201");

            System.out.println(railroad.call("the value"));
        }
        catch (Exception exception)
        {
            System.out.println(exception.getMessage());
        }
    }
}