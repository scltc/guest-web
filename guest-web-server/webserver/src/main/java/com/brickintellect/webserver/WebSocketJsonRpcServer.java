package com.brickintellect.webserver;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.googlecode.jsonrpc4j.JsonRpcBasicServer;

public class WebSocketJsonRpcServer implements WebSocketSession.IEndpoint{
    
    private final WebSocketSession session;
    private final int endpointNumber;
    private final JsonRpcBasicServer rpcServer;

    public WebSocketJsonRpcServer(final WebSocketSession session, final int endpointNumber, ObjectMapper mapper, final Object service, final Class<?> serviceClass) {
        this.session = session;
        this.endpointNumber = endpointNumber;
        rpcServer = new JsonRpcBasicServer(mapper, service, serviceClass);
    }

    public int endpointNumber() {
        return endpointNumber;
    }

    public void onMessage(final WebSocketInputStream inputStream) {
        System.out.println("WebSocketSession.OnMessage() {");
        try {
                try (WebSocketOutputStream outputStream = new WebSocketOutputStream(session, inputStream.getEndpointNumber())) {
                    rpcServer.handleRequest(inputStream, outputStream);
                }
        } catch (IOException exception) {
            System.out.println(exception.toString());
            throw new RuntimeException(exception);
        }
        System.out.println("WebSocketSession.OnMessage() }");
    }    
}
