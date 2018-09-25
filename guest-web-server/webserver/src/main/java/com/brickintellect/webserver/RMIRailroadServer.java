package com.brickintellect.webserver;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMIRailroadServer extends UnicastRemoteObject implements RMIRailroad {

    private static final long serialVersionUID = -9847368908925158L;

    public RMIRailroadServer(String host) throws MalformedURLException, RemoteException {

        System.out.println(host);
        Naming.rebind(String.format("//%s/%s", host, RMIRailroad.SERVICE_NAME), this);
    }

    @Override
    public String call(String value) {
    
        return value + " returned from server.";
    }

    public static RMIRailroad Create(String host) throws MalformedURLException, RemoteException {

        LocateRegistry.createRegistry(Registry.REGISTRY_PORT);

        return new RMIRailroadServer(host);
    }

    public static RMIRailroad Create() throws MalformedURLException, RemoteException {
        return Create(RMIHelper.getCurrentIp4().getHostAddress());
    }
}