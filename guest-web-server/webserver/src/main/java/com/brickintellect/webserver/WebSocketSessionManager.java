package com.brickintellect.webserver;

import org.nanohttpd.protocols.websockets.CloseCode;
import org.nanohttpd.protocols.http.IHTTPSession;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.LinkedList;
import java.util.List;

// Inspiration:
// Websocket closes after being open
// https://github.com/NanoHttpd/nanohttpd/issues/441
// http://tutorials.jenkov.com/java-util-concurrent/scheduledexecutorservice.html#scheduledexecutorservice-example

class WebSocketSessionManager {
    // A list of currently connected sessions.
    private static final List<WebSocketSession> connections = new LinkedList<WebSocketSession>();
    // Queue connection list additions and deletions list so lock times are minimized.
    private static final List<WebSocketSession> additions = new LinkedList<WebSocketSession>();
    private static final List<WebSocketSession> deletions = new LinkedList<WebSocketSession>();

    public static WebSocketSession CreateSession(IHTTPSession session) {
        WebSocketSession result = new WebSocketSession(session);
        synchronized (additions) {
            if (!additions.contains(result)) {
                additions.add(result);
            }
        }
        return result;
    }

    public static void DeleteSession(WebSocketSession session) {
        synchronized (deletions) {
            if (!deletions.contains(session)) {
                deletions.add(session);
            }
        }
    }

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void stop() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();

            for (WebSocketSession session : connections) {
                try {
                    session.close(CloseCode.GoingAway, "Server shutdown.", false);
                } catch (Exception ignored) {
                }
            }
        }
    }

    static {
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("pinger");

            synchronized (additions) {
                connections.addAll(additions);
                additions.clear();
            }

            synchronized (deletions) {
                connections.removeAll(deletions);
                deletions.clear();
            }

            for (WebSocketSession session : connections) {
                try {
                    session.ping();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    DeleteSession(session);
                }
            }
        }, 5, 5, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("pinger shutdown.");
            stop();
        }));
    }
}