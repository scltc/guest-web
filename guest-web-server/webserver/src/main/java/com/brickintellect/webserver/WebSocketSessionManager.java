package com.brickintellect.webserver;

import org.nanohttpd.protocols.websockets.CloseCode;
import org.nanohttpd.protocols.http.IHTTPSession;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

// Inspiration:
// Websocket closes after being open
// https://github.com/NanoHttpd/nanohttpd/issues/441
// http://tutorials.jenkov.com/java-util-concurrent/scheduledexecutorservice.html#scheduledexecutorservice-example

public class WebSocketSessionManager {
    // A list of currently connected sessions.
    private static final List<WebSocketSession> connections = new LinkedList<WebSocketSession>();
    // Queue connection list additions and deletions list so lock times are
    // minimized.
    private static final List<WebSocketSession> additions = new LinkedList<WebSocketSession>();
    private static final List<WebSocketSession> deletions = new LinkedList<WebSocketSession>();

    /**
     * Create a new WebSocketSession.
     * 
     * @param session
     * @return
     */
    public static WebSocketSession createSession(WebSocketSession session) {
        synchronized (additions) {
            if (!additions.contains(session)) {
                additions.add(session);
            }
        }
        return session;
    }

    public static WebSocketSession createSession(IHTTPSession session) {
        return createSession(new WebSocketSession(session));
    }

    /**
     * Delete a WebSocketSession.
     * 
     * @param session
     */
    public static void deleteSession(WebSocketSession session) {
        synchronized (deletions) {
            if (!deletions.contains(session)) {
                deletions.add(session);
            }
        }
    }

    public static List<WebSocketSession> getConnections() {

        synchronized (additions) {
            connections.addAll(additions);
            additions.clear();
        }

        synchronized (deletions) {
            connections.removeAll(deletions);
            deletions.clear();
        }

        return connections;
    }

    public static WebSocketSession getConnection(UUID identifier) {
        WebSocketSession result = null;
        synchronized (connections) {
            for (WebSocketSession session : getConnections()) {
                if (session.getClientIdentifier().equals(identifier)) {
                    result = session;
                    break;
                }
            }
        }
        return result;
    }

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * Shutdown the WebSocketSessionManager and close all connected WebSockets.
     */
    private static void shutdown(boolean abort) {
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();

            if (!abort) {
                for (WebSocketSession session : getConnections()) {
                    try {
                        session.close(CloseCode.GoingAway, "Server shutdown.", false);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    public static void shutdown() {
        shutdown(false);
    }

    public static void abort() {
        shutdown(true);
    }

    static {
        scheduler.scheduleAtFixedRate(() -> {
            // System.out.println("pinger");

            for (WebSocketSession session : getConnections()) {
                try {
                    session.ping();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    deleteSession(session);
                }
            }
        }, 5, 5, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("pinger shutdown.");
            shutdown();
        }));
    }
}