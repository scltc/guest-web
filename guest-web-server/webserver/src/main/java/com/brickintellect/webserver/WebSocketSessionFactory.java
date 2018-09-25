package com.brickintellect.webserver;

import org.nanohttpd.protocols.http.IHTTPSession;

public class WebSocketSessionFactory {

    public WebSocketSessionFactory() {
    }

    public WebSocketSession createSession(IHTTPSession session, Object service, Class<?> serviceClass) {
        return new WebSocketSession(session, service, serviceClass);
    }
}