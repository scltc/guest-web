package com.brickintellect.webserver;

import java.io.File;
import java.io.IOException;

public class Main {

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

            WebServer server = new WebServer(host, port);

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
