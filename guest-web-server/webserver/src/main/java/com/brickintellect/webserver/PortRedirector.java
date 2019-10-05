package com.brickintellect.webserver;

import java.io.BufferedReader;
import java.io.BufferedOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class PortRedirector implements Runnable {

    private ServerSocket server;
    private final String target;
    private final Thread thread;

    public PortRedirector(final int port, final String target) throws IOException {
        this.target = target;
        this.server = new ServerSocket(port);
        this.thread = new Thread(this);
        this.thread.start();
    }

    private boolean stopping = false;

    @Override
    public void run() {
        while (!stopping) {
            // Wait for a request.
            try (Socket socket = server.accept()) {

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null && !line.equals("")) {
                        System.out.println(line);
                    }

                    try (PrintStream writer = new PrintStream(new BufferedOutputStream(socket.getOutputStream()))) {
                        String response = "HTTP/1.1 301 Moved Permanently\r\nLocation: " + target + "\r\n\r\n";
                        System.out.println(response);
                        writer.print(response);
                        writer.flush();
                    }
                }
            } catch (IOException exception) {
                System.out.println(exception);
            }
        }

        System.out.println("Redirector.run() : exiting.");
    }

    public void shutdown() {
        stopping = true;
        try {
            server.close();
        } catch (IOException ignored) {
        }
    }
}