package com.brickintellect.exhibit;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import com.brickintellect.webserver.WebSocketSessionManager;

public class Main {

    /**
     * The exhibit controller entry point.
     */
    public static void main(String[] args) {

        // Set parameter defaults.
        String host = null; // bind to all interfaces by default
        int port = 80;
        File root = null;
        File keyStore = null;
        boolean daemon = false;

        // Parse the command-line.
        for (int i = 0; i < args.length; ++i) {
            if ("--daemon".equals(args[i])) {
                daemon = true;
            } else if ("--host".equals(args[i])) {
                host = args[++i];
            } else if ("--port".equals(args[i])) {
                port = Integer.parseInt(args[++i]);
            } else if ("--key-store".equals(args[i])) {
                keyStore = new File(args[++i]).getAbsoluteFile();
            } else if ("--root".equals(args[i])) {
                root = new File(args[++i]).getAbsoluteFile();
            }
        }

        // Create and launch the server!
        try {
            
            WebServer server = new WebServer(host, port, keyStore, root);

            // The default NanoHTTPD socket read timeout is 5 seconds. While that
            // may be fine for a HTTP server, it doesn't work so well when serving
            // persistent WebSocket connections.  We "ping" our clients every 5
            // seconds, three times that should work better.
            server.start(15 * 1000/* NanoHTTPD.SOCKET_READ_TIMEOUT */, false);

            if (daemon) {
                System.out.println("Exhibit controller running as daemon!");
                while (true) {
                    try {
                        Thread.sleep(1000 * 10);
                    } catch (InterruptedException ignored) {
                        break;
                    }
                }
            } else {
                System.out.println("Exhibit controller running.  Press Enter to stop.\n");
                // In order to test our client reconnection logic, we need the
                // ability to uncleanly shutdown our WebSockets.  Press 'a'
                // and then 'Enter' (instead of just 'Enter') to do that.
                try (Scanner scanner = new Scanner(System.in)) {
                    String input = scanner.nextLine();

                    if ("a".equals(input.toLowerCase())) {
                        WebSocketSessionManager.abort();
                    }
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
