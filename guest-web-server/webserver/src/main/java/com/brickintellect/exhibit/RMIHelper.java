package com.brickintellect.exhibit;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class RMIHelper
{
    // https://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java
    // https://stackoverflow.com/questions/2381316/java-inetaddress-getlocalhost-returns-127-0-0-1-how-to-get-real-ip
    public static Inet4Address getCurrentIp4() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) networkInterfaces.nextElement();
                Enumeration<InetAddress> nias = ni.getInetAddresses();
                while (nias.hasMoreElements()) {
                    InetAddress ia = (InetAddress) nias.nextElement();
                    if (!ia.isLinkLocalAddress() && !ia.isLoopbackAddress() && ia instanceof Inet4Address) {
                        return (Inet4Address) ia;
                    }
                }
            }
        } catch (SocketException e) {
            //LOG.error("unable to get current IP " + e.getMessage(), e);
        }
        return null;
    }
}