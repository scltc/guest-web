package com.brickintellect.webserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.nanohttpd.protocols.http.NanoHTTPD;

public class Main {

    /**
     * Creates an SSLSocketFactory for HTTPS. Pass a KeyStore resource with your
     * certificate and passphrase
     */
    public static SSLServerSocketFactory makeSSLSocketFactory(File keyAndTrustStorePath, char[] passphrase)
            throws IOException {
        try (InputStream keystoreStream = new FileInputStream(keyAndTrustStorePath)) {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(keystoreStream, passphrase);

            KeyManagerFactory keyManagerFactory = KeyManagerFactory
                    .getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keystore, passphrase);
            return NanoHTTPD.makeSSLSocketFactory(keystore, keyManagerFactory);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Starts as a standalone file server and waits for Enter.
     */
    public static void main(String[] args) {

        // Set parameter defaults.
        String host = null; // bind to all interfaces by default
        int port = 8080;
        File root = null;
        boolean daemon = false;
        // boolean verbose = false;
        // String cors = null;

        // Parse the command-line.
        for (int i = 0; i < args.length; ++i) {
            if ("--host".equalsIgnoreCase(args[i])) {
                host = args[i + 1];
            } else if ("--port".equalsIgnoreCase(args[i])) {
                port = Integer.parseInt(args[i + 1]);
                // } else if ("--verbose".equalsIgnoreCase(args[i])) {
                // verbose = true;
            } else if ("--root".equalsIgnoreCase(args[i])) {
                root = new File(args[i + 1]).getAbsoluteFile();
            } else if ("--daemon".equalsIgnoreCase(args[i])) {
                daemon = true;
                // } else if (args[i].startsWith("--cors")) {
                // cors = "*";
                // int equalIdx = args[i].indexOf('=');
                // if (equalIdx > 0) {
                // cors = args[i].substring(equalIdx + 1);
                // }
            }
        }

        // Launch the server!
        try {

            WebServer server;

            File keyStore = new File("/home/robot/certificates/exhibit.scltc.club.jks");

            if (!keyStore.exists()) {
                server = new WebServer(host, port);
            } else {
                server = new WebServer(host, 443);

                server.makeSecure(
                        makeSSLSocketFactory(
                                keyStore, "storePassword".toCharArray()),
                        null);
            }

            server.start(root);

            RMIRailroad rmi = RMIRailroadServer.Create();

            if (daemon) {
                System.out.println("Webserver running as daemon!");
                while (true) {
                    try {
                        Thread.sleep(1000 * 10);
                    } catch (InterruptedException ignored) {
                        break;
                    }
                }
            } else {
                System.out.println("Webserver running.  Press Enter to stop.\n");

                try {
                    System.in.read();
                } catch (Throwable ignored) {
                }
            }

            server.stop();

            System.out.println("Webserver stopped.\n");
            System.exit(0);

        } catch (IOException ioe) {

            System.err.println("Couldn't start server:\n" + ioe);
            System.exit(-1);

        }
    }
}
