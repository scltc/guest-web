package com.brickintellect.webserver;

import org.nanohttpd.protocols.http.IHTTPSession;

import java.util.LinkedList;

// Inspiration:
// Websocket closes after being open
// https://github.com/NanoHttpd/nanohttpd/issues/441
// http://tutorials.jenkov.com/java-util-concurrent/scheduledexecutorservice.html#scheduledexecutorservice-example
class WebSocketSessionManager {
    private static final LinkedList<WebSocketSession> additions = new LinkedList<WebSocketSession>();
    private static final LinkedList<WebSocketSession> deletions = new LinkedList<WebSocketSession>();
    private static final LinkedList<WebSocketSession> connections = new LinkedList<WebSocketSession>();

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


}